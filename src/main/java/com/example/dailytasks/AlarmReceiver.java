package com.example.dailytasks;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

public class AlarmReceiver extends BroadcastReceiver {
    private PowerManager.WakeLock wakeLock;
    private Context CONTEXT;
    private int notificationId = 0;


    public static int REQUEST_CODE = 101;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Alarm intent received");

        CONTEXT = context;
        DBHelper dbHelper = new DBHelper(context);

        int taskId = Integer.parseInt(intent.getExtras().get(DBHelper.COLUMN_ID).toString());
        Task task = dbHelper.getTask(taskId);

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "AlarmReceiver::WakeUpTag" // correct WakeLock tag
        );
        wakeLock.acquire(3000); // 3 seconds hold

        Intent serviceIntent = new Intent(context, AlarmForegroundService.class);
        serviceIntent.putExtra(DBHelper.COLUMN_ID, taskId);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            context.startForegroundService(serviceIntent);
        }else{
            context.startService(serviceIntent);
        }


//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createChannel();
//        }
//
//
//
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent alarmIntent = new Intent(context, AlarmActivity.class);
//
//        // wake up phone

//
//        if(task.getId() != -1 && dbHelper.isTaskDone(taskId)){
//            stopAlarmManager(taskId, alarmIntent, alarmManager);
//            return;
//        }else{
//            createNextAlert(task, alarmManager);
//        }
//
//        // Start AlarmActivity
//        //showFullScreenNotification(context, task);
//
//        context.startActivity(new Intent(context, AlarmActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    } // onReceive ends here =================================

    private void showFullScreenNotification(Context context, Task task){

        Intent intent = new Intent(context, AlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.dt_logo)
                .setContentTitle(task.getTitle())
                .setContentText(task.getDescription())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
        notificationId++;
    }
    @SuppressLint("ScheduleExactAlarm")
    private void createNextAlert(Task task, AlarmManager alarmManager){
        int taskId = (int) task.getId();

        long hourMillis = TimeUnit.HOURS.toMillis(task.getNotificationHours());
        long minuteMillis = TimeUnit.MINUTES.toMillis(task.getNotificationMinutes());

        long intervalInMillis = hourMillis + minuteMillis;

        Intent alarmIntent = new Intent(CONTEXT, AlarmReceiver.class);
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
        Log.d("AlarmReceiver", "Setting alarm for task at " + triggerTime + " (interval: " + intervalInMillis + ")");
    }

    private void stopAlarmManager(int taskId, Intent alarmIntent, AlarmManager alarmManager){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                CONTEXT,
                taskId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d("NotificationReceiver", "Task is done. Alarm cancelled.");
    }

    private void createChannel(){
        NotificationManager notificationManager = (NotificationManager) CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("alarm_channel", "Alarm Channel", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Channel for task alarms");
        notificationManager.createNotificationChannel(channel);
    }
} // AlarmReceiver ends here ===============================
