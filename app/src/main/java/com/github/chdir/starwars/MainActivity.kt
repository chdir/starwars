package com.github.chdir.starwars

import android.os.Bundle
import android.view.MenuItem
import android.widget.ViewAnimator
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var pager: ViewAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pager = findViewById(R.id.page_animator)
        bottomNav = findViewById(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { onItemSelected(it) }
    }

    private fun onItemSelected(item : MenuItem) : Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                pager.displayedChild = 0
            }
            R.id.action_favorites -> {
                pager.displayedChild = 1
            }
            R.id.action_about -> {
                pager.displayedChild = 2
            }
        }

        return true
    }
}