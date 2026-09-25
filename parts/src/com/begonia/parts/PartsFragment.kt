package com.begonia.parts

import android.os.Bundle
import android.provider.Settings
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.collapsingtoolbar.BasePreferencesFragment

class PartsFragment : BasePreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.parts_dashboard, rootKey)
        bindDoubleTapToWake()
    }

    private fun bindDoubleTapToWake() {
        val toggle = findPreference<SwitchPreferenceCompat>("dt2w_switch") ?: return
        val resolver = requireContext().contentResolver
        toggle.isChecked = Settings.Secure.getInt(
            resolver, Settings.Secure.DOUBLE_TAP_TO_WAKE, 0
        ) == 1
        toggle.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            Settings.Secure.putInt(
                resolver,
                Settings.Secure.DOUBLE_TAP_TO_WAKE,
                if (enabled) 1 else 0
            )
            true
        }
    }
}