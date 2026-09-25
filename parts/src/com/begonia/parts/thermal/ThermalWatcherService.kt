package com.begonia.parts.thermal

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ThermalWatcherService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null
}