package com.example.word

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CalendarAdapter(
    private val context: Context,
    private val daysOfMonth: List<Int>,
    private val words: List<Pair<String, String>>
) : BaseAdapter() {

    override fun getCount(): Int {
        return daysOfMonth.size
    }

    override fun getItem(position: Int): Any {
        return daysOfMonth[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.calendar_day_item, parent, false)
            holder = ViewHolder()
            holder.dayText = view.findViewById(R.id.dayText)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val day = daysOfMonth[position]
        holder.dayText?.text = day.toString()

        // Проверяем, нужно ли отображать слово для данного дня
        if (position < words.size) {
            holder.dayText?.text = "${day}\n${words[position].first}"
        } else {
            holder.dayText?.text = day.toString()
        }

        return view
    }

    private class ViewHolder {
        var dayText: TextView? = null
    }
}
