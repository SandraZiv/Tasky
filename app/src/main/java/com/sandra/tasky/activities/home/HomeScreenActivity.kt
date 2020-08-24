package com.sandra.tasky.activities.home

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.DataSetObserver
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.sandra.tasky.R
import com.sandra.tasky.SortType
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.activities.categories.CategoriesActivity
import com.sandra.tasky.activities.TaskActivity
import com.sandra.tasky.adapter.CalendarEventAdapter
import com.sandra.tasky.db.AppDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.entity.TaskCategory
import com.sandra.tasky.settings.SettingsActivity
import com.sandra.tasky.utils.TaskyUtils
import com.sandra.tasky.utils.TimeUtils
import com.sandra.tasky.utils.ToastWrapper
import kotlinx.android.synthetic.main.content_home_screen.*
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import kotlinx.android.synthetic.main.fragment_tasks.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class HomeScreenActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnDayClickListener {

    // all tasks from db that later get filtered etc
    private var tasks: List<SimpleTask> = emptyList()
    // task currently visible depending on query, category etc
    private var current: List<SimpleTask> = emptyList()

    private var categories: List<TaskCategory> = emptyList()
    private var categoriesCount: Map<Int, Int> = emptyMap()  // todo kolko ima koje vrste taskova
    private var selectedCategoryId = TaskCategory.ALL_CATEGORY_ID.toInt()

    private var observer: TasksDataObserver? = null

    private var homeListAdapter: HomeListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        setSupportActionBar(toolbar) // todo how this toolbar is set

        JodaTimeAndroid.init(this)

        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        fabAddTask.setOnClickListener { createNewTask() }

        handleIntent(intent)

        //added due to bug fix 28.6
//        setUpFragment();
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        setUpFragment()
        //old
//        try {
//            setUpFragment();
//            new getDataAsyncTask().execute();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    override fun onPause() {
        TaskyUtils.updateWidget(this)
        super.onPause()
    }

    override fun onDestroy() {
        TaskyUtils.updateWidget(this)
        super.onDestroy()
        homeListAdapter!!.unregisterDataSetObserver(observer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_screen_menu, menu)
        val searchView = menu.findItem(R.id.home_menu_search).actionView as SearchView
        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val componentName = componentName
        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                search(newText)
                return true
            }
        })
        menu.findItem(R.id.home_menu_search).setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                hideItems(menu)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                showItems(menu)
                return true
            }
        })
        return true
    }

    private fun hideItems(menu: Menu) {
        menu.setGroupVisible(R.id.menu_group_hidden, false)
        fabAddTask.visibility = View.GONE
    }

    private fun showItems(menu: Menu) {
        menu.setGroupVisible(R.id.menu_group_hidden, true)
        fabAddTask.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home_menu_search -> true
            R.id.home_menu_sort -> {
                sortBy()
                true
            }
            R.id.home_menu_delete_all -> {
                deleteTasks()
                true
            }
            else -> {
                ToastWrapper.showShort( this, R.string.error)
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            search(query)
        }
    }

    private fun search(query: String) {
        updateListView(query)
    }

    private fun sortBy() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.sort_by)
        val preferences = getSharedPreferences(TaskyConstants.PREF_GENERAL, Context.MODE_PRIVATE)
        val selectedSortOption = preferences.getInt(TaskyConstants.PREF_SORT, SortType.getDefault().value)
        val sortOptions = arrayOf(getString(R.string.due_date), getString(R.string.title), getString(R.string.completed))
        builder.setSingleChoiceItems(sortOptions, selectedSortOption) { dialog, which ->
            preferences.edit()
                    .putInt(TaskyConstants.PREF_SORT, which)
                    .apply()
            updateListView(null)
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun deleteTasks() {
        val builder = AlertDialog.Builder(this@HomeScreenActivity)
        builder.setTitle(getString(R.string.delete))
        val filteredTasks = sortAndFilterTasks(null)
        if (filteredTasks.isEmpty()) {
            builder.setMessage(R.string.nothing_to_delete)
            builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        } else {
            builder.setMessage(resources.getQuantityString(R.plurals.delete_alert, filteredTasks.size, filteredTasks.size))
            builder.setPositiveButton(R.string.ok) { _, _ ->
                if (selectedCategoryId == TaskCategory.ALL_CATEGORY_ID) {
                    // todo async
//                    database!!.deleteAllTasks(this@HomeScreenActivity)
                } else {
                    val ids = IntArray(filteredTasks.size)
                    for (i in filteredTasks.indices) {
                        ids[i] = filteredTasks[i].id
                    }
                    // todo async
//                    database!!.deleteAllTasksInCategory(this@HomeScreenActivity, ids)
                }
                loadData()
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        }
        builder.show()
    }

    private fun createNewTask() {
        val newTaskIntent = Intent(this, TaskActivity::class.java)
        newTaskIntent.putExtra(TaskyConstants.SELECTED_CATEGORY_KEY, selectedCategoryId)
        startActivityForResult(newTaskIntent, REQUEST_CODE)
    }

    private fun createNewTask(dateTime: DateTime) {
        val newTaskIntent = Intent(this, TaskActivity::class.java)
        newTaskIntent.putExtra(TaskyConstants.SELECTED_CATEGORY_KEY, selectedCategoryId)
        newTaskIntent.putExtra(TaskyConstants.TASK_TIME_KEY, dateTime)
        startActivityForResult(newTaskIntent, REQUEST_CODE)
    }

    private fun updateListView(query: String?) {
        val list: List<SimpleTask> = sortAndFilterTasks(query)
        current = ArrayList(list)
        observer = TasksDataObserver()
        homeListAdapter = HomeListAdapter(this@HomeScreenActivity, list)
        homeListAdapter!!.registerDataSetObserver(observer)
        lvHome.adapter = homeListAdapter
        lvHome.emptyView = emptyViewHome
        lvHome.onItemClickListener = OnItemClickListener { _, _, position, _ -> openTaskActivity(list[position]) }
        lvHome.onItemLongClickListener = OnItemLongClickListener { _, _, position, _ ->
            val builder = AlertDialog.Builder(this@HomeScreenActivity)
            builder.setTitle(list[position].title)
            val optionList = ArrayAdapter<String>(this@HomeScreenActivity, android.R.layout.simple_list_item_1)
            optionList.add(getString(R.string.delete))
            builder.setAdapter(optionList) { dialog, _ ->
                ToastWrapper.showShort(this, R.string.task_deleted)
                // todo async
//                database!!.deleteTask(this@HomeScreenActivity, list[position])
                loadData()
                dialog.cancel()
            }
            builder.show()
            true
        }
        val sortByCompleted: List<SimpleTask> = ArrayList(list)
        Collections.sort(sortByCompleted, Comparator { o1, o2 -> //first completed then other
            if (o1.isCompleted && o2.isCompleted) {
                return@Comparator 0
            }
            if (o1.isCompleted) 1 else -1
        })

        //completed tasks go first, so that if there were more tasks on same day
        //uncompleted icon will be shown
        val events: MutableList<EventDay?> = ArrayList()
        for (task in sortByCompleted) {
            if (task.dueDate != null) {
                events.add(task.asEventDay())
            }
        }
        calendarView.setEvents(events)
        TaskyUtils.updateWidget(this)
    }

    private fun setUpFragment() {
        val adapter = SwipeAdapter(supportFragmentManager, this)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    fun initTasksList(view: View) {
        // deleted in old file
        try {
            loadData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initCalendarList(view: View) {
        view.calendarView.setOnDayClickListener(this)
        try {
            view.calendarView.setDate(Calendar.getInstance())
        } catch (e: OutOfDateRangeException) {
            e.printStackTrace()
        }
    }

    override fun onDayClick(eventDay: EventDay) {
        val builder = AlertDialog.Builder(this@HomeScreenActivity)
        val day = DateTime(eventDay.calendar.timeInMillis)
        val dayFormatted = DateTimeFormat.fullDate().print(day)
        builder.setTitle(dayFormatted.capitalize())
        val selectedDayTasks: MutableList<SimpleTask> = ArrayList()
        for (task in current) {
            if (task.dueDate != null && TimeUtils.dateEqual(day, task.dueDate)) {
                selectedDayTasks.add(task)
            }
        }
        if (selectedDayTasks.isNotEmpty()) {
            val calendarEventAdapter = CalendarEventAdapter(this@HomeScreenActivity, selectedDayTasks)
            calendarEventAdapter.registerDataSetObserver(observer)
            builder.setAdapter(calendarEventAdapter) { dialog, which ->
                openTaskActivity(selectedDayTasks[which])
                dialog.dismiss()
            }
            builder.setOnDismissListener {
                try {
                    calendarEventAdapter.unregisterDataSetObserver(observer)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            builder.setMessage(R.string.no_tasks_here)
        }
        builder.setPositiveButton(R.string.add_task) { _, _ -> createNewTask(day) }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun setActionBar(title: CharSequence) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = title
        }
    }

    private fun openTaskActivity(task: SimpleTask) {
        val openTaskIntent = Intent(this@HomeScreenActivity, TaskActivity::class.java)
        openTaskIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, task)
        openTaskIntent.putExtra(TaskyConstants.SELECTED_CATEGORY_KEY, selectedCategoryId)
        startActivityForResult(openTaskIntent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                loadData()
                invalidateOptionsMenu()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_manage -> startActivityForResult(Intent(this@HomeScreenActivity, CategoriesActivity::class.java), REQUEST_CODE)
            R.id.nav_settings -> startActivity(Intent(this@HomeScreenActivity, SettingsActivity::class.java))
            else -> openCategory(item.itemId, item.title)
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun openCategory(categoryId: Int, categoryTitle: CharSequence) {
        setActionBar(categoryTitle)
        selectedCategoryId = categoryId
        updateListView(null)
    }

    private fun updateCategoriesList() {
        navView.menu.removeGroup(R.id.menu_group_top)
        navView.menu.add(R.id.menu_group_top, TaskCategory.ALL_CATEGORY_ID.toInt(), 1,
                getString(R.string.all_num, tasks.size))
        var categorySize = 0
        for (category in categories) {
            navView.menu.add(R.id.menu_group_top, category.id, 2,
                    category.title + " (" + categoriesCount[category.id] + ")")
            categorySize += categoriesCount[category.id] ?: error("")
        }

        //add others if there is at least one category
        if (categories.isNotEmpty()) {
            navView.menu.add(R.id.menu_group_top, TaskCategory.OTHERS_CATEGORY_ID.toInt(),
                    categories.size + 1, getString(R.string.others_num, tasks.size - categorySize))
        }
    }

    private fun sortAndFilterTasks(query: String?): List<SimpleTask> {
        val filteredTasks: MutableList<SimpleTask>

        //filter by category
        if (selectedCategoryId == TaskCategory.ALL_CATEGORY_ID) {
            filteredTasks = LinkedList(tasks)
        } else {
            filteredTasks = LinkedList()
            for (task in tasks) {
                //get others or selected category
                if (task.category == null && selectedCategoryId == TaskCategory.OTHERS_CATEGORY_ID
                        || task.category != null && task.category!!.id == selectedCategoryId) {
                    filteredTasks.add(task)
                }
            }
        }
        val queryTasks: MutableList<SimpleTask>
        if (query != null) {
            queryTasks = LinkedList()
            for (task in filteredTasks) {
                if (task.title.toLowerCase().contains(query.toLowerCase().trim { it <= ' ' })) {
                    queryTasks.add(task)
                }
            }
        } else {
            queryTasks = LinkedList(filteredTasks)
        }

        //sort
        queryTasks.sortWith(Comparator { o1, o2 ->
            when (getSharedPreferences(TaskyConstants.PREF_GENERAL, Context.MODE_PRIVATE).getInt(TaskyConstants.PREF_SORT, SortType.getDefault().value)) {
                SortType.SORT_DUE_DATE.value -> 0
                SortType.SORT_TITLE.value -> o1!!.title.toLowerCase().compareTo(o2!!.title.toLowerCase())
                SortType.SORT_COMPLETED.value -> if (o1!!.isCompleted) 1 else -1
                else -> 0
            }
        })
        return queryTasks
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.buildDatabase(this@HomeScreenActivity)
            tasks = database.taskDao().getAll()
            categories = database.categoriesDao().getAll()

            withContext(Dispatchers.Main) {
                updateCategoriesList()
                val item = navView.menu.findItem(selectedCategoryId)
                //check if item has been deleted
                if (item == null) {
                    selectedCategoryId = TaskCategory.ALL_CATEGORY_ID.toInt()
                }
                setActionBar(navView.menu.findItem(selectedCategoryId).title)
                updateListView(null)
            }
        }
    }

    private inner class TasksDataObserver : DataSetObserver() {
        override fun onChanged() {
            super.onChanged()
            updateListView(null)
        }
    }

    companion object {
        private const val REQUEST_CODE = 1
    }
}