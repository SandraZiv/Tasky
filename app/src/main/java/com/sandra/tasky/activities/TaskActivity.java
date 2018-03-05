package com.sandra.tasky.activities;

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
import android.support.v7.app.AlertDialog;
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

import com.sandra.tasky.NotificationUtils;
import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.TaskyUtils;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;
import com.sandra.tasky.entity.TaskCategory;
import com.sandra.tasky.widget.TaskWidget;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

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
    boolean isTaskVisibilityInWidgetChanged = false;

    private List<TaskCategory> categories;
    private String[] categoriesTitle;
    private long[] categoriesId;

    //index for above arrays calculated on given categories and selectedCategoryId from intent extras
    private int selectedCategoryIndex = 0;

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        JodaTimeAndroid.init(this);

        if (savedInstanceState != null) {
            isTaskNew = savedInstanceState.getBoolean(IS_TASK_NEW);
            task = (SimpleTask) savedInstanceState.get(TaskyConstants.TASK_BUNDLE_KEY);
        } else if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(TaskyConstants.TASK_BUNDLE_KEY)) {
            isTaskNew = false;
            task = (SimpleTask) getIntent().getExtras().getSerializable(TaskyConstants.TASK_BUNDLE_KEY);
        } else {
            task = new SimpleTask();
        }

        //implementation for back button_close
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(isTaskNew ? R.string.create_task : R.string.edit_task);
        }

        //open db
        new getDataAsyncTask().execute();

        title = findViewById(R.id.et_title);
        if (!isTaskNew) {
            title.setText(task.getTitle());
            title.setSelection(task.getTitle().length());
        }

        completed = findViewById(R.id.cb_task);
        completed.setChecked(task.isCompleted());

        note = findViewById(R.id.et_note);
        if (!isTaskNew) {
            note.setText(task.getNote());
        }

        twDate = findViewById(R.id.task_tw_date);
        imageCancelDate = findViewById(R.id.img_btn_clear_date);

        twTime = findViewById(R.id.task_tw_time);
        imageCancelTime = findViewById(R.id.img_btn_clear_time);

        dateTime = new DateTime();
        if ((!isTaskNew || savedInstanceState != null) && task.getDueDate() != null) {
            dateTime = task.getDueDate();
            if (!task.isTimePresent()) {
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
                        month = month + 1; //beacuse in joda it starts from 0
                        boolean isTimePresent = !twTime.getText().toString().equals(getString(R.string.select_time));
                        int hour = isTimePresent ? dateTime.getHourOfDay() : new DateTime().getHourOfDay();
                        int min = isTimePresent ? dateTime.getMinuteOfHour() : new DateTime().getMinuteOfHour();
                        dateTime = dateTime.withYear(year).withMonthOfYear(month).withDayOfMonth(dayOfMonth).
                                withHourOfDay(hour).withMinuteOfHour(min);
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

        //cancel button_close
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
        //cancel button_close
        imageCancelTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twTime.setText(getString(R.string.select_time));
                dateTime = dateTime
                        .withHourOfDay(new DateTime().getHourOfDay())
                        .withMinuteOfHour(new DateTime().getMinuteOfHour());
                isDateChanged = true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new getDataAsyncTask().execute();
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
        outState.putSerializable(TaskyConstants.TASK_BUNDLE_KEY, task);
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
        if (!isTaskNew) {
            //open menu for edit task
            menuInflater.inflate(R.menu.task_edit_menu, menu);
        } else {
            //open menu for new task
            menuInflater.inflate(R.menu.task_new_menu, menu);
        }
        //set initial value
        menu.findItem(R.id.task_show).setChecked(task.isShowInWidget());
        //prevent menu from closing
        menu.findItem(R.id.task_show).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(getApplicationContext()));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                return false;
            }
        });
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
                database.deleteTask(this, task);
                setupForOnBackPressed();
                break;
            case R.id.task_save:
                if (title.getText().toString().trim().isEmpty()) {
                    mToast = TaskyUtils.addToast(mToast, TaskActivity.this, R.string.empty_title, true);
                    setupForOnBackPressed();
                    break;
                }
                onBackPressed();
                break;
            case R.id.task_confirm:
                if (title.getText().toString().trim().isEmpty()) {
                    mToast = TaskyUtils.addToast(mToast, TaskActivity.this, R.string.empty_title_confirmed, true);
                    break;
                }
                onBackPressed();
                break;
            case R.id.task_category:
                openCategoryAlert();
                break;
            case R.id.task_show:
                item.setChecked(!item.isChecked());
                task.setShowInWidget(item.isChecked());
                isTaskVisibilityInWidgetChanged = true;
                break;
            default:
                mToast = TaskyUtils.addToast(mToast, this, R.string.error, true);
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
            task.setTitle(title.getText().toString().trim());
            task.setCompleted(completed.isChecked());
            task.setNote(note.getText().toString().trim());
            if (isDateChanged) {
                //set date
                if (!twDate.getText().equals(getString(R.string.select_date))) {
                    //date is set
                    if (twTime.getText().toString().equals(getString(R.string.select_time))) {
                        //no time
                        dateTime = resetTime(dateTime);
                        task.setTimePresent(false);
                    } else {
                        //with time
                        task.setTimePresent(true);
                    }
                    //doesn't matter for task precision
                    dateTime = setupDateTimeForDB(dateTime);
                    task.setDueDate(dateTime);
                } else {
                    //there is no date thus no time
                    task.setDueDate(null);
                    task.setTimePresent(false);
                }
            }

            if (isTaskNew) {
                task.setId(database.addTask(task));
            } else {
                database.updateTask(task);
            }

            if (task.isShowInWidget()
                    && (isDateChanged || isTaskVisibilityInWidgetChanged)
                    && task.isTimePresent()
                    && isInFuture(task)) {
                TaskyUtils.setAlarm(this, task.getDueDate().getMillis() + 60 * 1000, task.getTitle(), false);
            }

            if (task.getDueDate() != null && isInFuture(task)) {
                NotificationUtils.setNotificationReminder(this, task);
            }
        }
    }

    private DateTime setupDateTimeForDB(DateTime dateTime) {
        return dateTime.withSecondOfMinute(0).withMillisOfSecond(0);
    }

    private DateTime resetTime(DateTime dateTime) {
        return dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
    }

    private boolean isInFuture(SimpleTask task) {
        return (task.getDueDate().getMillis() + 60 * 1000) > System.currentTimeMillis();
    }

    private void openCategoryAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);

        builder.setTitle(R.string.select_category);

        int preselected = task.getCategory() == null ? selectedCategoryIndex : categories.indexOf(task.getCategory());

        builder.setSingleChoiceItems(categoriesTitle, preselected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                task.setCategory(categories.get(which));
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void setCategoriesPicker(List<TaskCategory> categories) {
        this.categories = categories;
        this.categories.add(new TaskCategory(TaskyConstants.OTHERS_CATEGORY_ID,
                getString(categories.size() == 0 ? R.string.all : R.string.others)));

        setSelectedCategory();

        categoriesTitle = new String[categories.size()];
        categoriesId = new long[categories.size()];

        for (int i = 0; i < categories.size(); i++) {
            categoriesTitle[i] = categories.get(i).getTitle();
            categoriesId[i] = categories.get(i).getId();
        }

        //set category others if task is new and category is different than others
        //need in case user doesn't want to change category manually
        if (isTaskNew && selectedCategoryIndex != categories.size() - 1) {
            long categoryId = categoriesId[selectedCategoryIndex];
            String categoryTitle = categoriesTitle[selectedCategoryIndex];
            task.setCategory(new TaskCategory(categoryId, categoryTitle));
        }
    }

    private void setSelectedCategory() {
        //to handle opening new task activity from widget
        int selectedCategoryId = getIntent().getExtras() == null ?
                (int) TaskyConstants.OTHERS_CATEGORY_ID
                : getIntent().getExtras().getInt(TaskyConstants.SELECTED_CATEGORY_KEY);
        //init
        this.selectedCategoryIndex = categories.size() - 1;
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == selectedCategoryId) {
                this.selectedCategoryIndex = i;
                break;
            }
        }
    }

    private class getDataAsyncTask extends AsyncTask<String, Integer, List<TaskCategory>> {

        @Override
        protected List<TaskCategory> doInBackground(String... params) {
            database = new TaskDatabase(TaskActivity.this);
            return database.getAllCategories();
        }

        @Override
        protected void onPostExecute(List<TaskCategory> categories) {
            setCategoriesPicker(categories);
        }
    }
}
