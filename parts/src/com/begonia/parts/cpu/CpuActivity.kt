package com.begonia.parts.cpu

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity
import com.android.settingslib.collapsingtoolbar.R as ToolbarR

class CpuActivity : CollapsingToolbarBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(ToolbarR.id.content_frame, CpuFragment())
                .commit()
        }
    }
}