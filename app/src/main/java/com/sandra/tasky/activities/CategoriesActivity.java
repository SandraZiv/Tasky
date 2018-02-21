package com.sandra.tasky.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyUtils;
import com.sandra.tasky.adapter.CategoriesAdapter;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.TaskCategory;

import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private Toast mToast;
    private CategoriesDataObserver observer;
    private CategoriesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        observer = new CategoriesDataObserver();

        adapter = new CategoriesAdapter(this);
        adapter.registerDataSetObserver(observer);

        new CategoryAsyncTask().execute(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.categories_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_category:
                openNewCategoryDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.unregisterDataSetObserver(observer);
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
    }

    private void openNewCategoryDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_title);

        final EditText etTitle = new EditText(this);
        etTitle.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(etTitle);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String inputTitle = etTitle.getText().toString().trim();
                if (inputTitle.isEmpty()) {
                    mToast = TaskyUtils.addToast(mToast, CategoriesActivity.this, R.string.please_enter_title, true);
                } else {
                    //save new category
                    TaskDatabase database = new TaskDatabase(CategoriesActivity.this);
                    if (database.addCategory(new TaskCategory(inputTitle))) {
                        //update UI
                        new CategoryAsyncTask().execute(CategoriesActivity.this);
                    } else {
                        mToast = TaskyUtils.addToast(mToast, CategoriesActivity.this, R.string.category_exists, true);
                    }
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private class CategoryAsyncTask extends AsyncTask<Context, Integer, List<TaskCategory>> {
        private Context context;

        @Override
        protected List<TaskCategory> doInBackground(Context... params) {
            this.context = params[0];
            TaskDatabase database = new TaskDatabase(context);
            return database.getAllCategories();
        }

        @Override
        protected void onPostExecute(List<TaskCategory> categories) {
            adapter.setCategories(categories);

            ListView listView = (ListView) findViewById(R.id.lv_categories);
            listView.setAdapter(adapter);

            listView.setEmptyView(findViewById(R.id.tv_no_categories));
        }
    }

    private class CategoriesDataObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            new CategoryAsyncTask().execute(CategoriesActivity.this);
        }
    }
}
