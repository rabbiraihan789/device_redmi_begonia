package com.begonia.parts.util

import android.content.Context
import android.media.AudioSystem
import android.os.SystemProperties
import android.util.Log
import androidx.preference.PreferenceManager

object MiSound {

    const val KEY_ENABLE = "misound_enable"
    const val KEY_HEADSET = "misound_headset"
    const val KEY_PRESET = "misound_preset"

    const val DEFAULT_HEADSET = 15

    const val DEFAULT_PRESET = 0

    private const val PROP_MUSIC = "persist.vendor.audio.sfx.hd.music.state"
    private const val PROP_HEADSET = "persist.vendor.audio.sfx.hd.type"
    private const val PROP_PRESET = "persist.vendor.audio.scenario"

    private const val TAG = "MiSound"

    val HEADSET_IDS = intArrayOf(1, 2, 5, 8, 9, 10, 11, 13, 14, 15, 16, 17)

    fun isEnabled(): Boolean = SystemProperties.getBoolean(PROP_MUSIC, false)

    fun setEnabled(enabled: Boolean) {
        val value = if (enabled) "true" else "false"
        setParam("MusicMode", value)
        setProp(PROP_MUSIC, value)
    }

    fun currentHeadset(): Int {
        val value = SystemProperties.getInt(PROP_HEADSET, -1)
        return if (value in HEADSET_IDS) value else DEFAULT_HEADSET
    }

    fun setHeadset(value: String) {
        value.toIntOrNull()?.let { setHeadset(it) }
    }

    fun setHeadset(type: Int) {
        if (type !in HEADSET_IDS) return
        val value = type.toString()
        setParam("HeadphoneIndex", value)
        setProp(PROP_HEADSET, value)
    }

    fun currentScenario(): Int {
        val value = SystemProperties.getInt(PROP_PRESET, -1)
        return if (value in 0..3) value else DEFAULT_PRESET
    }

    fun setScenario(value: String) {
        value.toIntOrNull()?.let { setScenario(it) }
    }

    fun setScenario(scenario: Int) {
        if (scenario !in 0..3) return
        val value = scenario.toString()
        setParam("MisoundScenario", value)
        setProp(PROP_PRESET, value)
    }

    fun restore(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!prefs.contains(KEY_ENABLE)) {
            return
        }
        val enabled = prefs.getBoolean(KEY_ENABLE, false)
        setEnabled(enabled)
        prefs.getString(KEY_HEADSET, null)?.let { setHeadset(it) }
        prefs.getString(KEY_PRESET, null)?.let { setScenario(it) }
    }

    private fun setParam(key: String, value: String) {
        try {
            AudioSystem.setParameters("misound=$key: $value")
        } catch (e: RuntimeException) {
            Log.w(TAG, "setParameters misound $key failed", e)
        }
    }

    private fun setProp(key: String, value: String) {
        try {
            SystemProperties.set(key, value)
        } catch (e: RuntimeException) {
            Log.w(TAG, "set prop $key failed", e)
        }
    }
}