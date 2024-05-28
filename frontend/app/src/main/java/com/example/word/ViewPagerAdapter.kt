package com.example.word

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.*

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var words: List<Pair<String, String>> = emptyList()

    override fun getItemCount(): Int {
        return words.size.coerceAtMost(getCurrentDayOfYear() + 1)
    }

    override fun createFragment(position: Int): Fragment {
        val word = words.getOrNull(position)?.first
        val about = words.getOrNull(position)?.second
        return PageFragment.newInstance(word, about, position >= getCurrentDayOfYear())
    }

    fun setWords(words: List<Pair<String, String>>) {
        this.words = words
        notifyDataSetChanged()
    }

    private fun getCurrentDayOfYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_YEAR)
    }
}
