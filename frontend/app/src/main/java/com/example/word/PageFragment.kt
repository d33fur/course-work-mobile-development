package com.example.word

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment

class PageFragment : Fragment() {

    private var word: String? = null
    private var about: String? = null
    private var isTomorrow: Boolean = false
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            word = it.getString("WORD")
            about = it.getString("ABOUT")
            isTomorrow = it.getBoolean("IS_TOMORROW", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_page, container, false)

        val wordTextView: TextView = view.findViewById(R.id.wordTextView)
        val aboutTextView: TextView = view.findViewById(R.id.aboutTextView)
        val visitTomorrowTextView: TextView = view.findViewById(R.id.visitTomorrowTextView)
        val favoriteButton: ImageButton = view.findViewById(R.id.favoriteButton)

        val sharedPreferences = requireActivity().getSharedPreferences("favorites", Context.MODE_PRIVATE)
        isFavorite = sharedPreferences.contains(word)

        if (isTomorrow) {
            visitTomorrowTextView.visibility = View.VISIBLE
            wordTextView.visibility = View.GONE
            aboutTextView.visibility = View.GONE
            favoriteButton.visibility = View.GONE
        } else {
            visitTomorrowTextView.visibility = View.GONE
            wordTextView.visibility = View.VISIBLE
            aboutTextView.visibility = View.VISIBLE
            favoriteButton.visibility = View.VISIBLE

            wordTextView.text = word
            aboutTextView.text = about

            favoriteButton.setImageResource(if (isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_empty)

            favoriteButton.setOnClickListener {
                isFavorite = if (isFavorite) {
                    sharedPreferences.edit().remove(word).apply()
                    favoriteButton.setImageResource(R.drawable.ic_star_empty)
                    false
                } else {
                    sharedPreferences.edit().putString(word, about).apply()
                    favoriteButton.setImageResource(R.drawable.ic_star_filled)
                    true
                }
            }
        }

        return view
    }

    companion object {
        fun newInstance(word: String?, about: String?, isTomorrow: Boolean): PageFragment {
            val fragment = PageFragment()
            val args = Bundle()
            args.putString("WORD", word)
            args.putString("ABOUT", about)
            args.putBoolean("IS_TOMORROW", isTomorrow)
            fragment.arguments = args
            return fragment
        }
    }
}
