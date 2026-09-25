package com.begonia.parts.cpu

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.android.settingslib.collapsingtoolbar.BasePreferencesFragment
import com.begonia.parts.R
import com.begonia.parts.util.KernelNode
import kotlin.math.abs

class CpuFragment : BasePreferencesFragment() {

    private val sysfsBase = "/sys/devices/system/cpu/cpufreq"

    override fun getTitle(): CharSequence = getString(R.string.cpu_title)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val screen = preferenceManager.createPreferenceScreen(requireContext())
        addGovernorSection(screen)
        addClusterSections(screen)
        preferenceScreen = screen
    }

    private fun addGovernorSection(screen: PreferenceScreen) {
        val governors = CpuTuner.availableGovernors()
        val category = PreferenceCategory(requireContext()).apply {
            title = getString(R.string.cpu_governor_title)
        }

        screen.addPreference(category)
        val governor = ListPreference(requireContext()).apply {
            key = CpuTuner.KEY_GOVERNOR
            title = getString(R.string.cpu_governor_title)
            dialogTitle = getString(R.string.cpu_governor_dialog)
            entries = governors.map { CpuTuner.governorLabel(it) }.toTypedArray()
            entryValues = governors.toTypedArray()
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            value = CpuTuner.currentGovernor() ?: governors.firstOrNull()
            setOnPreferenceChangeListener { _, newValue ->
                val chosen = newValue as String
                for (policy in CpuTuner.policies()) {
                    CpuTuner.applyGovernor(policy, chosen)
                }
                if (chosen == "userspace") {
                    syncUserspaceSpeed()
                }
                true
            }
        }
        category.addPreference(governor)
    }

    private fun addClusterSections(screen: PreferenceScreen) {
        val policies = CpuTuner.policies()
        if (policies.isEmpty()) {
            screen.addPreference(Preference(requireContext()).apply {
                title = getString(R.string.cpu_no_policies)
                isSelectable = false
            })
            return
        }
        for (policy in policies) {
            val frequencies = CpuTuner.frequencies(policy)
            if (frequencies.isEmpty()) continue

            val category = PreferenceCategory(requireContext()).apply {
                title = CpuTuner.clusterLabel(requireContext(), policy)
            }
            screen.addPreference(category)
            category.addPreference(frequencyPreference(policy, frequencies, isFloor = true))
            category.addPreference(frequencyPreference(policy, frequencies, isFloor = false))
        }
    }

    private fun frequencyPreference(
        policy: String,
        frequencies: LongArray,
        isFloor: Boolean
    ): ListPreference {
        val prefs = CpuTuner.prefs(requireContext())
        val sysfsNode = if (isFloor) "scaling_min_freq" else "scaling_max_freq"
        val persisted = prefs.getLong(
            if (isFloor) CpuTuner.floorKey(policy) else CpuTuner.ceilingKey(policy),
            -1L
        )
        val sysfsCurrent = KernelNode.readLong("$sysfsBase/$policy/$sysfsNode")
        val target = when {
            persisted > 0 -> persisted
            sysfsCurrent > 0 -> sysfsCurrent
            else -> frequencies[0]
        }
        val snapped = frequencies[nearestIndex(frequencies, target)]

        return ListPreference(requireContext()).apply {
            key = if (isFloor) CpuTuner.floorKey(policy) else CpuTuner.ceilingKey(policy)
            title = getString(if (isFloor) R.string.cpu_min_title else R.string.cpu_max_title)
            dialogTitle =
                getString(if (isFloor) R.string.cpu_min_dialog else R.string.cpu_max_dialog)
            entries = frequencies.map { CpuTuner.freqLabel(it) }.toTypedArray()
            entryValues = frequencies.map { it.toString() }.toTypedArray()
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            value = snapped.toString()
            setOnPreferenceChangeListener { _, newValue ->
                val chosen = (newValue as String).toLong()
                KernelNode.write("$sysfsBase/$policy/$sysfsNode", chosen)
                if (!isFloor) {
                    val governor = CpuTuner.prefs(requireContext())
                        .getString(CpuTuner.KEY_GOVERNOR, "")
                    if (governor == "userspace") {
                        KernelNode.write("$sysfsBase/$policy/scaling_setspeed", chosen)
                    }
                }
                true
            }
        }
    }

    private fun syncUserspaceSpeed() {
        val prefs = CpuTuner.prefs(requireContext())
        for (policy in CpuTuner.policies()) {
            var target = prefs.getLong(CpuTuner.ceilingKey(policy), -1L)
            if (target <= 0) target = KernelNode.readLong("$sysfsBase/$policy/scaling_max_freq")
            if (target > 0) KernelNode.write("$sysfsBase/$policy/scaling_setspeed", target)
        }
    }

    private fun nearestIndex(frequencies: LongArray, target: Long): Int {
        var best = 0
        var bestDiff = Long.MAX_VALUE
        for (i in frequencies.indices) {
            val diff = abs(frequencies[i] - target)
            if (diff < bestDiff) {
                bestDiff = diff
                best = i
            }
        }
        return best
    }
}