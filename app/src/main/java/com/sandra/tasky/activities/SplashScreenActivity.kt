package com.sandra.tasky.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sandra.tasky.R
import com.sandra.tasky.activities.home.HomeScreenActivity
import com.sandra.tasky.utils.startActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        startActivity(HomeScreenActivity::class.java)
        finish()
    }
}
