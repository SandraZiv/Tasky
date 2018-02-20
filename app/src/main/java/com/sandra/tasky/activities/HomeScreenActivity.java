package com.sandra.tasky.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.TaskyUtils;
import com.sandra.tasky.adapter.HomeListAdapter;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.List;

public class HomeScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE = 1;

    private TaskDatabase database;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private FloatingActionButton fabAddTask;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_drawer);

        JodaTimeAndroid.init(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fabAddTask = (FloatingActionButton) findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewTask();
            }
        });

        //open db
        new OpenDBAsyncTask().execute("Open");
    }

    @Override
    protected void onResume() {
        super.onResume();
        new OpenDBAsyncTask().execute("Open");
    }

    @Override
    protected void onPause() {
        TaskyUtils.updateWidget(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        TaskyUtils.updateWidget(this);
        super.onDestroy();
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
            case R.id.home_menu_btn_delete_all:
                database.deleteAllData();
                updateListView();
                break;
            default:
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
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
        listView.setEmptyView(findViewById(R.id.home_empty_view));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openTaskIntent = new Intent(HomeScreenActivity.this, TaskActivity.class);
                openTaskIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, list.get(position));
                startActivityForResult(openTaskIntent, REQUEST_CODE);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);
                builder.setTitle(list.get(position).getTitle());

                ArrayAdapter<String> optionList = new ArrayAdapter<>(HomeScreenActivity.this, android.R.layout.simple_list_item_1);
                optionList.add(getString(R.string.delete));

                builder.setAdapter(optionList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(HomeScreenActivity.this, R.string.task_deleted, Toast.LENGTH_SHORT).show();
                        database.deleteData(list.get(position));
                        updateListView();
                        dialog.cancel();
                    }
                });

                builder.show();
                return true;
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.all_tasks) + " (" + list.size() + ")");
        }

        navigationView.getMenu().removeGroup(R.id.menu_group_top);
        navigationView.getMenu().add(R.id.menu_group_top, 1, 1, (getString(R.string.all_tasks) + " (" + list.size() + ")"));
        navigationView.getMenu().add(R.id.menu_group_top, 2, 2, "Others (1)");

        TaskyUtils.updateWidget(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                new OpenDBAsyncTask().execute("Reopen");
            }
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_manage:
                startActivity(new Intent(HomeScreenActivity.this, CategoriesActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(HomeScreenActivity.this, SettingsActivity.class));
                break;
            default:
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private class OpenDBAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            database = new TaskDatabase(HomeScreenActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            updateListView();
        }
    }
}
