package com.example.dailytasks;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AlarmForegroundService extends Service {

    public static final String CHANNEL_ID = "alarm_foreground_service_id";
    public static final int NOTIFICATION_ID = 111;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AlarmForegroundService", "Service started");

        int taskId = Integer.parseInt(intent.getExtras().get(DBHelper.COLUMN_ID).toString());

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        Task task = dbHelper.getTask(taskId);

        String title = task.getTitle();
        String description = task.getDescription();

        Intent alarmActivityIntent = new Intent(this, AlarmActivity.class);
        alarmActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(alarmActivityIntent);

//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                this,
//                0,
//                alarmActivityIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.dt_logo)
//                .setContentTitle(title)
//                .setContentText(description)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setCategory(NotificationCompat.CATEGORY_ALARM)
//                .setFullScreenIntent(pendingIntent, true)
//                .setAutoCancel(true)
//                .build();
//
//        Log.d("AlarmForegroundService", "Notification was build");
//
//        startForeground(NOTIFICATION_ID, notification);
//
//        Log.d("AlarmForegroundService", "Foreground started");

        return START_NOT_STICKY;
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription("Channel for alarm foreground service");
            serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if(manager != null){
                manager.createNotificationChannel(serviceChannel);
            }
            Log.d("AlarmForegroundService", "Channel created");
        }
    }
}
