package com.example.dailytasks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {
    public static final String FILE_NAME = "dailyTasksLog.txt";
    private final File logFile;
    private Context context;

    public  Logger(Context context){
        this.context = context;
        File docsDir = new File(context.getFilesDir(), "log");
        if(docsDir == null){
            throw new RuntimeException("Unable to access documents directory");
        }

        logFile = new File(docsDir, FILE_NAME);
        if(!logFile.exists()){
            try{
                if(!logFile.createNewFile()){
                    Log.e("Logger", "Failed to create log file");
                }
            }catch (Exception e){
                Log.e("Exception", "Exception while creating the file");
            }
        }

    }

    public void log(String message){
        // The file name and relative path
        String relativePath = "Documents/";

        Uri queryUri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.MediaColumns.DISPLAY_NAME + " = ? AND " +
                MediaStore.MediaColumns.RELATIVE_PATH + " = ?";
        String[] selectionArgs = new String[]{FILE_NAME, relativePath};

        Uri fileUri = null;

        // Check if file already exists
        try (Cursor cursor = context.getContentResolver().query(queryUri, null, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // File exists, get its URI
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                fileUri = ContentUris.withAppendedId(queryUri, id);

                // Open the file in append mode ("wa")
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri, "wa")) {
                    if (outputStream != null) {
                        outputStream.write(message.getBytes());
                        outputStream.flush();
                        System.out.println("Appended to existing file.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        // If file doesn't exist, create a new one
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri uri = context.getContentResolver().insert(queryUri, values);
            if (uri != null) {
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        outputStream.write(message.getBytes());
                        outputStream.flush();
                        System.out.println("Created new file and wrote message.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public File getLogFile(){
        return logFile;
    }



}
