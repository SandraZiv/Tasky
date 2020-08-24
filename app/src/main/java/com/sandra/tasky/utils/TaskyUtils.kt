package com.sandra.tasky.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.sandra.tasky.R
import com.sandra.tasky.widget.TaskWidget
import java.io.*

object TaskyUtils {

    fun updateWidget(context: Context) {
        val taskyWidget = ComponentName(context, TaskWidget::class.java)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(taskyWidget), R.id.widgetList)
    }

    fun serialize(obj: Any?): ByteArray {
        val out = ByteArrayOutputStream()
        val os = ObjectOutputStream(out)
        os.writeObject(obj)
        return out.toByteArray()
    }

    fun deserialize(data: ByteArray?): Any {
        val `in` = ByteArrayInputStream(data)
        val `is` = ObjectInputStream(`in`)
        return `is`.readObject()
    }
}