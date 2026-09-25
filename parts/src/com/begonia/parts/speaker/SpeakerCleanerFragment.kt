package com.begonia.parts.speaker

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.BasePreferencesFragment
import com.begonia.parts.R

class SpeakerCleanerFragment : BasePreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

    }

    override fun getTitle(): CharSequence = getString(R.string.speaker_title)
}