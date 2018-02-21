package com.sandra.tasky.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sandra.tasky.R;
import com.sandra.tasky.entity.TaskCategory;

import java.util.List;

public class CategoriesAdapter extends BaseAdapter {
    private Context context;
    private List<TaskCategory> categories;

    public CategoriesAdapter(Context context, List<TaskCategory> categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return categories.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TaskCategory category = categories.get(position);
        View view;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
            view = convertView;
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        }
        ((TextView) view.findViewById(R.id.tv_category_title)).setText(category.getTitle());
        return view;
    }
}
