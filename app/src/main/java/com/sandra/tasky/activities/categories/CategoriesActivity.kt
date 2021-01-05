package com.sandra.tasky.activities.categories

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.sandra.tasky.R
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.TaskCategory
import com.sandra.tasky.utils.ToastWrapper
import kotlinx.android.synthetic.main.activity_categories.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesActivity : AppCompatActivity() {

    private lateinit var adapter: CategoriesAdapter
    private lateinit var database: TaskDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        database = TaskDatabase(this)

        lvCategories.emptyView = tvNoCategories

        adapter = CategoriesAdapter(this, object : CategoriesAdapter.OnDeleteClickListener {
            override fun onClick(category: TaskCategory) {
                CoroutineScope(Dispatchers.IO).launch {
                    database.deleteCategory(category)
                }
            }
        })
        lvCategories.adapter = adapter
        getAllCategoriesFromDb()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.categories_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_category -> {
                openNewCategoryDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setResult(Activity.RESULT_OK, Intent())
    }

    private fun openNewCategoryDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.enter_title)

            val etTitle = EditText(this@CategoriesActivity)
            etTitle.inputType = InputType.TYPE_CLASS_TEXT
            setView(etTitle)

            setPositiveButton(R.string.ok) { _, _ ->
                val inputTitle = etTitle.text.toString().trim { it <= ' ' }
                if (inputTitle.isEmpty()) {
                    ToastWrapper.showShort(this@CategoriesActivity, R.string.please_enter_title)
                } else if (inputTitle.toLowerCase() == getString(R.string.all).toLowerCase() || inputTitle.toLowerCase() == getString(R.string.others).toLowerCase()) {
                    ToastWrapper.showShort( this@CategoriesActivity, R.string.reserved_title)
                } else {
                    // todo handle unique constraint
                    // save new category
                    CoroutineScope(Dispatchers.IO).launch {
                        database.addCategory(TaskCategory(title = inputTitle))
                        getAllCategoriesFromDb()
//                        ToastWrapper.showShort(this@CategoriesActivity, R.string.category_exists)
                    }
                }
            }

            setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }

            show()
        }
    }

    private fun getAllCategoriesFromDb() {
        CoroutineScope(Dispatchers.IO).launch {
            val allCategories = database.allCategories
            withContext(Dispatchers.Main) {
                adapter.setCategories(allCategories as MutableList<TaskCategory>)
            }
        }
    }
}