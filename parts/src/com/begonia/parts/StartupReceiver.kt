package com.begonia.parts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.begonia.parts.cpu.CpuTuner

class StartupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }
        CpuTuner.restore(context)

    }
}