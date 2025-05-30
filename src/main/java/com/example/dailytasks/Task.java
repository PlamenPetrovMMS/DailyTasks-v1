package com.example.dailytasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Task {

    public static final String NOTIFICATION_TAG = "Notification";
    public static final String ALARM_TAG = "Alarm";


    public static final int VALID = 0;
    public static final int INVALID = 1;
    public static int NEXT_ID = 0;
    public static final String DATE_FORMAT = "dd.MM.yyyy";

    long id;
    int notificationHours = Integer.parseInt(TaskAdapter.DEFAULT_HOURS);
    int notificationMinutes = Integer.parseInt(TaskAdapter.DEFAULT_MINUTES);

    String title;
    String description;

    boolean done;
    boolean sending;

    long currentTimeMillis;

    Date createdDate;
    Date deadlineDate; // deadline should also be editable, again, in time format

    Map<String, Integer> dateTimeMap;
    String alertType;

    public Task(){
        dateTimeMap = new HashMap<>();
        this.title = null;
        this.description = null;
        done = false;

        currentTimeMillis = System.currentTimeMillis();
        createdDate = new Date(currentTimeMillis);
        deadlineDate = new Date(currentTimeMillis);

        alertType = NOTIFICATION_TAG;

        loadDateTimeMap();
    }
    public Task(String title, String description){
        dateTimeMap = new HashMap<>();
        this.title = title;
        this.description = description;
        done = false;

        currentTimeMillis = System.currentTimeMillis();
        createdDate = new Date(currentTimeMillis);
        deadlineDate = new Date(currentTimeMillis);

        alertType = NOTIFICATION_TAG;

        loadDateTimeMap();
    }


    public void setId(long id){
        if(id == NEXT_ID){
            NEXT_ID++;
        }
        this.id = id;
    }
    public void setNextId(){
        NEXT_ID++;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public void setDone(boolean state){
        done = state;
    }
    public void setCreatedDate(String createdDate){
        try{
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            this.createdDate = formatter.parse(createdDate);
        }catch (ParseException e){
            e.printStackTrace();
            this.createdDate = new Date(System.currentTimeMillis());
        }
    }
    public int setDeadlineTime(String deadlineTime, Context context){
        try{
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            this.deadlineDate = formatter.parse(deadlineTime);

            Date defaultDate = formatter.parse(TaskAdapter.DEFAULT_DEADLINE);
            if(this.deadlineDate.before(defaultDate)){
                this.deadlineDate = new Date(System.currentTimeMillis());
                return INVALID;
            }
            return VALID;
        }catch (ParseException pe){
            Toast.makeText(context, "Invalid date", Toast.LENGTH_SHORT).show();
            this.deadlineDate = new Date(System.currentTimeMillis());
            return INVALID;
        }
    }
    public void setNotificationHours(int hours){
        notificationHours = hours;
    }
    public void setNotificationMinutes(int minutes){
        int addHours = 0;
        while(minutes > 60){
            addHours++;
            minutes -= 60;
        }
        notificationHours += addHours;
        notificationMinutes = minutes;
    }
    public void setCurrentTimeMillis(long millis){
        currentTimeMillis = millis;
    }
    public void setAlertType(String type){
        alertType = type;
    }




    public long getId(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    public String getDescription(){
        return description;
    }
    public boolean getDoneState(){
        return done;
    }
    public Date getDeadlineDate(){
        return deadlineDate;
    }
    public Date getCreatedDate(){
        return createdDate;
    }
    public long getCurrentTimeMillis(){
        return currentTimeMillis;
    }
    public Map<String, Integer> getDateTimeMap(){
        return dateTimeMap;
    }
    public int getNotificationHours(){
        return notificationHours;
    }
    public int getNotificationMinutes(){
        return notificationMinutes;
    }
    public String getAlertType(){
        return alertType;
    }





    public void loadDateTimeMap(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdDate);
        dateTimeMap.put("Hours", calendar.get(Calendar.HOUR_OF_DAY)); // 0-23
        dateTimeMap.put("Minutes", calendar.get(Calendar.MINUTE));
        dateTimeMap.put("Seconds", calendar.get(Calendar.SECOND));
    }
    public boolean isSending(){
        return sending;
    }
    public void setSendingState(boolean state){
        sending = state;
    }
    public String getDeadlineTimeString(){
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return formatter.format(deadlineDate);
    }
    public String getCreatedDateString(){
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return formatter.format(createdDate);
    }

    @SuppressLint("InvalidWakeLockTag")
    public void scheduleAlert(Context context){
        DBHelper dbHelper = new DBHelper(context);

        if(isSending()) {
            stopAlarmManager(context);
        }
        createNewAlert(context);

        dbHelper.updateTask(this);
        dbHelper.close();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void createNewAlert(Context context){
        int hourCount = getNotificationHours();
        int minuteCount = getNotificationMinutes();

        long hourMillis = TimeUnit.HOURS.toMillis(hourCount);
        long minuteMillis = TimeUnit.MINUTES.toMillis(minuteCount);


        long intervalInMillis = hourMillis + minuteMillis;

        long triggerTime = SystemClock.elapsedRealtime() + intervalInMillis;

        Intent intent = getIntent(context);
        if(intent == null) return;



        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                NotificationReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if(getAlertType().equals(NOTIFICATION_TAG)) NotificationReceiver.REQUEST_CODE++;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager != null) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(settingsIntent);
                return;
            }
        }

        if(alarmManager != null){
            switch(getAlertType()){
                case NOTIFICATION_TAG:
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                    Log.d("Task", "Setting notification for task at " + triggerTime + " (interval: " + intervalInMillis + ")");
                    break;
                case ALARM_TAG:
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                    Log.d("Task", "Setting alarm for task at " + triggerTime + " (interval: " + intervalInMillis + ")");
                    break;
                default:
                    Log.e("Task", "Failed to create alarm manager");
            }
        }
    }
    private void stopAlarmManager(Context context){
        int taskId = Integer.parseInt(String.valueOf(getId()));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = getIntent(context);
        if(intent == null) return;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d("NotificationReceiver", "Task is done. Alarm cancelled.");
    }

    private Intent getIntent(Context context){
        Intent intent;
        switch(getAlertType()){
            case Task.NOTIFICATION_TAG:
                intent = new Intent(context, NotificationReceiver.class);
                break;
            case Task.ALARM_TAG:
                intent = new Intent(context, AlarmReceiver.class);
                break;
            default:
                Log.e("Task", "getIntent() didn't create any intent");
                return null;
        }
        intent.putExtra(DBHelper.COLUMN_ID, String.valueOf(getId()));
        return intent;
    }
}
