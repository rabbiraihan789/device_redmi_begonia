package com.begonia.parts.cpu

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.begonia.parts.R
import com.begonia.parts.util.KernelNode
import java.io.File
import java.util.Locale

object CpuTuner {

    const val KEY_GOVERNOR = "cpu_scheduler"
    private const val KEY_FLOOR_PREFIX = "cpu_floor_"
    private const val KEY_CEILING_PREFIX = "cpu_ceiling_"

    private const val SYS_CPUFREQ = "/sys/devices/system/cpu/cpufreq"
    private const val SCHEDUTIL_UP_US = "500"
    private const val SCHEDUTIL_DOWN_US = "20000"

    private val GOVERNOR_ORDER = listOf(
        "schedutil", "interactive", "ondemand", "conservative",
        "userspace", "powersave", "performance"
    )

    fun policies(): List<String> =
        (File(SYS_CPUFREQ).listFiles() ?: emptyArray())
            .filter { it.name.startsWith("policy") }
            .map { it.name }
            .sorted()

    fun availableGovernors(): List<String> {
        val available = linkedSetOf<String>()
        for (policy in policies()) {
            KernelNode.read("$SYS_CPUFREQ/$policy/scaling_available_governors")
                ?.split(Regex("\\s+"))
                ?.filter { it.isNotBlank() }
                ?.let(available::addAll)
        }
        val preferred = GOVERNOR_ORDER.filter { it in available }
        return preferred + (available - preferred)
    }

    fun currentGovernor(): String? {
        for (policy in policies()) {
            KernelNode.read("$SYS_CPUFREQ/$policy/scaling_governor")
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }
        }
        return null
    }

    fun frequencies(policy: String): LongArray {
        val raw = KernelNode.read("$SYS_CPUFREQ/$policy/scaling_available_frequencies")
            ?: return LongArray(0)
        return raw.split(Regex("\\s+"))
            .mapNotNull { it.toLongOrNull() }
            .distinct()
            .sorted()
            .toLongArray()
    }

    fun floorKey(policy: String) = KEY_FLOOR_PREFIX + policy
    fun ceilingKey(policy: String) = KEY_CEILING_PREFIX + policy

    fun prefsLong(prefs: SharedPreferences, key: String, default: Long): Long {
        val stored = prefs.all[key] ?: return default
        return when (stored) {
            is Long -> stored
            is Int -> stored.toLong()
            is String -> stored.toLongOrNull() ?: default
            else -> default
        }
    }

    fun prefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

    fun applyGovernor(policy: String, governor: String) {
        val base = "$SYS_CPUFREQ/$policy"
        if (!KernelNode.write("$base/scaling_governor", governor)) return
        if (governor == "schedutil") {
            KernelNode.write("$base/schedutil/up_rate_limit_us", SCHEDUTIL_UP_US)
            KernelNode.write("$base/schedutil/down_rate_limit_us", SCHEDUTIL_DOWN_US)
        }
    }

    fun restore(context: Context) {
        val prefs = prefs(context)
        val governor = prefs.getString(KEY_GOVERNOR, "") ?: ""
        if (governor.isEmpty()) return

        val policies = policies()
        for (policy in policies) applyGovernor(policy, governor)
        for (policy in policies) {
            val floor = prefsLong(prefs, floorKey(policy), -1L)
            val ceiling = prefsLong(prefs, ceilingKey(policy), -1L)
            if (floor > 0) KernelNode.write("$SYS_CPUFREQ/$policy/scaling_min_freq", floor)
            if (ceiling > 0) KernelNode.write("$SYS_CPUFREQ/$policy/scaling_max_freq", ceiling)
        }
        if (governor == "userspace") {
            for (policy in policies) {
                var target = prefsLong(prefs, ceilingKey(policy), -1L)
                if (target <= 0) target = KernelNode.readLong("$SYS_CPUFREQ/$policy/scaling_max_freq")
                if (target > 0) KernelNode.write("$SYS_CPUFREQ/$policy/scaling_setspeed", target)
            }
        }
    }

    fun governorLabel(governor: String): String = when (governor) {
        "schedutil" -> "Adaptive scheduler (balanced)"
        "interactive" -> "Interactive (responsive)"
        "ondemand" -> "Ondemand (scales on load)"
        "conservative" -> "Conservative (gradual)"
        "userspace" -> "Userspace (fixed speed)"
        "powersave" -> "Powersave (lowest)"
        "performance" -> "Performance (highest)"
        else -> governor
    }

    fun clusterLabel(context: Context, policy: String): String {
        val res = when (policy) {
            "policy0" -> R.string.cpu_cluster_efficiency
            "policy6" -> R.string.cpu_cluster_performance
            else -> null
        }
        return if (res != null) context.getString(res) else policy
    }

    fun freqLabel(kHz: Long): String =
        if (kHz >= 1_000_000) {
            String.format(Locale.US, "%.2f GHz", kHz / 1_000_000.0)
        } else {
            String.format(Locale.US, "%d MHz", kHz / 1000)
        }
}