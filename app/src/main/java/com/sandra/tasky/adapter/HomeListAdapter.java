package com.sandra.tasky.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;

import java.util.List;

public class HomeListAdapter extends BaseAdapter {
    private Context context;
    private List<SimpleTask> taskList;

    public HomeListAdapter(Context context, List<SimpleTask> taskList) {
        this.context = context;
        this.taskList = taskList;
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
            TextView text = listView.findViewById(R.id.tw_text);

            if (!task.getNote().isEmpty()) {
                text.setText(cutText(task.getNote(), TaskyConstants.MAX_TEXT_LENGTH));
            } else {
                text.setText(task.isTimePresent() ? task.parseDateTime() : task.parseDate());
            }

        }
        //in case everything is present
        else {
            listView = LayoutInflater.from(context).inflate(R.layout.list_all_home_screen, parent, false);

            TextView note = listView.findViewById(R.id.tw_note);
            note.setText(cutText(task.getNote(), TaskyConstants.MAX_TEXT_LENGTH));

            TextView dueDate = listView.findViewById(R.id.tw_due_date);
            dueDate.setText(task.isTimePresent() ? task.parseDateTime() : task.parseDate());
        }

        //setup for any case, there must always be title and checkbox
        TextView title = listView.findViewById(R.id.tw_title);
        title.setText(cutText(task.getTitle(), TaskyConstants.MAX_TITLE_LENGTH));

        final CheckBox checkBox = listView.findViewById(R.id.check_box);
        checkBox.setChecked(task.isCompleted());
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.setCompleted(checkBox.isChecked());
                TaskDatabase db = new TaskDatabase(context);
                db.updateTask(task);
                notifyDataSetChanged();
            }
        });

        return listView;
    }

    private String cutText(String text, int limit) {
        return text.length() > limit ? text.substring(0, limit) + "..." : text;
    }
}
