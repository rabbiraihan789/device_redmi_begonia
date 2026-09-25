package com.begonia.parts.thermal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.begonia.parts.R

class ThermalProfilesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.thermal_profiles_stub, container, false)

    override fun onResume() {
        super.onResume()
        activity?.setTitle(getString(R.string.thermal_title))
    }
}