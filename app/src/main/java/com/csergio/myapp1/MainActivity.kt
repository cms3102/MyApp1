package com.csergio.myapp1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.viewpager.widget.ViewPager
import com.csergio.myapp1.chat.SelectFriendsActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private  val io = NotificationService.getIO()
    private var myId = ""
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getSharedPreferences("UserCookie", Context.MODE_PRIVATE)
        myId = preferences.getString("user_id", "")

        if (myId.isNotEmpty()){
            // 서비스 확인 및 실행
            if (!NotificationService.state){
                Log.d("NotificationService", "메인 액티비티에서 NotificationService 실행함")
                val serviceIntent = Intent(this, NotificationService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            } else {
                Log.d("NotificationService", "메인 액티비티에서 NotificationService 실행 취소")
            }
        }

        // 액션바 대신 툴바 설정
        setSupportActionBar(mainActivity_toolbar)
        setToolbarTitle(0)

        // BottomNavigation과 ViewPager 이벤트 연동
        mainActivity_bottomNavigationView.setOnNavigationItemSelectedListener {

            when(it.itemId){
                R.id.mainActivity_menu_navigation_friends -> {
                    mainActivity_viewPager.currentItem = 0
                    setToolbarTitle(0)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.mainActivity_menu_navigation_chat -> {
                    mainActivity_viewPager.currentItem = 1
                    setToolbarTitle(1)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.mainActivity_menu_navigation_entertainment -> {
                    mainActivity_viewPager.currentItem = 2
                    setToolbarTitle(2)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.mainActivity_menu_navigation_settings -> {
                    mainActivity_viewPager.currentItem = 3
                    setToolbarTitle(3)
                    return@setOnNavigationItemSelectedListener true
                }
            }

            return@setOnNavigationItemSelectedListener false
        }

        // ViewPager 어댑터 설정 및 이벤트 처리
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        mainActivity_viewPager.adapter = viewPagerAdapter
        mainActivity_viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                mainActivity_bottomNavigationView.menu.getItem(position).isChecked = true
                setToolbarTitle(position)
            }

        })

    }

    // 툴바 메뉴 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    // 툴바 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            R.id.menu_toolbar_createChatRoom -> {
                startActivity(Intent(this, SelectFriendsActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setToolbarTitle(index:Int){
        when(index){
            0 -> supportActionBar?.title = "친구"
            1 -> supportActionBar?.title = "채팅"
            2 -> supportActionBar?.title = "오락"
            3 -> supportActionBar?.title = "설정"
            else -> supportActionBar?.title = ""
        }
    }

}
