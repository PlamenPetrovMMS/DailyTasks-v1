package com.example.dailytasks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;
import android.Manifest;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 100;

    NotificationReceiver receiver;
    FloatingActionButton createButton;
    RecyclerView recyclerView;
    TaskAdapter adapter;
    public static TextView completedTasksResult;
    public static Context CONTEXT;
    public static Logger LOGGER;
    public static int completedCount;

    ArrayList<Task> taskList = new ArrayList<>();
    private boolean isPermissionDialogShowing = false;



    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        CONTEXT = getApplicationContext();
        LOGGER = new Logger(this);
        LOGGER.log("", "\n\n\n");
//        LOGGER.clearLogFile();

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE);

            }
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DBHelper dbHelper = new DBHelper(this);
//        dbHelper.deleteDatabase(); // reset database

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, taskList);
        recyclerView.setAdapter(adapter);

        completedTasksResult = findViewById(R.id.completedResult);
        completedCount = dbHelper.getDoneTasksCount();
        completedTasksResult.setText(String.valueOf(completedCount));

        createButton = findViewById(R.id.createNewTaskButton);
        createButton.setStateListAnimator(null);

        loadUnfinishedTasks();

        createButton.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.newtask_popup);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.blue_box);

            Window window = dialog.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            params.y = 100;
            window.setAttributes(params);
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            EditText titleInput, descriptionInput;
            FloatingActionButton addButton, cancelButton;

            titleInput = dialog.findViewById(R.id.taskPopupTitleInputField);
            descriptionInput = dialog.findViewById(R.id.taskPopupDescriptionInputField);

            addButton = dialog.findViewById(R.id.taskPopupAddButton);
            addButton.setStateListAnimator(null);
            cancelButton = dialog.findViewById(R.id.taskPopupCancelButton);
            cancelButton.setStateListAnimator(null);

            addButton.setOnClickListener(v -> {
                Task task = new Task();
                task.setTitle(titleInput.getText().toString().isEmpty() ? "New Task" : titleInput.getText().toString());
                task.setDescription(descriptionInput.getText().toString());
                task.setDone(false);
                task.setSendingState(false);

                task.setId(dbHelper.insertTask(task));

                taskList.add(task);
                adapter.updateDataset(taskList);
                adapter.notifyDataSetChanged();
                dialog.dismiss();

                task.scheduleAlert(this);
            });

            cancelButton.setOnClickListener(v -> {
                dialog.dismiss();
            });

            dialog.show();
        });
    } // onCreate ends here =====

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);

        if(requestCode == REQUEST_CODE){
            if((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                finishAffinity(); // close this and all parent activities
                System.exit(0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver != null){
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        isPermissionDialogShowing = false;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && !isPermissionDialogShowing){

                showPermissionNotGrantedDialog();
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Task Channel";
            String description = "Channel for task notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NotificationReceiver.CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void loadUnfinishedTasks(){
        DBHelper dbHelper = new DBHelper(this);
        for(Task task: dbHelper.getAllTasks()){
            if(!task.getDoneState()){
                taskList.add(task);
            }
        }

    }

    private void showPermissionNotGrantedDialog() {

        isPermissionDialogShowing = true;

        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs notification permission to work. Please allow it from Settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                    System.exit(0);
                })
                .setCancelable(false)
                .show();
    }



} // MainActivity ends here ======