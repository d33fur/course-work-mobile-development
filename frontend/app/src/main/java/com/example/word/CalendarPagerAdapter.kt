package com.example.word

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.*

class CalendarPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val words: List<Pair<String, String>>
) : FragmentStateAdapter(fragmentActivity) {

    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH)
    private val currentYear = calendar.get(Calendar.YEAR)


    private val maxCount = if (currentYear == 2024) currentMonth + 1 else 12

    override fun getItemCount(): Int {
        return maxCount
    }

    override fun createFragment(position: Int): Fragment {
        val year = 2024
        return MonthFragment.newInstance(position, year, words)
    }
}
