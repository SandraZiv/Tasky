package com.sandra.tasky.activities.home

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.sandra.tasky.R
import com.sandra.tasky.activities.categories.CategoriesActivity
import com.sandra.tasky.databinding.ActivityHomeBinding
import com.sandra.tasky.settings.SettingsActivity
import net.danlew.android.joda.JodaTimeAndroid

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        JodaTimeAndroid.init(this)

        val drawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open_nav, R.string.close_nav)
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        binding.viewPager.adapter = SwipeAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setText(
                if (position == SwipeAdapter.TASKS_FRAGMENT_POSITION) R.string.tasks
                else R.string.calendar
            )
        }.attach()

        binding.fabAddTask.setOnClickListener {
            Toast.makeText(this, "Add task", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_manage -> resultLauncher.launch(Intent(this, CategoriesActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            else -> openCategory(item.itemId, item.title)
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun openCategory(categoryId: Int, categoryTitle: CharSequence) {
//        setActionBar(categoryTitle)
//        selectedCategoryId = categoryId
//        updateListView()
    }

    private val resultLauncher =  registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
//            loadDataAndUpdateUI()
            invalidateOptionsMenu()
        }
    }

    private fun setToolbarTitle(title: String) {
        supportActionBar?.let {
            it.title = title
        }
    }

}