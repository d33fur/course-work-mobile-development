package com.example.word

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val switchPreference: SwitchPreferenceCompat? = findPreference("notifications")

        switchPreference?.setOnPreferenceChangeListener { _, newValue ->
            val sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("notifications", newValue as Boolean)
                apply()
            }
            true
        }
    }
}
