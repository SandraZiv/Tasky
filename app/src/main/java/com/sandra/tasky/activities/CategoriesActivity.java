package com.sandra.tasky.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.sandra.tasky.R;
import com.sandra.tasky.entity.TaskCategory;

import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        new CategoryAsyncTask().execute(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.categories_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class CategoryAsyncTask extends AsyncTask<Context, Integer ,List<TaskCategory>> {
        private Context context;

        @Override
        protected List<TaskCategory> doInBackground(Context... params) {
            this.context = params[0];

            return null;
        }

        @Override
        protected void onPostExecute(List<TaskCategory> taskCategories) {
            super.onPostExecute(taskCategories);
        }
    }
}
