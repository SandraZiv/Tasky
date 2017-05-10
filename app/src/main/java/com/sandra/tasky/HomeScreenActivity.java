package com.sandra.tasky;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class HomeScreenActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    private TaskDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //open db
        new OpenDBAsyncTask().execute("Open");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_menu_btn_sync:
                syncData();
                break;
            case R.id.home_menu_btn_new:
                createNewTask();
                break;
            case R.id.home_menu_btn_delete_all:
                database.deleteAllData();
                updateListView();
                break;
            default:
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void syncData() {
        updateListView();
        Toast.makeText(this, R.string.data_synced, Toast.LENGTH_SHORT).show();
    }

    private void createNewTask() {
        Intent newTaskIntent = new Intent(this, TaskActivity.class);
        startActivityForResult(newTaskIntent, REQUEST_CODE);
    }

    private void updateListView() {
        final List<SimpleTask> list = database.getAllData();
        ListAdapter homeListAdapter = new HomeListAdapter(HomeScreenActivity.this, list);
        ListView listView = (ListView) findViewById(R.id.home_list);
        listView.setAdapter(homeListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openTaskIntent = new Intent(HomeScreenActivity.this, TaskActivity.class);
                openTaskIntent.putExtra(SimpleTask.TASK_BUNDLE_KEY, list.get(position));
                startActivityForResult(openTaskIntent, REQUEST_CODE);
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.all_data) + " (" + list.size() + ")");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE)
            if (resultCode == RESULT_OK)
                updateListView();
    }

    private class OpenDBAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String ... params) {
            database = new TaskDatabase(HomeScreenActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            updateListView();
        }
    }
}
