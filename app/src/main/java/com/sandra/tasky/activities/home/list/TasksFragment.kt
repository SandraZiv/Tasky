package com.sandra.tasky.activities.home.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sandra.tasky.R
import com.sandra.tasky.databinding.FragmentTasksBinding
import com.sandra.tasky.db.DatabaseWrapper
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.utils.ToastWrapper
import com.sandra.tasky.utils.hide
import com.sandra.tasky.utils.show
import com.sandra.tasky.viewmodel.TasksViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

    private lateinit var binding: FragmentTasksBinding

    private val viewModel by lazy {
        ViewModelProvider(requireActivity()).get(TasksViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTasksBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        setupListItems(TaskDatabase(context).allTasks)

//        lifecycleScope.launchWhenStarted {
//            viewModel.getTasks(context).collect {
//                if (it.isEmpty()) {
//                    binding.rvTasks.hide()
//                } else {
//                    binding.rvTasks.show()
//                    setupListItems(it)
//                }
//            }
//        }
    }

    private fun setupListItems(items: List<SimpleTask>) {
        val context = requireContext()
        binding.rvTasks.adapter = TasksAdapter(items, object : TasksViewHolder.TaskItemListener {
            override fun onTaskClicked(task: SimpleTask) {
                // open task activity
            }

            override fun onTaskLongClicked(task: SimpleTask) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(task.title)
                val optionList =
                    ArrayAdapter<String>(context, android.R.layout.simple_list_item_1)
                optionList.add(getString(R.string.delete))
                builder.setAdapter(optionList) { dialog, _ ->
                    CoroutineScope(Dispatchers.Main).launch {
                        DatabaseWrapper.deleteTask(context, task)
                        ToastWrapper.showShort(context, R.string.task_deleted)
                    }
                    dialog.cancel()
                }
                builder.show()
            }

            override fun onTaskChecked(task: SimpleTask, isChecked: Boolean) {
                task.isCompleted = isChecked
                CoroutineScope(Dispatchers.Main).launch {
                    DatabaseWrapper.updateTask(context, task)
                }
            }
        })
    }

}