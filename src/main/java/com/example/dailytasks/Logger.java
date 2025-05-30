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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {
    public static final String FILE_NAME = "dailyTasksLog.txt";
    private final Context context;


    // ========== URIs ==========
    private Uri fileUri;
    private Uri queryUri;


    // ========== SELECTION ==========
    String selection;
    String[] selectionArgs;

    // ========== RELATIVE PATH ==========
    String relativePath;


    // ========== CONSTRUCTOR ===========
    public  Logger(Context context){
        this.context = context;
        loadFileUri();
    }

    // ========== LOG ==========
    public void log(String sender, String message){
        try {
            if (fileUri != null) {
                // Append to existing file
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri, "wa")) {
                    if (outputStream != null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(sender).append("\t").append(message).append("\n");
                        outputStream.write(stringBuilder.toString().getBytes());
                        outputStream.flush();
                        Log.d("Logger", "Appended to log file.");
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Create new file
                createNewLogFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Logger", "Error writing to log file");
        }
    }

    private void loadFileUri(){

        relativePath = "Documents/";
        selection = MediaStore.MediaColumns.DISPLAY_NAME + " = ? AND " + MediaStore.MediaColumns.RELATIVE_PATH + " = ?";
        selectionArgs = new String[]{FILE_NAME, relativePath};
        queryUri = MediaStore.Files.getContentUri("external");
        fileUri = null;



        try (Cursor cursor = context.getContentResolver().query(queryUri, null, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {

                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                fileUri = ContentUris.withAppendedId(queryUri, id);
                Log.d("Logger", "File Uri was found");

            }else {

                Log.e("Logger", "File not found with name: " + FILE_NAME + " in path: " + relativePath);
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

                    fileUri = context.getContentResolver().insert(queryUri, values);

                    if(fileUri == null){
                        Log.e("Logger", "Error creating File Uri");
                    }else{
                        Log.d("Logger", "File Uri was created");
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void clearLogFile() {

        if (fileUri == null) {
            Log.e("Logger", "File URI is null");
            return;
        }

        try (OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri, "rwt")) {
            if (outputStream != null) {
                outputStream.write("".getBytes());
                outputStream.flush();
                Log.d("Logger", "Log file was reset");
            } else {
                Log.e("Logger", "OutputStream is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNewLogFile(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);

        Uri newUri = context.getContentResolver().insert(queryUri, values);
        if (newUri != null) {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(newUri)) {
                if (outputStream != null) {
                    outputStream.write("".getBytes());
                    outputStream.flush();
                    Log.d("Logger", "Created and wrote to log file.");
                }
            }catch (IOException e) {
                e.printStackTrace();
                Log.e("Logger", "Creating new log file failed");
            }
        }
    }
}
