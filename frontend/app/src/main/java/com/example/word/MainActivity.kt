package com.example.word

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private var words: List<Pair<String, String>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

        lifecycleScope.launch {
            try {
                val json = fetchDataFromServer("http://85.92.110.57:8000/words")
                words = parseJson(json)

                if (savedInstanceState == null) {
                    showWordOfDayFragment()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_word_of_the_day -> {
                showWordOfDayFragment()
            }
            R.id.nav_all_words -> {
                showWordsFragment(showFavorites = false)
            }
            R.id.nav_favorites -> {
                showWordsFragment(showFavorites = true)
            }
            R.id.nav_calendar -> {
                showCalendarFragment()
            }
            R.id.nav_settings -> {
                showSettingsFragment()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showWordOfDayFragment() {
        val fragment = WordOfDayFragment.newInstance(words)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showWordsFragment(showFavorites: Boolean) {
        val fragment = WordsFragment.newInstance(words, showFavorites)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showCalendarFragment() {
        val fragment = CalendarFragment.newInstance(words)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showSettingsFragment() {
        val fragment = SettingsFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private suspend fun fetchDataFromServer(url: String): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                response.body?.string() ?: throw IOException("Null response body")
            }
        }
    }

    private fun parseJson(json: String): List<Pair<String, String>> {
        val jsonArray = JSONObject(json).getJSONArray("words")
        val wordList = mutableListOf<Pair<String, String>>()
        for (i in 0 until jsonArray.length()) {
            val wordObject = jsonArray.getJSONObject(i)
            val word = wordObject.getString("word")
            val about = wordObject.getString("about")
            wordList.add(Pair(word, about))
        }
        return wordList
    }
}
