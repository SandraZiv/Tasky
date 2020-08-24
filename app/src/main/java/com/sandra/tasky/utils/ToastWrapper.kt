package com.sandra.tasky.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object ToastWrapper {

    private var toast: Toast? = null

    fun showShort(context: Context, @StringRes messageId: Int) {
        show(context, messageId, Toast.LENGTH_SHORT)
    }

    fun showLong(context: Context, @StringRes messageId: Int) {
        show(context, messageId, Toast.LENGTH_LONG)
    }

    private fun show(context: Context, @StringRes messageId: Int, toastLength: Int) {
        toast?.cancel()
        toast = Toast.makeText(context, messageId, toastLength)
        toast!!.show()
    }
}