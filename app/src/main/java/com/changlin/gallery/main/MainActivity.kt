package com.changlin.gallery.main

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.changlin.gallery.R
import com.changlin.gallery.adapter.ViewPagerAdapter
import com.changlin.gallery.main.latest.LatestFragment
import com.changlin.gallery.main.random.RandomFragment
import com.changlin.gallery.main.toplist.TopListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var viewPager: ViewPager ?= null

    private var navView: BottomNavigationView ?= null

    private var menuItem: MenuItem ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        viewPager = findViewById(R.id.viewpager)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        //        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
        //                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
        //                .build();
//        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
//                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController)
        navView.setOnNavigationItemSelectedListener {
            menuItem = it
            val b: Unit = when (it.itemId) {
                R.id.navigation_home -> viewPager?.currentItem = 0
                R.id.navigation_dashboard -> viewPager?.currentItem = 1
                R.id.navigation_notifications -> viewPager?.currentItem = 2
                else -> viewPager?.currentItem = 0
            }
            false
        }
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (menuItem != null) {
                    menuItem!!.isChecked = false
                } else {
                    navView.menu.getItem(0).isChecked = false
                }
                menuItem = navView.menu.getItem(position)
                menuItem!!.isChecked = true
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        var viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPager!!.adapter = viewPagerAdapter
        var list = ArrayList<Fragment>()
        list.add(LatestFragment())
        list.add(TopListFragment())
        list.add(RandomFragment())
        viewPagerAdapter.setList(list)
    }

}
