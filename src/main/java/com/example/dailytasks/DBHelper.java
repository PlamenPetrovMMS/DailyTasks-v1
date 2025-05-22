package com.example.dailytasks;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Tasks.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "task_table";


    public static final String COLUMN_ID = "task_id";
    public static final String COLUMN_TITLE = "task_title";
    public static final String COLUMN_DONE = "task_isDone";
    public static final String COLUMN_SENDING = "task_isSending";
    public static final String COLUMN_MILLIS = "task_millis";
    public static final String COLUMN_CREATED_DATE = "task_created_date";
    public static final String COLUMN_DEADLINE = "task_deadline";
    public static final String COLUMN_NOTIFICATION_HOURS = "task_notify_hours";
    public static final String COLUMN_NOTIFICATION_MINUTES = "task_notify_mins";


    public static final String COLUMN_DESCRIPTION = "description";

    Context CONTEXT = null;

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        CONTEXT = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TITLE + " TEXT," +
                COLUMN_DESCRIPTION + " TEXT," +
                COLUMN_DONE + " INTEGER," + // boolean column (0 or 1)
                COLUMN_SENDING + " INTEGER," +
                COLUMN_DEADLINE + " TEXT," +
                COLUMN_CREATED_DATE + " TEXT," +
                COLUMN_MILLIS + " LONG," +
                COLUMN_NOTIFICATION_HOURS + " INTEGER," +
                COLUMN_NOTIFICATION_MINUTES + " INTEGER" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertTask(Task task){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        fillTaskValues(values, task);

        return database.insert(TABLE_NAME, null, values);
    }
    public void updateTask(Task task){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        fillTaskValues(values, task);

        database.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(task.getId())});
        database.close();
    }
    public void deleteTask(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        database.close();
    }
    public Cursor getTaskCursor(int id){
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
    @SuppressLint("Range")
    public Task getTask(int id) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        Task task = null;

        if (cursor != null && cursor.moveToFirst()) {
             int taskId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));


            int doneInt = cursor.getColumnIndex(COLUMN_DONE) != -1
                    ? cursor.getInt(cursor.getColumnIndex(COLUMN_DONE))
                    : 0;
            boolean done = doneInt == 1;

            int sendingInt = cursor.getColumnIndex(COLUMN_SENDING) != -1 ?
                    cursor.getInt(cursor.getColumnIndex(COLUMN_SENDING)) :
                    0;
            boolean sending = sendingInt == 1;

            String deadline = cursor.getString(cursor.getColumnIndex(COLUMN_DEADLINE));
            String created = cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_DATE));
            long millis = cursor.getLong(cursor.getColumnIndex(COLUMN_MILLIS));
            int notificationHours = cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATION_HOURS));
            int notificationMinutes = cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATION_MINUTES));

            task = new Task();
            task.setId(taskId);
            task.setTitle(title);
            task.setDescription(description);
            task.setDone(done);
            task.setSendingState(sending);
            task.setDeadlineTime(deadline, CONTEXT);
            task.setCreatedDate(created);
            task.setCurrentTimeMillis(millis);
            task.setNotificationHours(notificationHours);
            task.setNotificationMinutes(notificationMinutes);

            cursor.close();
        }

        return task;
    }
    @SuppressLint("Range")
    public ArrayList<Task> getAllTasks(){
        ArrayList<Task> taskArrayList = new ArrayList<>();

        Cursor cursor = getAllTasksCursor();

        if (cursor != null && cursor.moveToFirst()) {
            do{
                int taskId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));


                int doneInt = cursor.getColumnIndex(COLUMN_DONE) != -1
                        ? cursor.getInt(cursor.getColumnIndex(COLUMN_DONE))
                        : 0;
                boolean done = doneInt == 1;

                int sendingInt = cursor.getColumnIndex(COLUMN_SENDING) != -1 ?
                        cursor.getInt(cursor.getColumnIndex(COLUMN_SENDING)) :
                        0;
                boolean sending = sendingInt == 1;

                String deadline = cursor.getString(cursor.getColumnIndex(COLUMN_DEADLINE));
                String created = cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_DATE));
                long millis = cursor.getLong(cursor.getColumnIndex(COLUMN_MILLIS));
                int notificationHours = cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATION_HOURS));
                int notificationMinutes = cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATION_MINUTES));

                Task task = new Task();
                task.setId(taskId);
                task.setTitle(title);
                task.setDescription(description);
                task.setDone(done);
                task.setSendingState(sending);
                task.setDeadlineTime(deadline, CONTEXT);
                task.setCreatedDate(created);
                task.setCurrentTimeMillis(millis);
                task.setNotificationHours(notificationHours);
                task.setNotificationMinutes(notificationMinutes);

                taskArrayList.add(task);

            }while(cursor.moveToNext());
        }
        cursor.close();
        return taskArrayList;
    }

    public Cursor getAllTasksCursor(){
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC", null);
    }
    @SuppressLint("Range")
    public boolean isTaskDone(long id){
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + COLUMN_DONE + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        boolean done = false;
        if(cursor.moveToFirst()){
            done = cursor.getInt(cursor.getColumnIndex(COLUMN_DONE)) == 1;
        }
        cursor.close();
        return done;
    }

    public void setTaskDone(int id, boolean isDone){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DONE, isDone ? 1 : 0);
        database.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        database.close();
    }

    public void printAllTasks(){
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = getAllTasksCursor();
        while(cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DESCRIPTION));
            System.out.println(
                    "ID: " + id +
                    ", TITLE: " + title +
                    ", DESCRIPTION: " + description
            );
        }
    }

    private void fillTaskValues(ContentValues values, Task task){
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DONE, task.getDoneState());
        values.put(COLUMN_SENDING, task.isSending());
        values.put(COLUMN_DEADLINE, task.getDeadlineTimeString());
        values.put(COLUMN_CREATED_DATE, task.getCreatedDateString());
        values.put(COLUMN_MILLIS, task.getCurrentTimeMillis());
        values.put(COLUMN_NOTIFICATION_HOURS, task.getNotificationHours());
        values.put(COLUMN_NOTIFICATION_MINUTES, task.getNotificationMinutes());
    }
}
