package com.csergio.myapp1

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.csergio.myapp1.fragments.ChatFragment
import com.csergio.myapp1.fragments.EntertainmentFragment
import com.csergio.myapp1.fragments.FriendsFragment
import com.csergio.myapp1.fragments.SettingsFragment

class ViewPagerAdapter(fm:FragmentManager):FragmentStatePagerAdapter(fm) {

    private val FRAGMENT_COUNT = 4

    override fun getItem(position: Int): Fragment {

        return when (position){
            1 -> ChatFragment()
            2 -> EntertainmentFragment()
            3 -> SettingsFragment()
            else -> FriendsFragment()
        }

    }

    override fun getCount(): Int {
        return FRAGMENT_COUNT
    }

}