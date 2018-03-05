package com.sandra.tasky.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sandra.tasky.R;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.TaskCategory;

import java.util.LinkedList;
import java.util.List;

public class CategoriesAdapter extends BaseAdapter {
    private Context context;
    private List<TaskCategory> categories;

    public CategoriesAdapter(Context context) {
        this.context = context;
        this.categories = new LinkedList<>();
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
        final TaskCategory category = categories.get(position);
        final View view;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
            view = convertView;
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        }
        ((TextView) view.findViewById(R.id.tv_category_title)).setText(category.getTitle());

        view.findViewById(R.id.iv_category_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CategoryAsyncTask().execute(category);
            }
        });

        return view;
    }

    public void setCategories(List<TaskCategory> categories) {
        this.categories = categories;
    }

    private class CategoryAsyncTask extends AsyncTask<TaskCategory, Integer, List<TaskCategory>> {
        private TaskCategory category;

        @Override
        protected List<TaskCategory> doInBackground(TaskCategory... params) {
            this.category = params[0];
            TaskDatabase database = new TaskDatabase(context);
            database.deleteCategory(category);
            return database.getAllCategories();
        }

        @Override
        protected void onPostExecute(List<TaskCategory> categories) {
            notifyDataSetChanged();
        }
    }
}
