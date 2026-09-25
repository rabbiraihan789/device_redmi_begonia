package com.begonia.parts.thermal

import android.content.Context
import android.content.Intent
import android.os.UserHandle
import androidx.preference.PreferenceManager
import com.begonia.parts.util.KernelNode

object ThermalProfiles {

    const val MODE_DEFAULT = 0
    const val MODE_BENCHMARK = 1
    const val MODE_BROWSER = 2
    const val MODE_CAMERA = 3
    const val MODE_DIALER = 4
    const val MODE_GAMING = 5
    const val MODE_STREAMING = 6

    private val kernelStates = arrayOf("0", "10", "11", "12", "8", "9", "14")

    private const val KEY_ASSIGNMENTS = "profile_assignments"
    private const val SCONFIG = "/sys/class/thermal/thermal_message/sconfig"

    fun sconfigPresent(): Boolean = KernelNode.exists(SCONFIG)

    fun kernelState(mode: Int): String = kernelStates[mode.coerceIn(MODE_DEFAULT, MODE_STREAMING)]

    fun writeKernelState(mode: Int) {
        KernelNode.write(SCONFIG, kernelState(mode))
    }

    fun resetKernelState() {
        KernelNode.write(SCONFIG, kernelStates[MODE_DEFAULT])
    }

    fun boundMode(context: Context, packageName: String): Int {
        val raw = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_ASSIGNMENTS, null)
            ?: return MODE_DEFAULT
        return raw.split(",").asSequence()
            .map { it.split("=") }
            .filter { it.size == 2 && it[0] == packageName }
            .mapNotNull { it[1].toIntOrNull() }
            .firstOrNull()
            ?.coerceIn(MODE_DEFAULT, MODE_STREAMING)
            ?: MODE_DEFAULT
    }

    fun bindPackage(context: Context, packageName: String, mode: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val assignments = LinkedHashMap<String, Int>()
        prefs.getString(KEY_ASSIGNMENTS, null)?.split(",")?.forEach { pair ->
            val parts = pair.split("=")
            if (parts.size == 2) {
                parts[1].toIntOrNull()?.let { assignments[parts[0]] = it }
            }
        }
        if (mode == MODE_DEFAULT) {
            assignments.remove(packageName)
        } else {
            assignments[packageName] = mode
        }
        val joined = assignments.entries.joinToString(",") { "${it.key}=${it.value}" }
        prefs.edit().putString(KEY_ASSIGNMENTS, joined).apply()
    }

    fun applyPackage(context: Context, packageName: String) {
        writeKernelState(boundMode(context, packageName))
    }

    fun startWatcher(context: Context) {
        if (sconfigPresent()) {
            context.startServiceAsUser(
                Intent(context, ThermalWatcherService::class.java),
                UserHandle.CURRENT
            )
        }
    }
}