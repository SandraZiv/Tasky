package com.sandra.tasky.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sandra.tasky.entity.TaskCategory;

import java.util.List;

import static com.sandra.tasky.TaskyConstants.DEFAULT_CATEGORY_ID;


public class CategoryPickerAdapter extends BaseAdapter {
    private Context context;
    private List<TaskCategory> categories;

    public CategoryPickerAdapter(Context context, List<TaskCategory> categories) {
        this.context = context;
        this.categories = categories;

        this.categories.add(0, new TaskCategory(DEFAULT_CATEGORY_ID, "All"));
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
        View view;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_selectable_list_item, parent, false);
            view = convertView;
        } else {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_single_choice, parent, false);
        }

        ((TextView)view.findViewById(android.R.id.text1)).setText(categories.get(position).getTitle());
//        ((TextView)view.findViewById(android.R.id.text1)).setTextSize(18);
        view.setTag(categories.get(position).getId());

        return view;
    }
}
