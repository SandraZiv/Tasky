package com.sandra.tasky.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.sandra.tasky.db.DatabaseWrapper

class TasksViewModel : ViewModel() {

    fun getTasks(context: Context) = DatabaseWrapper.getTasks(context)

}