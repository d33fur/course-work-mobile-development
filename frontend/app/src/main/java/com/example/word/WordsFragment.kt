package com.example.word

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.*

class WordsFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var buttonAllWords: Button
    private lateinit var buttonFavorites: Button
    private lateinit var wordsAdapter: WordAdapter
    private lateinit var favoriteWordsAdapter: WordAdapter
    private var words: List<Pair<String, String>> = emptyList()
    private lateinit var sharedPreferences: SharedPreferences

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
        val view = inflater.inflate(R.layout.fragment_words, container, false)

        listView = view.findViewById(R.id.listView)
        buttonAllWords = view.findViewById(R.id.button_all_words)
        buttonFavorites = view.findViewById(R.id.button_favorites)

        sharedPreferences = requireActivity().getSharedPreferences("favorites", Context.MODE_PRIVATE)

        val availableWords = getAvailableWords()

        wordsAdapter = WordAdapter(requireContext(), availableWords) { wordPair ->
            val dayIndex = findWordIndex(wordPair.first)
            showWordOfDayFragment(dayIndex)
        }
        favoriteWordsAdapter = WordAdapter(requireContext(), getFavoriteWords(availableWords)) { wordPair ->
            val dayIndex = findWordIndex(wordPair.first)
            showWordOfDayFragment(dayIndex)
        }

        listView.adapter = wordsAdapter

        buttonAllWords.setOnClickListener {
            updateFavoriteWords()
            listView.adapter = wordsAdapter
            updateButtonState(isAllWords = true)
        }

        buttonFavorites.setOnClickListener {
            updateFavoriteWords()
            listView.adapter = favoriteWordsAdapter
            updateButtonState(isAllWords = false)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val wordPair = if (buttonFavorites.isEnabled) wordsAdapter.getItem(position) else favoriteWordsAdapter.getItem(position)
            wordPair?.let {
                val dayIndex = findWordIndex(it.first)
                showWordOfDayFragment(dayIndex)
            }
        }

        val showFavorites = arguments?.getBoolean("SHOW_FAVORITES") ?: false
        updateButtonState(isAllWords = !showFavorites)
        listView.adapter = if (showFavorites) favoriteWordsAdapter else wordsAdapter

        return view
    }

    private fun getAvailableWords(): List<Pair<String, String>> {
        val currentDayOfYear = getCurrentDayOfYear()
        return words.take(currentDayOfYear)
    }

    private fun getFavoriteWords(availableWords: List<Pair<String, String>>): List<Pair<String, String>> {
        val favorites = sharedPreferences.all.keys
        return availableWords.filter { favorites.contains(it.first) }
    }

    private fun updateFavoriteWords() {
        val availableWords = getAvailableWords()
        favoriteWordsAdapter.clear()
        favoriteWordsAdapter.addAll(getFavoriteWords(availableWords))
        favoriteWordsAdapter.notifyDataSetChanged()
    }

    private fun updateButtonState(isAllWords: Boolean) {
        if (isAllWords) {
            buttonAllWords.isEnabled = false
            buttonAllWords.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.selected_button)
            buttonFavorites.isEnabled = true
            buttonFavorites.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.unselected_button)
        } else {
            buttonAllWords.isEnabled = true
            buttonAllWords.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.unselected_button)
            buttonFavorites.isEnabled = false
            buttonFavorites.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.selected_button)
        }
    }

    private fun findWordIndex(word: String): Int {
        return words.indexOfFirst { it.first == word }
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

    private fun getCurrentDayOfYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_YEAR)
    }

    companion object {
        fun newInstance(words: List<Pair<String, String>>, showFavorites: Boolean = false): WordsFragment {
            val fragment = WordsFragment()
            val args = Bundle()
            args.putSerializable("WORDS_DATA", ArrayList(words))
            args.putBoolean("SHOW_FAVORITES", showFavorites)
            fragment.arguments = args
            return fragment
        }
    }
}
