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
    public static final String INTENT = "TASK_NOTIFICATION";



    private static int notificationId = 0;
    private AlarmManager alarmManager;
    private Context CONTEXT;
    private int taskId;
    private Intent alarmIntent;



    @SuppressLint("ScheduleExactAlarm")
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "Notification triggered at: " + SystemClock.elapsedRealtime());
        CONTEXT = context;

        DBHelper dbHelper = new DBHelper(context);

        taskId = Integer.parseInt(intent.getExtras().get("task_id").toString());
        Task task = dbHelper.getTask(taskId);

        String groupKey = "task_group_" + task.getId();

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = new Intent(context, NotificationReceiver.class);

        // Cancel alarm scheduler
        if(task.getId() != -1 && dbHelper.isTaskDone(task.getId())){
            stopAlarmManager(alarmManager, CONTEXT, taskId, alarmIntent);
        }else{
            // Create next alarm
            long hourMillis = TimeUnit.HOURS.toMillis(task.getNotificationHours());
            long minuteMillis = TimeUnit.MINUTES.toMillis(task.getNotificationMinutes());

            long intervalInMillis = hourMillis + minuteMillis;

            alarmIntent = new Intent(context, NotificationReceiver.class);
            alarmIntent.putExtra("task_id", String.valueOf(task.getId()));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
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
            Log.d("AlarmScheduler", "Setting alarm for task at " + triggerTime + " (interval: " + intervalInMillis + ")");
        }
        // Check for permission
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Activity activity = (Activity) context;
            ActivityCompat.requestPermissions(activity , new String[]{
                    Manifest.permission.POST_NOTIFICATIONS
            }, REQUEST_CODE);
            return;
        }

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.dt_logo)
                .setContentTitle(task.getTitle())
                .setContentText(task.getDescription())
                .setGroup(groupKey)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Notify the device with single notification
        notificationManager.notify(notificationId, builder.build());
        notificationId++;

        // Create notification summary
        Notification notificationSummary = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.dt_logo)
                .setContentTitle("DailyTasks")
                .setContentText("You have multiple tasks for today!")
                .setGroup(groupKey)
                .setGroupSummary(true)
                .build();

        // Notify the device with the summary
        notificationManager.notify(taskId + 1000, notificationSummary);

    } // onReceive ends here ==============

    public static void stopAlarmManager(AlarmManager alarmManager, Context context, int taskId, Intent alarmIntent){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d("NotificationReceiver", "Task is done. Alarm cancelled.");
        return;
    }

} // NotificationReceiver ends here =======

