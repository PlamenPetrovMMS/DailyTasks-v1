package com.example.dailytasks;

import android.content.Context;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Task {

    public static final int VALID = 0;
    public static final int INVALID = 1;
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

    public Task(){
        dateTimeMap = new HashMap<>();
        this.title = null;
        this.description = null;
        done = false;

        currentTimeMillis = System.currentTimeMillis();
        createdDate = new Date(currentTimeMillis);
        deadlineDate = new Date(currentTimeMillis);

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
        loadDateTimeMap();
    }


    public void setId(long id){
        if(id == Utils.NEXT_ID){
            Utils.NEXT_ID++;
        }
        this.id = id;
    }
    public void setNextId(){
        Utils.NEXT_ID++;
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
                Toast.makeText(context, "Old date", Toast.LENGTH_SHORT).show();
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



}
