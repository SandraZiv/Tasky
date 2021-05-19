package com.sandra.tasky.activities.categories

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.sandra.tasky.R
import com.sandra.tasky.entity.TaskCategory
import kotlinx.android.synthetic.main.item_category.view.*

class CategoriesAdapter(
    private val context: Context,
    private val onDeleteClickListener: OnDeleteClickListener
) : BaseAdapter() {

    private var categories: MutableList<TaskCategory> = mutableListOf()

    override fun getCount(): Int {
        return categories.size
    }

    override fun getItem(position: Int): Any {
        return categories[position]
    }

    override fun getItemId(position: Int): Long {
        return categories[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val category = categories[position]
        val view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)

        view.tvCategoryTitle.text = category.title
        // todo
        view.tvCategoryTitle.setOnTouchListener { compoundView, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val margin = compoundView.right - compoundView.paddingRight
                if (event.rawX <= margin) {
                    compoundView.performClick()
                }
            }
            false
        }
        view.tvCategoryTitle.setOnClickListener {
            onDeleteClickListener.onClick(category)
            categories.remove(category)
        }
        return view
    }

    fun setCategories(categories: MutableList<TaskCategory>) {
        this.categories = categories
    }

    interface OnDeleteClickListener {
        fun onClick(category: TaskCategory)
    }
}