package com.begonia.parts.speaker

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity
import com.android.settingslib.collapsingtoolbar.R as ToolbarR

class SpeakerCleanerActivity : CollapsingToolbarBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(ToolbarR.id.content_frame, SpeakerCleanerFragment())
                .commit()
        }
    }
}