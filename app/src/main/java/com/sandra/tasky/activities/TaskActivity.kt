package com.sandra.tasky.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.DialogInterface
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
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.entity.TaskCategory
import com.sandra.tasky.utils.*
import com.sandra.tasky.widget.TaskWidget
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.item_task.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var categoriesTitle: Array<String> = emptyArray()

    // index for above arrays calculated on given categories and selectedCategoryId from intent extras
    private var selectedCategoryIndex = 0

    private lateinit var appDatabase: TaskDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        JodaTimeAndroid.init(this)

        appDatabase = TaskDatabase(this)

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
        } else if (isTaskNew && intent.extras != null && intent.extras!!.containsKey(TaskyConstants.TASK_TIME_KEY)) {
            dateTime = intent.extras!![TaskyConstants.TASK_TIME_KEY] as DateTime
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
            val previousSelected = tvTaskDate.text.toString()

            val dateListener = OnDateSetListener { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val isTimePresent = tvTaskTime.text.toString() != getString(R.string.select_time)
                val hour = if (isTimePresent) dateTime!!.hourOfDay else DateTime().hourOfDay
                val min = if (isTimePresent) dateTime!!.minuteOfHour else DateTime().minuteOfHour
                dateTime = dateTime!!
                        .withYear(selectedYear)
                        .withMonthOfYear(selectedMonth + 1)  // beacuse in joda it starts from 0
                        .withDayOfMonth(selectedDayOfMonth)
                        .withHourOfDay(hour)
                        .withMinuteOfHour(min)
                tvTaskDate.text = DateTimeFormat.fullDate().print(dateTime)
                isDateChanged = true
                isTimeEditable = true
                btnClearTime.isEnabled = true
                tvTaskTime.setTextColor(Color.BLACK)
            }

            DatePickerDialog(this@TaskActivity, dateListener, year, month, day).apply {
                setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)) { dialog, _ ->
                    dialog.cancel()
                    tvTaskDate.text = previousSelected
                    if (previousSelected == getString(R.string.select_date)) {
                        isTimeEditable = false
                        btnClearTime.isEnabled = false
                        tvTaskTime.setTextColor(Color.GRAY)
                    }
                }
            }.show()
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
            val previousTime = tvTaskTime.text.toString()
            val timeListener = OnTimeSetListener { _, hourOfDay, minute ->
                dateTime = dateTime!!.withHourOfDay(hourOfDay).withMinuteOfHour(minute).withSecondOfMinute(0)
                tvTaskTime.text = DateTimeFormat.shortTime().print(dateTime)
                isDateChanged = true
            }

            TimePickerDialog(this@TaskActivity, timeListener, hour, min, true)
                    .apply {
                        setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)) { dialog, _ ->
                            dialog.cancel()
                            tvTaskTime.text = previousTime
                        }
                    }.show()
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
        if (tvTaskDate.text.toString() == getString(R.string.select_date)) {
            task.dueDate = null
            task.isTimePresent = false
        } else {
            task.dueDate = dateTime
            task.isTimePresent = tvTaskTime.text.toString() != getString(R.string.select_time)
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
                dbAction { appDatabase.deleteTask(this, task) }
                setupForOnBackPressed()
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
                if (tvTaskDate.text != getString(R.string.select_date)) {
                    // date is set
                    if (tvTaskTime.text.toString() == getString(R.string.select_time)) {
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
            if (tvTaskDate.text != getString(R.string.select_date) && !task.isCompleted) {
                while (!task.isInFuture && task.isRepeating) {
                    task.dueDate = TimeUtils.moveToNextRepeat(task)
                }
            }
            if (isTaskNew) {
//                task.id = database!!.addTask(task)
                // todo we need task id?
                dbAction { appDatabase.addTask(task) }
            } else {
                dbAction { appDatabase.updateTask(task) }
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
        if (tvTaskDate.text.toString() == getString(R.string.select_date)) {
            ToastWrapper.showShort(this, R.string.date_must_be_selected)
            return
        }

        // TODO reset when date is reset or keep
        val builder = AlertDialog.Builder(this@TaskActivity)
        builder.setTitle(R.string.repeat)
        builder.setSingleChoiceItems(R.array.repeating_options, task.repeat.value) { dialog, which ->
            task.repeat = RepeatType.valueOf(which.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }
        builder.show()
    }

    private fun openCategoryAlert() = AlertDialog.Builder(this@TaskActivity).apply {
        setTitle(R.string.select_category)

        val preselected = if (task.category == null) selectedCategoryIndex else categories.indexOf(task.category!!)
        setSingleChoiceItems(categoriesTitle, preselected) { dialog, which ->
            task.category = categories[which]
            dialog.dismiss()
        }
        setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        show()

    }

    private fun setCategoriesPicker() {
        categories.add(
                TaskCategory(title = getString(if (categories.size == 0) R.string.all else R.string.others))
        )
        setSelectedCategory()
        categoriesTitle = emptyArray()
        val categoriesId = IntArray(categories.size)
        for (i in categories.indices) {
            categoriesTitle[i] = categories[i].title
            categoriesId[i] = categories[i].id
        }

        //set category others if task is new and category is different than others
        //need in case user doesn't want to change category manually
        if (isTaskNew && selectedCategoryIndex != categories.size - 1) {
            val categoryId = categoriesId[selectedCategoryIndex]
            val categoryTitle = categoriesTitle[selectedCategoryIndex]
            task.category = TaskCategory(categoryId, categoryTitle)
        }
    }

    private fun setSelectedCategory() {
        //to handle opening new task activity from widget
        val selectedCategoryId =
            intent.extras?.apply { getInt(TaskyConstants.SELECTED_CATEGORY_KEY) }
                ?: TaskCategory.OTHERS_CATEGORY_ID
        //init
        selectedCategoryIndex = categories.size - 1
        for (i in categories.indices) {
            if (categories[i].id == selectedCategoryId) {
                selectedCategoryIndex = i
                break
            }
        }
    }

    // DB related
    private fun getCategoriesFromDb() {
        CoroutineScope(Dispatchers.IO).launch {
            categories.addAll(appDatabase.allCategories)
            withContext(Dispatchers.Main) {
                setCategoriesPicker()
            }
        }
    }

    private fun dbAction(action: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            action()
        }
    }

    companion object {
        const val IS_TASK_NEW = "isTaskNew"
    }
}