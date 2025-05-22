package com.example.dailytasks;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private final String ITEM_NOTIFICATION_LABEL_TEXT = "Notification: ";
    private final String ITEM_DEADLINE_LABEL_TEXT = "Deadline: ";
    public static final String DEFAULT_HOURS = "1";
    public static final String DEFAULT_MINUTES = "00";


    static SimpleDateFormat formatter = new SimpleDateFormat(Task.DATE_FORMAT, Locale.getDefault());
    public static final String DEFAULT_DEADLINE = formatter.format(new Date(System.currentTimeMillis()));


    private ArrayList<Task> localDataSet;
    private Context CONTEXT;

    public TaskAdapter(Context context, ArrayList<Task> data){
        localDataSet = data;
        CONTEXT = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView itemTitleLabel, itemNotificationLabel, itemNotificationResult, itemDeadlineLabel, itemDeadlineResult;
        FloatingActionButton editButton, finishButton;
        RelativeLayout relativeLayout, itemLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.itemRelativeLayout);
            itemLayout = (RelativeLayout) itemView.findViewById(R.id.itemLayout);

            itemTitleLabel = (TextView) itemView.findViewById(R.id.itemTaskTitleLabel);

            itemNotificationLabel = (TextView) itemView.findViewById(R.id.itemNotificationLabel);
            itemNotificationResult = (TextView) itemView.findViewById(R.id.itemNotificationResult);

            itemDeadlineLabel = (TextView) itemView.findViewById(R.id.itemDeadlineLabel);
            itemDeadlineResult = (TextView) itemView.findViewById(R.id.itemDeadlineResult);

            //editButton = (FloatingActionButton) itemView.findViewById(R.id.itemEditTaskButton);
            //editButton.setStateListAnimator(null);

            finishButton = (FloatingActionButton) itemView.findViewById(R.id.itemFinishTaskButton);
            finishButton.setStateListAnimator(null);

        }
        public View getItemView(){
            return itemView;
        }
        public TextView getItemTitleLabelTextView(){
            return itemTitleLabel;
        }
        public TextView getItemNotificationLabelTextView(){
            return itemNotificationLabel;
        }
        public TextView getItemNotificationResult(){return itemNotificationResult;}
        public TextView getItemDeadlineResult(){return itemDeadlineResult;}
        public TextView getItemDeadlineLabelTextView(){
            return itemDeadlineLabel;
        }

        public FloatingActionButton getEditButton(){
            return editButton;
        }
        public FloatingActionButton getFinishButton(){
            return finishButton;
        }
        public RelativeLayout getRelativeLayout(){
            return relativeLayout;
        }
        public RelativeLayout getItemLayout(){
            return itemLayout;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged", "RecyclerView"})
    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.ViewHolder holder, int position) {
        Task task = localDataSet.get(position);

        if(task.getDoneState()){
            return;
        }

        holder.getItemView().setAlpha(1f);
        holder.getItemTitleLabelTextView().setText(task.getTitle());

        holder.getItemNotificationResult().setText(String.format(
                "%d " + (task.getNotificationHours() > 1 ? "hours" : "hour") + "  %d min",
                task.getNotificationHours(),
                task.getNotificationMinutes()));

        try{
            SimpleDateFormat formatter = new SimpleDateFormat(Task.DATE_FORMAT, Locale.getDefault());
            String stringDate = formatter.format(task.getDeadlineDate());
            holder.getItemDeadlineResult().setText(stringDate);
        }catch (NullPointerException e){
            holder.getItemDeadlineResult().setText(DEFAULT_DEADLINE);
        }

        holder.getItemLayout().setOnClickListener(view -> {
            openTaskEditDialog(task, position);
        });

//        holder.getEditButton().setOnClickListener(v ->{
//            openTaskEditDialog(task, position);
//        });

        holder.getFinishButton().setOnClickListener(view -> {
            if (position >= 0 && position < localDataSet.size()) {

                task.setSendingState(false);
                task.setDone(true);

                localDataSet.remove(position);

                // Notify the adapter that the item was removed
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, localDataSet.size()); // This ensures other items are properly re-positioned

                DBHelper dbHelper = new DBHelper(CONTEXT);
                dbHelper.updateTask(task);

                Toast.makeText(CONTEXT, "Task Done", Toast.LENGTH_SHORT).show();

                dbHelper.close();
            }
        });
    }
    public void openTaskEditDialog(Task task, int position){

        Dialog dialog = new Dialog(CONTEXT);
        dialog.setContentView(R.layout.taskedit_popup);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.blue_box);

        TextView titleTextView, descriptionTextView, sendAlertTextView, deadlineTextView;
        EditText titleInput, descriptionInput, sendAlertHours, sendAlertMinutes, deadlineDate;
        MaterialButton saveButton, applyTextButton, applyTimeButton;
        ImageButton cancelButton;

        titleInput = dialog.findViewById(R.id.editTitleInput);

        descriptionInput = dialog.findViewById(R.id.editDescriptionMultiLine);

        sendAlertHours = dialog.findViewById(R.id.editHourInput);
        sendAlertMinutes = dialog.findViewById(R.id.editMinuteInput);
        deadlineDate = dialog.findViewById(R.id.editDeadlineInput);

        saveButton = dialog.findViewById(R.id.editSaveAllButton);
        applyTextButton = dialog.findViewById(R.id.editApplyTextButton);
        applyTimeButton = dialog.findViewById(R.id.editApplyTimeButton);
        cancelButton = dialog.findViewById(R.id.editCloseButton);

        titleInput.setText(task.getTitle());
        descriptionInput.setText(task.getDescription());

        // =========================================================================================

        if(task.getNotificationHours() != Integer.parseInt(DEFAULT_HOURS)){
            sendAlertHours.setText(String.valueOf(task.getNotificationHours()));
        } else{
            sendAlertHours.setHint(DEFAULT_HOURS);
        }

        if( task.getNotificationMinutes() != Integer.parseInt(DEFAULT_MINUTES)) {
            sendAlertMinutes.setText(String.valueOf(task.getNotificationMinutes()));
        }else{
            sendAlertMinutes.setHint(DEFAULT_MINUTES);
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat(Task.DATE_FORMAT, Locale.getDefault());
            String taskDeadlineDate = formatter.format(task.getDeadlineDate());
            if (!taskDeadlineDate.equals(DEFAULT_DEADLINE)) {
                deadlineDate.setText(taskDeadlineDate);
            }else{
                deadlineDate.setHint(DEFAULT_DEADLINE);
            }
        }catch (NullPointerException e){
            deadlineDate.setHint(DEFAULT_DEADLINE);
        }

        // =========================================================================================

        saveButton.setOnClickListener(view -> {
            task.setTitle(titleInput.getText().toString());
            task.setDescription(descriptionInput.getText().toString());
            task.setNotificationHours(!sendAlertHours.getText().toString().isEmpty() ? Integer.parseInt(sendAlertHours.getText().toString()) : Integer.parseInt(TaskAdapter.DEFAULT_HOURS));
            task.setNotificationMinutes(!sendAlertMinutes.getText().toString().isEmpty() ? Integer.parseInt(sendAlertMinutes.getText().toString()) : Integer.parseInt(TaskAdapter.DEFAULT_MINUTES));

            if(!deadlineDate.getText().toString().isEmpty() && deadlineDate != null){
                if(task.setDeadlineTime(deadlineDate.getText().toString(), CONTEXT) == Task.INVALID){
                    deadlineDate.setHint(DEFAULT_DEADLINE);
                    return;
                }
            }else{
                task.setDeadlineTime(DEFAULT_DEADLINE, CONTEXT);
            }

            scheduleNotification(task);

            localDataSet.set(position, task);
            notifyDataSetChanged();

            Toast.makeText(CONTEXT, "Task saved", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        applyTextButton.setOnClickListener(view -> {
            DBHelper dbHelper = new DBHelper(CONTEXT);
            task.setTitle(titleInput.getText().toString());
            task.setDescription(descriptionInput.getText().toString());

            localDataSet.set(position, task);
            notifyDataSetChanged();

            dbHelper.updateTask(task);
            dbHelper.close();

            Toast.makeText(CONTEXT, "Text applied", Toast.LENGTH_SHORT).show();
        });

        applyTimeButton.setOnClickListener(view -> {
            DBHelper dbHelper = new DBHelper(CONTEXT);

            task.setNotificationHours(!sendAlertHours.getText().toString().isEmpty() ? Integer.parseInt(sendAlertHours.getText().toString()) : Integer.parseInt(TaskAdapter.DEFAULT_HOURS));
            task.setNotificationMinutes(!sendAlertMinutes.getText().toString().isEmpty() ? Integer.parseInt(sendAlertMinutes.getText().toString()) : Integer.parseInt(TaskAdapter.DEFAULT_MINUTES));

            if(!deadlineDate.getText().toString().isEmpty() && deadlineDate != null){
                if(task.setDeadlineTime(deadlineDate.getText().toString(), CONTEXT) == Task.INVALID){
                    deadlineDate.setHint(DEFAULT_DEADLINE);
                    return;
                }
            }else{
                task.setDeadlineTime(DEFAULT_DEADLINE, CONTEXT);
            }

            scheduleNotification(task);

            localDataSet.set(position, task);
            notifyDataSetChanged();

            dbHelper.updateTask(task);
            dbHelper.close();

            Toast.makeText(CONTEXT, "Time applied", Toast.LENGTH_SHORT).show();
        });

        cancelButton.setOnClickListener(view ->{
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void updateDataset(ArrayList<Task> newData){
        localDataSet = newData;
    }

    @SuppressLint({"ScheduleExactAlarm", "ShortAlarm"})
    public void scheduleNotification(Task task){
        DBHelper dbHelper = new DBHelper(CONTEXT);
        if(task.isSending()){
            stopCurrentTaskAlarm(task);
            createNewTaskAlarm(task);
            Log.d("TaskAdapter", "Task alarm was edited");
        }else{
            Log.d("TaskAdapter", "Creating first alarm");
            createNewTaskAlarm(task);
        }

        task.setSendingState(true);
        dbHelper.updateTask(task);
        dbHelper.close();
    }
    @SuppressLint("ScheduleExactAlarm")
    private void createNewTaskAlarm(Task task){
        int hourCount = task.getNotificationHours();
        int minuteCount = task.getNotificationMinutes();

        long hourMillis = TimeUnit.HOURS.toMillis(hourCount);
        long minuteMillis = TimeUnit.MINUTES.toMillis(minuteCount);

        long intervalInMillis = hourMillis + minuteMillis;

         long triggerTime = SystemClock.elapsedRealtime() + intervalInMillis;

        Intent intent = getAlarmIntent(task);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                CONTEXT,
                NotificationReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationReceiver.REQUEST_CODE++;

        AlarmManager alarmManager = (AlarmManager) CONTEXT.getSystemService(Context.ALARM_SERVICE);

        if(alarmManager != null){
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }

        Log.d("TaskAdapter", "Setting alarm for task at " + triggerTime + " (interval: " + intervalInMillis + ")");
    }
    private void stopCurrentTaskAlarm(Task task){
        int taskId = Integer.parseInt(String.valueOf(task.getId()));

        AlarmManager alarmManager = (AlarmManager) CONTEXT.getSystemService(Context.ALARM_SERVICE);

        Intent intent = getAlarmIntent(task);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                CONTEXT,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d("NotificationReceiver", "Task is done. Alarm cancelled.");
    }
    private Intent getAlarmIntent(Task task){
        Intent intent = new Intent(CONTEXT, NotificationReceiver.class);
        intent.putExtra("task_id", String.valueOf(task.getId()));
        return intent;
    }
}
