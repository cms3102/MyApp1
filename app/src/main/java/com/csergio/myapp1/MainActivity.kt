package com.csergio.myapp1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.csergio.myapp1.chat.SelectFriendsActivity
import com.csergio.myapp1.fragments.ChatFragment
import com.csergio.myapp1.fragments.EntertainmentFragment
import com.csergio.myapp1.fragments.FriendsFragment
import com.csergio.myapp1.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mainActivity_toolbar)
        supportActionBar?.title = "친구"

        mainActivity_bottomNavigationView.setOnNavigationItemSelectedListener {

            when(it.itemId){
                R.id.mainActivity_menu_navigation_friends -> {
                    mainActivity_viewPager.currentItem = 0
                    supportActionBar?.title = "친구"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.mainActivity_menu_navigation_chat -> {
                    mainActivity_viewPager.currentItem = 1
                    supportActionBar?.title = "채팅"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.mainActivity_menu_navigation_entertainment -> {
                    mainActivity_viewPager.currentItem = 2
                    supportActionBar?.title = "오락"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.mainActivity_menu_navigation_settings -> {
                    mainActivity_viewPager.currentItem = 3
                    supportActionBar?.title = "설정"
                    return@setOnNavigationItemSelectedListener true
                }
            }

            return@setOnNavigationItemSelectedListener false
        }

        mainActivity_viewPager.adapter = ViewPagerAdapter(supportFragmentManager)
        mainActivity_viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                mainActivity_bottomNavigationView.menu.getItem(position).isChecked = true
            }

        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            R.id.menu_toolbar_createChatRoom -> {
                startActivity(Intent(this, SelectFriendsActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }

}
