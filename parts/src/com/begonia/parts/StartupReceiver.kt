package com.begonia.parts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.begonia.parts.cpu.CpuTuner
import com.begonia.parts.thermal.ThermalProfiles
import com.begonia.parts.util.Haptics

class StartupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }
        CpuTuner.restore(context)
        Haptics.restore(context)

        ThermalProfiles.startWatcher(context)
    }
}
