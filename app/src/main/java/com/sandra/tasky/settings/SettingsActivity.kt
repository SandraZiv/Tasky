package com.sandra.tasky.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sandra.tasky.R

class SettingsActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
}