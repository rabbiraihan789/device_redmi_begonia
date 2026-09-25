package com.begonia.parts.cpu

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.BasePreferencesFragment
import com.begonia.parts.R

class CpuFragment : BasePreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

    }

    override fun getTitle(): CharSequence = getString(R.string.cpu_title)
}