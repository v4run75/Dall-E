package com.example.dall_e

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.dall_e.databinding.ActivityMainBinding
import com.example.dall_e.ui.trends.TrendsFragment
import com.example.dall_e.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var helper: Helper? = null
    var appCompatActivity: AppCompatActivity = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        helper = Helper.instance

        helper?.moveFragment(
            HomeFragment(),
            null,
            R.id.nav_host_fragment_activity_main,
            appCompatActivity
        )
        val navView: BottomNavigationView = binding.navView
        val toolbar: Toolbar = binding.toolbar

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {

                    val currentFragment = supportFragmentManager.findFragmentById(R.id.container)

                    if (currentFragment is HomeFragment) {

                    } else {
                        helper = Helper.instance
                        helper?.moveFragment(
                            HomeFragment(),
                            null,
                            R.id.nav_host_fragment_activity_main,
                            appCompatActivity
                        )
                        return@OnNavigationItemSelectedListener true
                    }
                }
                R.id.navigation_trends -> {

                    val currentFragment = supportFragmentManager.findFragmentById(R.id.container)

                    if (currentFragment is TrendsFragment) {

                    } else {
                        helper = Helper.instance
                        helper?.moveFragment(
                            TrendsFragment(),
                            null,
                            R.id.nav_host_fragment_activity_main,
                            appCompatActivity
                        )
                        return@OnNavigationItemSelectedListener true
                    }
                }
            }

            return@OnNavigationItemSelectedListener false
        }

}