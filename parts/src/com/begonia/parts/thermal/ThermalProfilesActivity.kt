package com.begonia.parts.thermal

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity
import com.android.settingslib.collapsingtoolbar.R as ToolbarR

class ThermalProfilesActivity : CollapsingToolbarBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(ToolbarR.id.content_frame, ThermalProfilesFragment())
                .commit()
        }
    }
}