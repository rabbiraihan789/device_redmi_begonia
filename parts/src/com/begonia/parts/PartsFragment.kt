package com.begonia.parts

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.BasePreferencesFragment

class PartsFragment : BasePreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.parts_dashboard, rootKey)
    }

    override fun getTitle(): CharSequence = getString(R.string.app_name)
}