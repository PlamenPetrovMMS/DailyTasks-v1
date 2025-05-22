package com.example.dailytasks;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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

public class MainActivity extends AppCompatActivity {

    NotificationReceiver receiver;
    FloatingActionButton createButton;
    RecyclerView recyclerView;
    TaskAdapter adapter;

    ArrayList<Task> taskList = new ArrayList<>();


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
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

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, taskList);
        recyclerView.setAdapter(adapter);

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

            TextView titleLabel, descriptionLabel;
            EditText titleInput, descriptionInput;
            FloatingActionButton addButton, cancelButton;

            titleLabel = dialog.findViewById(R.id.taskPopupTitleLabel);
            titleInput = dialog.findViewById(R.id.taskPopupTitleInputField);

            descriptionLabel = dialog.findViewById(R.id.taskPopupDescriptionLabel);
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

                adapter.scheduleNotification(task);
            });
            cancelButton.setOnClickListener(v -> {
                dialog.dismiss();
            });

            dialog.show();
        });
    } // onCreate ends here ======

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver != null){
            unregisterReceiver(receiver);
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void createNotificationChannel(){

        NotificationReceiver receiver = new NotificationReceiver();
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        IntentFilter filter = new IntentFilter(NotificationReceiver.INTENT);
        registerReceiver(receiver, filter);

        // sendBroadcast(new Intent(NotificationReceiver.INTENT));

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


} // MainActivity ends here ======