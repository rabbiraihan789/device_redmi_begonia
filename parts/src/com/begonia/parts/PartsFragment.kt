package com.begonia.parts

import android.os.Bundle
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.collapsingtoolbar.BasePreferencesFragment
import com.android.settingslib.widget.SliderPreference
import com.begonia.parts.util.Haptics
import com.begonia.parts.util.MiSound

class PartsFragment : BasePreferencesFragment() {

    override fun getTitle(): CharSequence = getString(R.string.app_name)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.parts_dashboard, rootKey)
        bindDoubleTapToWake()
        bindVibration()
        bindMiSound()
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

    private fun bindVibration() {
        val slider = findPreference<SliderPreference>(Haptics.KEY_LEVEL) ?: return
        slider.setOnPreferenceChangeListener { _, newValue ->
            Haptics.setLevel(newValue as Int)
            true
        }
    }

    private fun bindMiSound() {
        val enable = findPreference<SwitchPreferenceCompat>(MiSound.KEY_ENABLE) ?: return
        enable.isChecked = MiSound.isEnabled()
        enable.setOnPreferenceChangeListener { _, newValue ->
            MiSound.setEnabled(newValue as Boolean)
            true
        }

        val headset = findPreference<ListPreference>(MiSound.KEY_HEADSET)
        headset?.let { pref ->
            pref.value = MiSound.currentHeadset().toString()
            pref.summary = pref.entry
            pref.setOnPreferenceChangeListener { p, newValue ->
                val value = newValue.toString()
                MiSound.setHeadset(value)
                val index = pref.findIndexOfValue(value)
                p.summary = pref.entries?.getOrNull(index)?.toString() ?: value
                true
            }
        }

        val preset = findPreference<ListPreference>(MiSound.KEY_PRESET)
        preset?.let { pref ->
            pref.value = MiSound.currentScenario().toString()
            pref.summary = pref.entry
            pref.setOnPreferenceChangeListener { p, newValue ->
                val value = newValue.toString()
                MiSound.setScenario(value)
                val index = pref.findIndexOfValue(value)
                p.summary = pref.entries?.getOrNull(index)?.toString() ?: value
                true
            }
        }
    }
}