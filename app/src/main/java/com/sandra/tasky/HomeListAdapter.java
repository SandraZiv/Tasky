package com.sandra.tasky;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class HomeListAdapter extends BaseAdapter {
    private List<SimpleTask> taskList;
    private Context context;

    public HomeListAdapter(Context context, List<SimpleTask> taskList) {
        this.taskList = taskList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View listView;
        final SimpleTask task = taskList.get(position);

        //in case there is only title present
        if (task.getNote().isEmpty() && task.getDueDate() == null) {
            listView = LayoutInflater.from(context).inflate(R.layout.list_title_home_screen, parent, false);
        }

        //in case note or due date is empty
        else if (task.getNote().isEmpty() || task.getDueDate() == null) {
            listView = LayoutInflater.from(context).inflate(R.layout.list_text_home_screen, parent, false);
            TextView text = (TextView) listView.findViewById(R.id.tw_text);

            if (!task.getNote().isEmpty())
                text.setText(task.getNote());
            else
                text.setText(context.getString(R.string.due_date) + ": " + (task.isTimePresent() ? task.parseDateTime() : task.parseDate()));

        }

        //in case everything is present
        else {
            listView = LayoutInflater.from(context).inflate(R.layout.list_all_home_screen, parent, false);

            TextView note = (TextView) listView.findViewById(R.id.tw_note);
            note.setText(task.getNote());

            TextView dueDate = (TextView) listView.findViewById(R.id.tw_due_date);
            dueDate.setText(context.getString(R.string.due_date) + ": " + (task.isTimePresent() ? task.parseDateTime() : task.parseDate()));
        }

        //setup for any case, there must always be title and checkbox
        TextView title = (TextView) listView.findViewById(R.id.tw_title);
        title.setText(task.getTitle());

        final CheckBox checkBox = (CheckBox) listView.findViewById(R.id.check_box);
        checkBox.setChecked(task.isCompleted());
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.setCompleted(checkBox.isChecked());
                TaskDatabase db = new TaskDatabase(context);
                db.updateData(task);
            }
        });

        return listView;
    }
}
