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
import com.sandra.tasky.db.DatabaseWrapper
import com.sandra.tasky.entity.TaskCategory
import com.sandra.tasky.utils.ToastWrapper
import kotlinx.android.synthetic.main.activity_categories.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoriesActivity : AppCompatActivity() {

    private lateinit var adapter: CategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        lvCategories.emptyView = tvNoCategories

        adapter = CategoriesAdapter(this, object : CategoriesAdapter.OnDeleteClickListener {
            override fun onClick(category: TaskCategory) {
                CoroutineScope(Dispatchers.Main).launch {
                    DatabaseWrapper.deleteCategory(this@CategoriesActivity, category)
                }
            }
        })

        lvCategories.adapter = adapter

        CoroutineScope(Dispatchers.Main).launch {
            val categories = DatabaseWrapper.getAllCategories(this@CategoriesActivity)
            updateCategoriesList(categories)
        }

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
                } else if (inputTitle.equals(getString(R.string.all), ignoreCase = true) || inputTitle.equals(getString(R.string.others), ignoreCase = true)) {
                    ToastWrapper.showShort( this@CategoriesActivity, R.string.reserved_title)
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        DatabaseWrapper.addCategory(this@CategoriesActivity, TaskCategory(title = inputTitle))
                        val allCategories = DatabaseWrapper.getAllCategories(this@CategoriesActivity)
                        updateCategoriesList(allCategories)

                        // todo handle unique constraint
//                        ToastWrapper.showShort(this@CategoriesActivity, R.string.category_exists)
                    }
                }
            }

            setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }

            show()
        }
    }

    private fun updateCategoriesList(categories: List<TaskCategory>) {
        adapter.setCategories(categories as MutableList<TaskCategory>)
    }

}