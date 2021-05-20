package com.sandra.tasky.activities.categories

import android.content.Context
import android.view.LayoutInflater
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

    override fun getCount(): Int = categories.size

    override fun getItem(position: Int): TaskCategory = categories[position]

    override fun getItemId(position: Int) = categories[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_category, parent, false)

        val category = categories[position]
        view.tvCategoryTitle.text = category.title
        view.btnDeleteCategory.setOnClickListener {
            onDeleteClickListener.onClick(category)
            categories.remove(category)
            notifyDataSetChanged()
        }

        return view
    }

    fun setCategories(categories: MutableList<TaskCategory>) {
        this.categories = categories
        notifyDataSetChanged()
    }

    interface OnDeleteClickListener {
        fun onClick(category: TaskCategory)
    }
}