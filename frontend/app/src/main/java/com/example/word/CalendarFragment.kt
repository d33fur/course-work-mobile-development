package com.example.word

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

class CalendarFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private var words: List<Pair<String, String>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            words = it.getSerializable("WORDS_DATA") as List<Pair<String, String>>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        viewPager = view.findViewById(R.id.viewPager)

        val calendarPagerAdapter = CalendarPagerAdapter(requireActivity(), words)
        viewPager.adapter = calendarPagerAdapter


        val calendar = Calendar.getInstance()
        val currentMonthPosition = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)
        viewPager.setCurrentItem(currentMonthPosition, false)

        return view
    }

    companion object {
        fun newInstance(words: List<Pair<String, String>>): CalendarFragment {
            val fragment = CalendarFragment()
            val args = Bundle()
            args.putSerializable("WORDS_DATA", ArrayList(words))
            fragment.arguments = args
            return fragment
        }
    }
}
