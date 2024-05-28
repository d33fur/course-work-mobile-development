package com.example.word

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.*

class WordOfDayFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var textViewCurrentDate: TextView
    private lateinit var infiniteAdapter: ViewPagerAdapter
    private var words: List<Pair<String, String>> = emptyList()
    private var initialDayIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_word_of_day, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        textViewCurrentDate = view.findViewById(R.id.textViewCurrentDate)

        infiniteAdapter = ViewPagerAdapter(requireActivity())
        viewPager.adapter = infiniteAdapter

        arguments?.let {
            words = it.getSerializable("WORDS_DATA") as? List<Pair<String, String>> ?: emptyList()
            initialDayIndex = it.getInt("DAY_INDEX", getCurrentDayOfYear() - 1)
        }

        if (words.isNotEmpty()) {
            infiniteAdapter.setWords(words)
            viewPager.setCurrentItem(initialDayIndex, false)
            updateCurrentDate(initialDayIndex)
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateCurrentDate(position)
            }
        })

        return view
    }

    private fun updateCurrentDate(position: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, position + 1)
        val sdf = SimpleDateFormat("dd MMMM", Locale.getDefault())
        val formattedDate = sdf.format(calendar.time)
        textViewCurrentDate.text = formattedDate
    }

    private fun getCurrentDayOfYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_YEAR)
    }

    companion object {
        fun newInstance(words: List<Pair<String, String>>?): WordOfDayFragment {
            val fragment = WordOfDayFragment()
            val args = Bundle()
            args.putSerializable("WORDS_DATA", ArrayList(words))
            fragment.arguments = args
            return fragment
        }
    }
}
