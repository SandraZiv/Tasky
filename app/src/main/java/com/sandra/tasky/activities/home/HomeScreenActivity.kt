package com.sandra.tasky.activities.home

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.android.material.tabs.TabLayoutMediator
import com.sandra.tasky.AppPrefs
import com.sandra.tasky.R
import com.sandra.tasky.SortType
import com.sandra.tasky.activities.TaskActivity
import com.sandra.tasky.activities.home.list.TasksFragment
import com.sandra.tasky.db.DatabaseWrapper
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.entity.TaskCategory
import com.sandra.tasky.utils.TaskyUtils
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import kotlinx.android.synthetic.main.fragment_tasks.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import java.util.*

class HomeScreenActivity : AppCompatActivity() {

    private var tasks: List<SimpleTask> = emptyList()
    private var visibleTasks: List<SimpleTask> = emptyList()

    private var categories: List<TaskCategory> = emptyList()
    private var selectedCategoryId = MENU_ITEM_CATEGORY_ALL_TASKS_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onPause() {
        TaskyUtils.updateWidget(this)
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_screen_menu, menu)
        val searchView = menu.findItem(R.id.home_menu_search).actionView as SearchView
        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
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
        fabAddTask.hide()
    }

    private fun showItems(menu: Menu) {
        menu.setGroupVisible(R.id.menu_group_hidden, true)
        fabAddTask.show()
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
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            query?.let { search(it) }
        }
    }

    private fun search(query: String) {
//        updateListView(query)
    }

    private fun sortBy() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.sort_by)
        val selectedSortOption = AppPrefs.getSortingCriteria(this)
        val sortOptions = arrayOf(getString(R.string.due_date), getString(R.string.title), getString(R.string.completed))
        builder.setSingleChoiceItems(sortOptions, selectedSortOption) { _, which ->
            AppPrefs.updateSortingCriteria(this, which)
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
                CoroutineScope(Dispatchers.Main).launch {
                    if (selectedCategoryId == MENU_ITEM_CATEGORY_ALL_TASKS_ID) {
                        DatabaseWrapper.deleteAllTasks(this@HomeScreenActivity)
                    } else {
                        val ids = IntArray(filteredTasks.size)
                        for (i in filteredTasks.indices) {
                            ids[i] = filteredTasks[i].id
                        }
                        DatabaseWrapper.deleteAllTasksInCategory(this@HomeScreenActivity, ids)
                    }
                    loadData()
                    updateUI()
                }
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        }
        builder.show()
    }

    private fun createNewTask(dateTime: DateTime? = null) {
        val newTaskIntent = Intent(this, TaskActivity::class.java)
        if (!isSelectedDefaultCategory()) {
            val selectedCategory = categories.findLast { it.id == selectedCategoryId }
            newTaskIntent.putExtra(TaskActivity.EXTRAS_SELECTED_CATEGORY_KEY, selectedCategory)
        }
        dateTime?.let { newTaskIntent.putExtra(TaskActivity.EXTRAS_SELECTED_DATETIME_KEY, it) }
        resultLauncher.launch(newTaskIntent)
    }

    private fun openTaskActivity(task: SimpleTask) {
        val openTaskIntent = Intent(this, TaskActivity::class.java)
        openTaskIntent.putExtra(TaskActivity.EXTRAS_TASK_KEY, task)
        resultLauncher.launch(openTaskIntent)
    }

    private fun updateCategoriesList() {
        navView.menu.apply {
            removeGroup(R.id.menu_group_top)

            add(R.id.menu_group_top, MENU_ITEM_CATEGORY_ALL_TASKS_ID, Menu.NONE, getString(R.string.all_num, tasks.size))

            var totalNumOfTasksWithCategory = 0
            categories.forEach {
                val numOfTasksInCategory = tasks.filter { task -> task.category == it }.count()
                add(R.id.menu_group_top, it.id, Menu.NONE, "${it.title} ($numOfTasksInCategory)")
                totalNumOfTasksWithCategory += numOfTasksInCategory
            }

            if (categories.isNotEmpty()) {
                add(R.id.menu_group_top, MENU_ITEM_CATEGORY_OTHER_TASKS_ID, Menu.NONE,
                    getString(R.string.others_num, tasks.size - totalNumOfTasksWithCategory))
            }

        }
    }

    private fun sortAndFilterTasks(query: String?): List<SimpleTask> {
        val filteredTasks =
            if (selectedCategoryId == MENU_ITEM_CATEGORY_ALL_TASKS_ID) {
                tasks
            } else {
                tasks.filter {
                    it.category == null && selectedCategoryId == MENU_ITEM_CATEGORY_OTHER_TASKS_ID
                            || it.category != null && it.category!!.id == selectedCategoryId
                }
                    .toList()
            }
        val queryTasks: MutableList<SimpleTask>
        if (query != null) {
            queryTasks = LinkedList()
            for (task in filteredTasks) {
                if (task.title.lowercase().contains(query.lowercase().trim { it <= ' ' })) {
                    queryTasks.add(task)
                }
            }
        } else {
            queryTasks = LinkedList(filteredTasks)
        }

        queryTasks.sortWith { o1, o2 ->
            when (AppPrefs.getSortingCriteria(this)) {
                SortType.SORT_DUE_DATE.value -> 0
                SortType.SORT_TITLE.value -> o1!!.title.lowercase().compareTo(o2!!.title.lowercase())
                SortType.SORT_COMPLETED.value -> if (o1!!.isCompleted) 1 else -1
                else -> 0
            }
        }
        return queryTasks
    }

    private fun isSelectedDefaultCategory() = selectedCategoryId == MENU_ITEM_CATEGORY_ALL_TASKS_ID ||
            selectedCategoryId == MENU_ITEM_CATEGORY_OTHER_TASKS_ID

    private fun loadDataAndUpdateUI() {
        CoroutineScope(Dispatchers.Main).launch {
            loadData()
            updateUI()
        }
    }

    private suspend fun loadData() = withContext(Dispatchers.IO) {
        val database = TaskDatabase(this@HomeScreenActivity)
        tasks = database.allTasks
        categories = database.allCategories
    }

    private fun updateUI() {
        updateCategoriesList()
        val item = navView.menu.findItem(selectedCategoryId)
        //check if item has been deleted
        if (item == null) {
            selectedCategoryId = MENU_ITEM_CATEGORY_ALL_TASKS_ID
        }
//        setActionBar(navView.menu.findItem(selectedCategoryId).title)
//        updateListView()
    }

    private val resultLauncher =  registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            loadDataAndUpdateUI()
            invalidateOptionsMenu()
        }
    }

    companion object {
        private const val MENU_ITEM_CATEGORY_ALL_TASKS_ID = -1
        private const val MENU_ITEM_CATEGORY_OTHER_TASKS_ID = -2
    }

}