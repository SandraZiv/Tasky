package com.sandra.tasky.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.sandra.tasky.R
import com.sandra.tasky.RepeatType
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.db.DatabaseWrapper
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.entity.TaskCategory
import com.sandra.tasky.utils.*
import com.sandra.tasky.widget.TaskWidget
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class TaskActivity : AppCompatActivity() {

    lateinit var task: SimpleTask
    private var dateTime: DateTime? = null

    private var isTaskNew = true
    private var isTimeEditable = false
    private var isDateChanged = false
    private var isTaskVisibilityInWidgetChanged = false

    private var categories: MutableList<TaskCategory> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        JodaTimeAndroid.init(this)

        if (savedInstanceState != null) {
            isTaskNew = savedInstanceState.getBoolean(IS_TASK_NEW)
            task = savedInstanceState[TaskyConstants.TASK_BUNDLE_KEY] as SimpleTask
        } else if (intent.extras != null && intent.extras!!.containsKey(TaskyConstants.TASK_BUNDLE_KEY)) {
            isTaskNew = false
            task = intent.extras!!.getSerializable(TaskyConstants.TASK_BUNDLE_KEY) as SimpleTask
        } else {
            task = SimpleTask()
        }

        //implementation for back button_close
        val actionBar = supportActionBar
        actionBar?.setTitle(if (isTaskNew) R.string.create_task else R.string.edit_task)

        getCategoriesFromDb()

        if (!isTaskNew) {
            etTitle.setText(task.title)
            etTitle.setSelection(task.title.length)
        }
        cbCompleted.isChecked = task.isCompleted
        if (!isTaskNew) {
            etNote.setText(task.note)
        }
        dateTime = DateTime()
        if ((!isTaskNew || savedInstanceState != null) && task.dueDate != null) {
            dateTime = task.dueDate
            if (!task.isTimePresent) {
                dateTime = dateTime!!.withHourOfDay(DateTime().hourOfDay)
                        .withMinuteOfHour(DateTime().minuteOfHour)
            }
        } else if (isTaskNew && intent.extras != null && intent.extras!!.containsKey(EXTRAS_SELECTED_DATETIME_KEY)) {
            dateTime = intent.extras!![EXTRAS_SELECTED_DATETIME_KEY] as DateTime
            dateTime = dateTime!!
                    .withHourOfDay(DateTime().hourOfDay)
                    .withMinuteOfHour(DateTime().minuteOfHour)
            task.dueDate = dateTime
        }

        // date section
        if (task.dueDate != null) {
            tvTaskDate.text = task.parseDate()
            isTimeEditable = true
            btnClearTime.isEnabled = true
            tvTaskTime.setTextColor(Color.BLACK)
        }

        tvTaskDate.setOnClickListener {
            val day = dateTime!!.dayOfMonth
            val month = dateTime!!.monthOfYear - 1
            val year = dateTime!!.year

            DatePickerDialog(
                this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    val isTimePresent = isTimeSelected()
                    val hour = if (isTimePresent) dateTime!!.hourOfDay else DateTime().hourOfDay
                    val min =
                        if (isTimePresent) dateTime!!.minuteOfHour else DateTime().minuteOfHour
                    dateTime = dateTime!!
                        .withYear(selectedYear)
                        .withMonthOfYear(selectedMonth + 1)  // because in joda it starts from 0
                        .withDayOfMonth(selectedDayOfMonth)
                        .withHourOfDay(hour)
                        .withMinuteOfHour(min)
                    tvTaskDate.text = DateTimeFormat.fullDate().print(dateTime)
                    isDateChanged = true
                    isTimeEditable = true
                    btnClearTime.isEnabled = true
                    tvTaskTime.setTextColor(Color.BLACK)
                }, year, month, day
            ).show()
        }

        btnClearDate.setOnClickListener {
            tvTaskDate.text = getString(R.string.select_date)
            tvTaskTime.text = getString(R.string.select_time)
            isTimeEditable = false
            btnClearTime.isEnabled = false
            tvTaskTime.setTextColor(Color.GRAY)
            isDateChanged = true
            dateTime = DateTime()
        }


        // time section
        if (task.dueDate != null && task.isTimePresent) {
            tvTaskTime.text = task.parseTime()
        }
        tvTaskTime.setOnClickListener(View.OnClickListener {
            if (!isTimeEditable) return@OnClickListener
            val hour: Int = dateTime!!.hourOfDay
            val min: Int = dateTime!!.minuteOfHour

            TimePickerDialog(
                this, { _, hourOfDay, minute ->
                    dateTime = dateTime!!.withHourOfDay(hourOfDay).withMinuteOfHour(minute).withSecondOfMinute(0)
                    tvTaskTime.text = DateTimeFormat.shortTime().print(dateTime)
                    isDateChanged = true
                }, hour, min, true
            ).show()
        })

        btnClearTime.setOnClickListener {
            tvTaskTime.text = getString(R.string.select_time)
            dateTime = dateTime!!
                    .withHourOfDay(DateTime().hourOfDay)
                    .withMinuteOfHour(DateTime().minuteOfHour)
            isDateChanged = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        task.title = etTitle.text.toString().trim { it <= ' ' }
        task.isCompleted = cbCompleted.isChecked
        task.note = etNote.text.toString().trim { it <= ' ' }
        if (!isDateSelected()) {
            task.dueDate = null
            task.isTimePresent = false
        } else {
            task.dueDate = dateTime
            task.isTimePresent = isTimeSelected()
        }
        outState.putSerializable(TaskyConstants.TASK_BUNDLE_KEY, task)
        outState.putBoolean(IS_TASK_NEW, isTaskNew)
    }

    override fun onBackPressed() {
        saveTask()
        setupForOnBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        if (!isTaskNew) {
            //open menu for edit task
            menuInflater.inflate(R.menu.task_edit_menu, menu)
        } else {
            //open menu for new task
            menuInflater.inflate(R.menu.task_new_menu, menu)
        }
        //set initial value
        menu.findItem(R.id.task_show).isChecked = task.shouldShowInWidget
        //prevent menu from closing
        menu.findItem(R.id.task_show).setOnMenuItemClickListener { item ->
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
            item.actionView = View(applicationContext)
            item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    return false
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    return false
                }
            })
            false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.task_cancel -> setupForOnBackPressed()
            R.id.task_delete -> {
                CoroutineScope(Dispatchers.Main).launch {
                    DatabaseWrapper.deleteTask(this@TaskActivity, task)
                    setupForOnBackPressed()
                }
            }
            R.id.task_save -> {
                if (etTitle.text.toString().trim { it <= ' ' }.isEmpty()) {
                    ToastWrapper.showShort(this, R.string.empty_title)
                    setupForOnBackPressed()
                }
                onBackPressed()
            }
            R.id.task_confirm -> {
                if (etTitle.text.toString().trim { it <= ' ' }.isEmpty()) {
                    ToastWrapper.showShort(this, R.string.empty_title_confirmed)
                }
                onBackPressed()
            }
            R.id.task_repeat -> openRepeatAlert()
            R.id.task_category -> openCategoryAlert()
            R.id.task_show -> {
                item.isChecked = !item.isChecked
                task.shouldShowInWidget = item.isChecked
                isTaskVisibilityInWidgetChanged = true
            }
            else -> {
                ToastWrapper.showShort(this, R.string.error)
                setupForOnBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupForOnBackPressed() {
        //update widget (important for opening activity from widget)
        AppWidgetManager.getInstance(this).notifyAppWidgetViewDataChanged(
                AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(application, TaskWidget::class.java))
                , R.id.widgetList)
        val returnIntent = Intent()
        setResult(Activity.RESULT_OK, returnIntent)
        super@TaskActivity.onBackPressed()
    }

    private fun saveTask() {
        if (etTitle.text.toString().trim { it <= ' ' }.isNotEmpty()) {
            task.title = etTitle.text.toString().trim { it <= ' ' }
            task.isCompleted = cbCompleted.isChecked
            task.note = etNote.text.toString().trim { it <= ' ' }
            if (isDateChanged) {
                // set date
                if (isDateSelected()) {
                    // date is set
                    if (!isTimeSelected()) {
                        // no time
                        dateTime = resetTime(dateTime)
                        task.isTimePresent = false
                    } else {
                        // with time
                        task.isTimePresent = true
                    }
                    // doesn't matter for task precision
                    dateTime = setupDateTimeForDB(dateTime)
                    task.dueDate = dateTime
                } else {
                    // there is no date thus no time
                    task.dueDate = null
                    task.isTimePresent = false
                }
            }

            // tweak date for repeating
            // add completed ? completed tasks should not be tweaked
            if (isDateSelected() && !task.isCompleted) {
                while (!task.isInFuture && task.isRepeating) {
                    task.dueDate = TimeUtils.moveToNextRepeat(task)
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                val context = this@TaskActivity
                if (isTaskNew) {
                    DatabaseWrapper.addTask(context, task)
                } else {
                    DatabaseWrapper.updateTask(context, task)
                }
            }

            if (task.dueDate != null && task.isInFuture) {
                AlarmUtils.initTaskAlarm(this@TaskActivity, task)
                NotificationUtils.setNotificationReminder(this, task)
            }
        }
    }

    private fun setupDateTimeForDB(dateTime: DateTime?): DateTime {
        return dateTime!!.withSecondOfMinute(0).withMillisOfSecond(0)
    }

    private fun resetTime(dateTime: DateTime?): DateTime {
        return dateTime!!.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0)
    }

    private fun openRepeatAlert() {
        if (!isDateSelected()) {
            ToastWrapper.showShort(this, R.string.date_must_be_selected)
            return
        }

        // TODO reset when date is reset or keep
        // todo crash when selecting repeat type
        val builder = AlertDialog.Builder(this@TaskActivity)
        builder.setTitle(R.string.repeat)
        builder.setSingleChoiceItems(R.array.repeating_options, task.repeat.value) { dialog, which ->
            task.repeat = RepeatType.valueOf(which.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun isDateSelected() = tvTaskDate.text != getString(R.string.select_date)

    private fun isTimeSelected() = tvTaskTime.text != getString(R.string.select_time)

    private fun openCategoryAlert() = AlertDialog.Builder(this@TaskActivity).apply {
        setTitle(R.string.select_category)

        val preselected = getSelectedCategory()
        val categoriesTitle = categories.map { it.title }.toTypedArray()
        setSingleChoiceItems(categoriesTitle, preselected) { dialog, selected ->
            if (selected != getDefaultCategoryIndex()) {
                task.category = categories[selected]
            }
            dialog.dismiss()
        }
        setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        show()
    }

    private fun getSelectedCategory(): Int {
        val selectedCategory = intent.getSerializableExtra(EXTRAS_SELECTED_CATEGORY_KEY)
        return when {
            task.category != null -> categories.indexOf(task.category)
            selectedCategory != null -> categories.indexOf(selectedCategory)
            else -> getDefaultCategoryIndex()
        }
    }

    private fun getDefaultCategoryIndex() = categories.size - 1

    private fun getCategoriesFromDb() {
        CoroutineScope(Dispatchers.Main).launch {
            categories.addAll(DatabaseWrapper.getAllCategories(this@TaskActivity))
            categories.add(TaskCategory.createDefaultCategory(this@TaskActivity, categories.isNotEmpty()))
        }
    }

    companion object {
        const val EXTRAS_TASK_KEY = "EXTRAS_TASK_KEY"
        const val EXTRAS_SELECTED_CATEGORY_KEY = "EXTRAS_SELECTED_CATEGORY_KEY"
        const val EXTRAS_SELECTED_DATETIME_KEY = "EXTRAS_SELECTED_DATETIME_KEY"

        private const val IS_TASK_NEW = "isTaskNew"
    }
}