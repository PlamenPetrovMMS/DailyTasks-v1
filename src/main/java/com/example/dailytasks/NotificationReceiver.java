package com.example.dailytasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.database.Cursor;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class NotificationReceiver extends BroadcastReceiver {
    public static int REQUEST_CODE = 0;
    public static final String CHANNEL_ID = "TASK";
    private static int notificationId = 0;
    private Context CONTEXT;



    @SuppressLint("ScheduleExactAlarm")
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "Notification triggered at: " + SystemClock.elapsedRealtime());

        CONTEXT = context;
        DBHelper dbHelper = new DBHelper(context);

        int taskId = Integer.parseInt(intent.getExtras().get(DBHelper.COLUMN_ID).toString());
        Task task = dbHelper.getTask(taskId);

        String groupKey = "task_group_" + task.getId();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, NotificationReceiver.class);

        // Check if task is done
        if(task.getId() != -1 && dbHelper.isTaskDone(taskId)){
            // Cancel alarm scheduler
            stopAlarmManager(alarmManager, taskId, alarmIntent);
            return;
        }else{
            // Create next alarm
            createNextAlert(task, alarmManager);
        }

        showNotification(task, groupKey);

    } // onReceive ends here ==============

    private void showNotification(Task task, String groupKey){
        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(CONTEXT, CHANNEL_ID)
                .setSmallIcon(R.drawable.dt_logo)
                .setContentTitle(task.getTitle())
                .setContentText(task.getDescription())
                .setGroup(groupKey)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(CONTEXT);

        // Check for permission
        if(checkPermission()) return;

        // Notify the device with single notification
        notificationManager.notify(notificationId, builder.build());

        // Change id for next notification to avoid overriding
        notificationId++;

        // Create summary
        Notification notificationSummary = new NotificationCompat.Builder(CONTEXT, CHANNEL_ID)
                .setSmallIcon(R.drawable.dt_logo)
                .setContentTitle("DailyTasks")
                .setContentText("You have multiple tasks for today!")
                .setGroup(groupKey)
                .setGroupSummary(true)
                .build();

        // Notify the device with the summary
        notificationManager.notify((int) task.getId() + 1000, notificationSummary);
    }
    private boolean checkPermission(){
        if (ActivityCompat.checkSelfPermission(CONTEXT, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Intent requestIntent = new Intent(CONTEXT, PermissionRequestActivity.class);
            requestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            CONTEXT.startActivity(requestIntent);
            return false;
        }
        return true;
    }

    public void stopAlarmManager(AlarmManager alarmManager, int taskId, Intent alarmIntent){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                CONTEXT,
                taskId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d("NotificationReceiver", "Task is done. Alarm cancelled.");
    }

    @SuppressLint("ScheduleExactAlarm")
    private void createNextAlert(Task task, AlarmManager alarmManager){

        int taskId = (int) task.getId();

        long hourMillis = TimeUnit.HOURS.toMillis(task.getNotificationHours());
        long minuteMillis = TimeUnit.MINUTES.toMillis(task.getNotificationMinutes());

        long intervalInMillis = hourMillis + minuteMillis;

        Intent alarmIntent = new Intent(CONTEXT, NotificationReceiver.class);
        alarmIntent.putExtra("task_id", String.valueOf(task.getId()));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                CONTEXT,
                taskId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = SystemClock.elapsedRealtime() + intervalInMillis;
        if(alarmManager != null){
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
        Log.d("NotificationReceiver", "Setting notification for task at " + triggerTime + " (interval: " + intervalInMillis + ")");
    }
} // NotificationReceiver ends here =======

