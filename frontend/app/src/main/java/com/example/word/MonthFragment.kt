package com.example.word

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class MonthFragment : Fragment() {

    private lateinit var gridView: GridView
    private lateinit var monthText: TextView
    private var words: List<Pair<String, String>> = emptyList()
    private var month: Int = 0
    private var year: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            month = it.getInt("MONTH")
            year = it.getInt("YEAR")
            words = it.getSerializable("WORDS_DATA") as List<Pair<String, String>>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_month, container, false)
        gridView = view.findViewById(R.id.calendarGridView)
        monthText = view.findViewById(R.id.monthText)

        updateMonthYear()

        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val dayIndex = getDayIndexForPosition(position)
            if (dayIndex != -1) {
                showWordOfDayFragment(dayIndex)
            }
        }

        return view
    }

    private fun updateMonthYear() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        monthText.text = sdf.format(calendar.time)

        val daysOfMonth = getDaysOfMonth()
        val availableWords = getAvailableWordsForMonth(month, year)

        val adapter = CalendarAdapter(requireContext(), daysOfMonth, availableWords)
        gridView.adapter = adapter

        gridView.viewTreeObserver.addOnGlobalLayoutListener {
            val totalHeight = gridView.height
            val rowCount = 5
            val columnCount = gridView.numColumns
            val itemHeight = totalHeight / rowCount
            val itemWidth = gridView.width / columnCount

            for (i in 0 until gridView.childCount) {
                val view = gridView.getChildAt(i)
                view?.layoutParams = AbsListView.LayoutParams(itemWidth, itemHeight)
            }
        }
    }

    private fun getDaysOfMonth(): List<Int> {
        val days = mutableListOf<Int>()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDay) {
            days.add(i)
        }
        return days
    }

    private fun getAvailableWordsForMonth(month: Int, year: Int): List<Pair<String, String>> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val maxDay = if (month == currentMonth && year == currentYear) {
            calendar.get(Calendar.DAY_OF_MONTH)
        } else {
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.YEAR, year)
            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        val startIndex = getStartIndexForMonth(month, year)
        val endIndex = startIndex + maxDay

        val safeStartIndex = startIndex.coerceAtLeast(0).coerceAtMost(words.size)
        val safeEndIndex = endIndex.coerceAtLeast(0).coerceAtMost(words.size)

        return words.subList(safeStartIndex, safeEndIndex)
    }

    private fun getStartIndexForMonth(month: Int, year: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val startOfYear = Calendar.getInstance().apply {
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_YEAR, 1)
        }

        return ((calendar.timeInMillis - startOfYear.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun getDayIndexForPosition(position: Int): Int {
        val calendar = Calendar.getInstance()
        return if (month == calendar.get(Calendar.MONTH) && year == calendar.get(Calendar.YEAR) && position >= calendar.get(Calendar.DAY_OF_MONTH)) {
            -1
        } else {
            getStartIndexForMonth(month, year) + position
        }
    }

    private fun showWordOfDayFragment(dayIndex: Int) {
        val fragment = WordOfDayFragment.newInstance(words)
        val args = Bundle()
        args.putInt("DAY_INDEX", dayIndex)
        args.putSerializable("WORDS_DATA", ArrayList(words))
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun newInstance(month: Int, year: Int, words: List<Pair<String, String>>): MonthFragment {
            val fragment = MonthFragment()
            val args = Bundle()
            args.putInt("MONTH", month)
            args.putInt("YEAR", year)
            args.putSerializable("WORDS_DATA", ArrayList(words))
            fragment.arguments = args
            return fragment
        }
    }
}
