package com.example.word

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView

class WordAdapter(
    context: Context,
    private val words: List<Pair<String, String>>,
    private val onItemClick: (Pair<String, String>) -> Unit
) : ArrayAdapter<Pair<String, String>>(context, 0, words) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val wordItem = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.word_list_item, parent, false)

        val wordTextView: TextView = view.findViewById(R.id.wordTextView)
        val favoriteButton: ImageButton = view.findViewById(R.id.favoriteButton)

        wordItem?.let {
            wordTextView.text = it.first
            val isFavorite = sharedPreferences.contains(it.first)
            favoriteButton.setImageResource(if (isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_empty)

            favoriteButton.setOnClickListener {
                val editor = sharedPreferences.edit()
                if (isFavorite) {
                    editor.remove(wordItem.first)
                    favoriteButton.setImageResource(R.drawable.ic_star_empty)
                } else {
                    editor.putString(wordItem.first, wordItem.second)
                    favoriteButton.setImageResource(R.drawable.ic_star_filled)
                }
                editor.apply()
                notifyDataSetChanged()
            }

            wordTextView.setOnClickListener {
                onItemClick(wordItem)
            }
        }

        return view
    }
}
