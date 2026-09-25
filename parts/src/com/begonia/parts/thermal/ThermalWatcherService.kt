package com.begonia.parts.thermal

import android.app.ActivityTaskManager
import android.app.TaskStackListener
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.RemoteException
import android.util.Log

class ThermalWatcherService : Service() {

    private var foregroundPackage: String? = null

    private val resetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            foregroundPackage = null
            ThermalProfiles.resetKernelState()
        }
    }

    private val taskListener = object : TaskStackListener() {
        override fun onTaskStackChanged() {
            try {
                val info = ActivityTaskManager.getService().focusedRootTaskInfo ?: return
                val top = info.topActivity ?: return
                val packageName = top.packageName
                if (packageName != foregroundPackage) {
                    foregroundPackage = packageName
                    ThermalProfiles.applyPackage(this@ThermalWatcherService, packageName)
                }
            } catch (e: RemoteException) {
                Log.w(TAG, "Task stack lookup failed", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            ActivityTaskManager.getService().registerTaskStackListener(taskListener)
        } catch (e: RemoteException) {
            Log.w(TAG, "Unable to register task listener", e)
        }
        registerReceiver(resetReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }, Context.RECEIVER_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        try {
            ActivityTaskManager.getService().unregisterTaskStackListener(taskListener)
        } catch (e: RemoteException) {
            Log.w(TAG, "Unable to unregister task listener", e)
        }
        unregisterReceiver(resetReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "ThermalWatcher"
    }
}