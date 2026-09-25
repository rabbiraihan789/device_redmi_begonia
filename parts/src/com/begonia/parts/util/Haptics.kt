package com.begonia.parts.util

import android.content.Context
import androidx.preference.PreferenceManager

object Haptics {

    const val KEY_LEVEL = "vibration_level"

    private const val NODE = "/sys/class/leds/vibrator/vmax"
    private const val NODE_MAX = "/sys/class/leds/vibrator/vmax_max"

    fun setLevel(percent: Int) {
        val maximum = KernelNode.readLong(NODE_MAX)
        if (maximum <= 0) return
        val safePercent = percent.coerceIn(0, 100)
        val value = (maximum * safePercent + 50) / 100
        KernelNode.write(NODE, value)
    }

    fun restore(context: Context) {
        val percent = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(KEY_LEVEL, -1)
        if (percent >= 0) setLevel(percent)
    }
}