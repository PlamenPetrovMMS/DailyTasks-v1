package com.example.dailytasks;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private final String ITEM_NOTIFICATION_LABEL_TEXT = "Notification: ";
    private final String ITEM_DEADLINE_LABEL_TEXT = "Deadline: ";
    public static final String DEFAULT_HOURS = "1";
    public static final String DEFAULT_MINUTES = "00";
    public static final String DATE_FORMAT = "dd.MM.yyyy";


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

                updateDoneTasksCount();

                Toast.makeText(CONTEXT, "Task Done", Toast.LENGTH_SHORT).show();

                dbHelper.close();
            }
        });
    }
    public void updateDoneTasksCount(){
        DBHelper dbHelper = new DBHelper(CONTEXT);
        MainActivity.completedCount = dbHelper.getDoneTasksCount();
        MainActivity.completedTasksResult.setText(String.valueOf(MainActivity.completedCount));
        dbHelper.close();
    }
    public void openTaskEditDialog(Task task, int position){
        Dialog dialog = new Dialog(CONTEXT);
        dialog.setContentView(R.layout.taskedit_popup);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.blue_box);

        EditText titleInput, descriptionInput, deadlineDate;
        MaterialButton saveButton;
        FloatingActionButton editTaskButton;
        ImageButton cancelButton;

        titleInput = dialog.findViewById(R.id.editTitleInput);
        descriptionInput = dialog.findViewById(R.id.editDescriptionMultiLine);
        deadlineDate = dialog.findViewById(R.id.editDeadlineInput);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            deadlineDate.setShowSoftInputOnFocus(false);
        }

        saveButton = dialog.findViewById(R.id.editSaveButton);
        editTaskButton = dialog.findViewById(R.id.editTaskButton);
        editTaskButton.setStateListAnimator(null);
        cancelButton = dialog.findViewById(R.id.editCloseButton);

        titleInput.setText(task.getTitle());
        descriptionInput.setText(task.getDescription());

        // =========================================================================================

        try {
            SimpleDateFormat formatter = new SimpleDateFormat(Task.DATE_FORMAT, Locale.getDefault());
            String taskDeadlineDate = formatter.format(task.getDeadlineDate());
            if (!taskDeadlineDate.equals(DEFAULT_DEADLINE)) {
                deadlineDate.setText(taskDeadlineDate);
            }else{
                deadlineDate.setText(DEFAULT_DEADLINE);
            }
        }catch (NullPointerException e){
            deadlineDate.setHint(DEFAULT_DEADLINE);
        }

        // =========================================================================================

        saveButton.setOnClickListener(view -> {
            if(titleInput.getText().toString().trim().isEmpty()){
                titleInput.setText(task.getTitle());
            }else{
                task.setTitle(titleInput.getText().toString());
            }

            task.setDescription(descriptionInput.getText().toString());

            if(!deadlineDate.getText().toString().isEmpty() && deadlineDate != null){
                if(task.setDeadlineTime(deadlineDate.getText().toString(), CONTEXT) == Task.INVALID){
                    deadlineDate.setText(DEFAULT_DEADLINE);
                    return;
                }
            }else{
                task.setDeadlineTime(DEFAULT_DEADLINE, CONTEXT);
            }

            task.scheduleAlert(CONTEXT);

            localDataSet.set(position, task);
            notifyDataSetChanged();

            Toast.makeText(CONTEXT, "Task saved", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        editTaskButton.setOnClickListener(view -> {
            Dialog editDialog = new Dialog(CONTEXT);
            editDialog.setContentView(R.layout.edit_task);
            Objects.requireNonNull(editDialog.getWindow()).setBackgroundDrawableResource(R.drawable.darkblue_box);

            EditText hourTextView, minuteTextView;
            RadioGroup radioGroup;
            RadioButton notificationRadioButton, alarmRadioButton;

            hourTextView = editDialog.findViewById(R.id.editTaskHourResult);
            minuteTextView = editDialog.findViewById(R.id.editTaskMinuteResult);

            radioGroup = editDialog.findViewById(R.id.radioGroup);
            notificationRadioButton = editDialog.findViewById(R.id.notificationRadioButton);
            alarmRadioButton = editDialog.findViewById(R.id.alarmRadioButton);

            switch(task.getAlertType()){
                case Task.NOTIFICATION_TAG:
                    notificationRadioButton.setChecked(true);
                    break;
                case Task.ALARM_TAG:
                    alarmRadioButton.setChecked(true);
                    break;
                default:
                    break;
            }

            hourTextView.setText(String.valueOf(task.getNotificationHours()));
            minuteTextView.setText(String.valueOf(task.getNotificationMinutes()));

            hourTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        hourTextView.setText("");
                    }
                }
            });

            minuteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            minuteTextView.setText("");
                        }
                    }
            });

            editDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                    String hourInput = hourTextView.getText().toString().trim();
                    String minuteInput = minuteTextView.getText().toString().trim();

                    int hours, minutes;

                    try{
                        hours = Integer.parseInt(hourInput);
                        minutes = Integer.parseInt(minuteInput);
                    }catch (NumberFormatException e){
                        hours = Integer.parseInt(DEFAULT_HOURS);
                        minutes = Integer.parseInt(DEFAULT_MINUTES);
                    }
                    if(hours < 5){
                        if(minutes > 59){
                            while(minutes > 59){
                                hours++;
                                minutes -= 60;
                            }
                            if(hours >= 5){
                                hours = 5;
                                minutes = 0;
                            }
                        }
                        if(minutes == 0 && hours == 0){
                            minutes = 1;
                            Toast.makeText(CONTEXT, "Minimum 1 minute", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        hours = 5;
                        minutes = 0;
                        Toast.makeText(CONTEXT, "Maximum 5 hours", Toast.LENGTH_SHORT).show();
                    }
                    task.setNotificationHours(hours);
                    task.setNotificationMinutes(minutes);
                    task.scheduleAlert(CONTEXT);

                    hourTextView.setText(String.valueOf(hours));
                    if(hours == 5){
                        Toast.makeText(CONTEXT, "Alarm every 5 hours", Toast.LENGTH_SHORT).show();
                    }else{
                        if(minutes != 0 && hours > 0){
                            Toast.makeText(CONTEXT, String.format("Alarm every %d " + (hours > 1 ? "hours" : "hour") + " %d " + (minutes > 1 ? "minutes" : "minute"), hours, minutes), Toast.LENGTH_SHORT).show();
                        }else if(minutes != 0 && hours == 0){
                            Toast.makeText(CONTEXT, String.format("Alarm every %d " + (minutes > 1 ? "minutes" : "minute"), minutes), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(CONTEXT, String.format("Alarm every %d " + (hours > 1 ? "hours" : "hour"), hours), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    DBHelper dbHelper = new DBHelper(CONTEXT);
                    RadioButton button = editDialog.findViewById(checkedId);

                    task.setAlertType(button.getText().toString());
                    Toast.makeText(CONTEXT, "Alert type: " + task.getAlertType(), Toast.LENGTH_SHORT).show();

                    dbHelper.updateTask(task);
                    dbHelper.close();
                }
            });

            editDialog.show();
        });

        cancelButton.setOnClickListener(view ->{
            dialog.dismiss();
        });

        deadlineDate.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CONTEXT,
                    (v, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                        if(calendar.getTimeInMillis() < System.currentTimeMillis()){
                            deadlineDate.setText(DEFAULT_DEADLINE);
                            Toast.makeText(CONTEXT, "Old date", Toast.LENGTH_SHORT).show();
                        }else{
                            deadlineDate.setText(dateFormat.format(calendar.getTime()));
                        }
                    },
                    year,
                    month,
                    day
                    );

            datePickerDialog.show();
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





}
