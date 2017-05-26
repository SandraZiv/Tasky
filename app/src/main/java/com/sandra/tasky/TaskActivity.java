package com.sandra.tasky;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class TaskActivity extends AppCompatActivity {

    public static final String IS_TASK_NEW = "isTaskNew";
    SimpleTask task;

    TaskDatabase database;

    DateTime dateTime;

    EditText title;
    CheckBox completed;
    EditText note;

    ImageButton imageCancelDate;
    TextView twDate;

    ImageButton imageCancelTime;
    TextView twTime;

    boolean isTaskNew = true;
    boolean isTimeEditable = false;
    boolean isDateChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        if (savedInstanceState != null) {
            isTaskNew = savedInstanceState.getBoolean(IS_TASK_NEW);
            task = (SimpleTask) savedInstanceState.get(SimpleTask.TASK_BUNDLE_KEY);
        } else if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(SimpleTask.TASK_BUNDLE_KEY)) {
            isTaskNew = false;
            task = (SimpleTask) getIntent().getExtras().getSerializable(SimpleTask.TASK_BUNDLE_KEY);
        } else
            task = new SimpleTask();

        //implementation for back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_task_home);
        if (!isTaskNew)
            actionBar.setTitle("Edit task");
        else
            actionBar.setTitle("Create task");

        //open db
        new OpenDBAsyncTask().execute("open");

        title = (EditText) findViewById(R.id.et_title);
        if (!isTaskNew) title.setText(task.getTitle());

        completed = (CheckBox) findViewById(R.id.cb_task);
        completed.setChecked(task.isCompleted());

        note = (EditText) findViewById(R.id.et_note);
        if (!isTaskNew) note.setText(task.getNote());

        twDate = (TextView) findViewById(R.id.task_tw_date);
        imageCancelDate = (ImageButton) findViewById(R.id.img_btn_clear_date);

        twTime = (TextView) findViewById(R.id.task_tw_time);
        imageCancelTime = (ImageButton) findViewById(R.id.img_btn_clear_time);

        dateTime = new DateTime();
        if ((!isTaskNew || savedInstanceState != null) && task.getDueDate() != null){
            dateTime = task.getDueDate();
            if(!task.isTimePresent()){
                dateTime = dateTime.withHourOfDay(new DateTime().getHourOfDay())
                        .withMinuteOfHour(new DateTime().getMinuteOfHour());
            }
        }


        //date section
        //text view date
        if (task.getDueDate() != null) {
            twDate.setText(task.parseDate());
            isTimeEditable = true;
            imageCancelTime.setEnabled(true);
            twTime.setTextColor(Color.BLACK);
        }
        twDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int day = dateTime.getDayOfMonth();
                int month = dateTime.getMonthOfYear() - 1;
                int year = dateTime.getYear();

                final String previousSelected = twDate.getText().toString();

                final DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1; //beacuse in joda it starts from 1
                        dateTime = dateTime.withYear(year).withMonthOfYear(month).withDayOfMonth(dayOfMonth).
                                withHourOfDay(new DateTime().getHourOfDay()).withMinuteOfHour(new DateTime().getMinuteOfHour());
                        twDate.setText(DateTimeFormat.mediumDate().print(dateTime));
                        isDateChanged = true;
                        isTimeEditable = true;
                        imageCancelTime.setEnabled(true);
                        twTime.setTextColor(Color.BLACK);
                    }
                };

                DatePickerDialog dialog = new DatePickerDialog(TaskActivity.this,
                        dateListener, year, month, day);

                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        twDate.setText(previousSelected);
                        if (previousSelected.equals(getString(R.string.select_date))) {
                            isTimeEditable = false;
                            imageCancelTime.setEnabled(false);
                            twTime.setTextColor(Color.GRAY);
                        }

                    }
                });
                dialog.show();
            }
        });

        //cancel button
        imageCancelDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twDate.setText(getString(R.string.select_date));
                twTime.setText(getString(R.string.select_time));
                isTimeEditable = false;
                imageCancelTime.setEnabled(false);
                twTime.setTextColor(Color.GRAY);
                isDateChanged = true;
                dateTime = new DateTime();
            }
        });


        //time section
        //text view
        if (task.getDueDate() != null && task.isTimePresent()) twTime.setText(task.parseTime());
        twTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTimeEditable)
                    return;

                final int hour, min;
                hour = dateTime.getHourOfDay();
                min = dateTime.getMinuteOfHour();

                final String previousTime = twTime.getText().toString();

                TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        dateTime = dateTime.withHourOfDay(hourOfDay).withMinuteOfHour(minute).withSecondOfMinute(0);
                        twTime.setText(DateTimeFormat.shortTime().print(dateTime));
                        isDateChanged = true;
                    }
                };

                TimePickerDialog dialog = new TimePickerDialog(TaskActivity.this,
                        timeListener,
                        hour, min, true);

                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        twTime.setText(previousTime);
                    }
                });
                dialog.show();
            }
        });
        //cancel button
        imageCancelTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twTime.setText(getString(R.string.select_time));
                isDateChanged = true;
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        new OpenDBAsyncTask().execute("Open");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        task.setTitle(title.getText().toString().trim());
        task.setCompleted(completed.isChecked());
        task.setNote(note.getText().toString().trim());
        if (twDate.getText().toString().equals(getString(R.string.select_date))) {
            task.setDueDate(null);
            task.setTimePresent(false);
        } else {
            task.setDueDate(dateTime);
            task.setTimePresent(!twTime.getText().toString().equals(getString(R.string.select_time)));
        }
        outState.putSerializable(SimpleTask.TASK_BUNDLE_KEY, task);
        outState.putBoolean(IS_TASK_NEW, isTaskNew);
    }

    @Override
    public void onBackPressed() {
        saveTask();
        setupForOnBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        //open menu for edit task
        if (!isTaskNew)
            menuInflater.inflate(R.menu.task_edit_menu, menu);
            //open menu for new task
        else
            menuInflater.inflate(R.menu.task_new_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.task_cancel:
                setupForOnBackPressed();
                break;
            case R.id.task_delete:
                database.deleteData(task);
                setupForOnBackPressed();
                break;
            case R.id.task_save:
                if (title.getText().toString().trim().isEmpty()) {
                    Toast.makeText(TaskActivity.this, "Title can't be empty", Toast.LENGTH_SHORT).show();
                    setupForOnBackPressed();
                    break;
                }
                onBackPressed();
                break;
            case R.id.task_confirm:
                if (title.getText().toString().trim().isEmpty()) {
                    Toast.makeText(TaskActivity.this, "Enter title to save task", Toast.LENGTH_SHORT).show();
                    break;
                }
                onBackPressed();
                break;
            default:
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                setupForOnBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupForOnBackPressed() {
        //update widget (important for opening activity from widget)
        AppWidgetManager.getInstance(this).notifyAppWidgetViewDataChanged(
                AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), TaskWidget.class))
                , R.id.widget_list);

        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        TaskActivity.super.onBackPressed();
    }

    private void saveTask() {
        if (!title.getText().toString().trim().isEmpty()) {
            //set title
            task.setTitle(title.getText().toString().trim());
            //set completed
            task.setCompleted(completed.isChecked());
            //set note
            task.setNote(note.getText().toString().trim());
            if (isDateChanged) {
                //set date
                if (!twDate.getText().equals(getString(R.string.select_date))) {
                    if (twTime.getText().toString().equals(getString(R.string.select_time))) {
                        dateTime = dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                        task.setTimePresent(false);
                    } else
                        task.setTimePresent(true);

                    task.setDueDate(dateTime);
                } else
                    task.setDueDate(null);
            }

            if (isTaskNew)
                database.addData(task);
            else
                database.updateData(task);
        }
    }

    private class OpenDBAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            database = new TaskDatabase(TaskActivity.this);
            return null;
        }
    }
}
