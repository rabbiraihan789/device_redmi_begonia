package com.begonia.parts.thermal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settingslib.applications.ApplicationsState
import com.begonia.parts.R
import java.util.ArrayList
import java.util.Locale

class ThermalProfilesFragment : Fragment(), ApplicationsState.Callbacks {

    private lateinit var appState: ApplicationsState
    private lateinit var appSession: ApplicationsState.Session
    private lateinit var launcherFilter: LauncherAppsFilter
    private lateinit var appAdapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appState = ApplicationsState.getInstance(requireContext().applicationContext)
        appSession = appState.newSession(this)
        appSession.onResume()
        launcherFilter = LauncherAppsFilter(requireContext().packageManager)
        appAdapter = AppListAdapter(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.thermal_app_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<RecyclerView>(R.id.app_list)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = appAdapter
    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(getString(R.string.thermal_title))
        rebuild()
    }

    override fun onDestroy() {
        appSession.onPause()
        appSession.onDestroy()
        super.onDestroy()
    }

    private fun rebuild() {
        appSession.rebuild(launcherFilter, ApplicationsState.ALPHA_COMPARATOR)
    }

    override fun onRebuildComplete(entries: ArrayList<ApplicationsState.AppEntry>?) {
        if (entries != null) {
            appAdapter.setEntries(entries)
        }
    }

    override fun onPackageListChanged() {
        launcherFilter.refresh()
        rebuild()
    }

    override fun onLoadEntriesCompleted() = rebuild()

    override fun onRunningStateChanged(running: Boolean) = Unit
    override fun onPackageIconChanged() = Unit
    override fun onPackageSizeChanged(packageName: String?) = Unit
    override fun onAllSizesComputed() = Unit
    override fun onLauncherInfoChanged() = Unit

    private sealed class RowItem {
        data class Section(val letter: String) : RowItem()
        data class App(val entry: ApplicationsState.AppEntry) : RowItem()
    }

    private inner class AppListAdapter(context: Context) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        AdapterView.OnItemSelectedListener {

        private val modeOptions = ModeOptions(context)
        private val items: MutableList<RowItem> = mutableListOf()

        fun setEntries(entries: List<ApplicationsState.AppEntry>) {
            items.clear()
            var previousLetter: String? = null
            for (entry in entries) {
                val letter = sectionLetter(entry)
                if (letter != previousLetter) {
                    items.add(RowItem.Section(letter))
                    previousLetter = letter
                }
                items.add(RowItem.App(entry))
            }
            notifyDataSetChanged()
        }

        private fun sectionLetter(entry: ApplicationsState.AppEntry): String {
            val label = entry.label ?: return ""
            if (label.isEmpty()) return ""
            return label.substring(0, 1).uppercase(Locale.ROOT)
        }

        override fun getItemCount(): Int = items.size

        override fun getItemViewType(position: Int): Int =
            if (items[position] is RowItem.Section) VIEW_SECTION else VIEW_APP

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == VIEW_SECTION) {
                SectionHolder(inflater.inflate(R.layout.thermal_section_header, parent, false))
            } else {
                AppHolder(inflater.inflate(R.layout.thermal_app_row, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            when (item) {
                is RowItem.Section -> (holder as SectionHolder).letter.text = item.letter
                is RowItem.App -> bindApp(holder as AppHolder, item.entry)
            }
        }

        private fun bindApp(holder: AppHolder, entry: ApplicationsState.AppEntry) {
            val context = holder.itemView.context
            appState.ensureIcon(entry)
            holder.icon.setImageDrawable(entry.icon)
            holder.label.text = entry.label

            val mode = ThermalProfiles.boundMode(context, entry.info.packageName)
            holder.mode.adapter = modeOptions
            holder.mode.tag = entry

            holder.mode.setSelection(mode, false)
            holder.mode.onItemSelectedListener = this
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val spinner = parent ?: return
            val entry = spinner.tag as? ApplicationsState.AppEntry ?: return
            val context = spinner.context
            if (ThermalProfiles.boundMode(context, entry.info.packageName) != position) {
                ThermalProfiles.bindPackage(context, entry.info.packageName, position)
                ThermalProfiles.applyPackage(context, entry.info.packageName)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }

    private class AppHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val label: TextView = view.findViewById(R.id.app_label)
        val mode: Spinner = view.findViewById(R.id.app_mode)
    }

    private class SectionHolder(view: View) : RecyclerView.ViewHolder(view) {
        val letter: TextView = view.findViewById(R.id.section_letter)
    }

    private class LauncherAppsFilter(packageManager: PackageManager) :
        ApplicationsState.AppFilter {

        private val packageManager = packageManager
        private val launcherPackages: MutableSet<String> = mutableSetOf()

        init {
            refresh()
        }

        fun refresh() {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolved: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
            launcherPackages.clear()
            resolved.forEach { info ->
                info.activityInfo?.packageName?.let { launcherPackages.add(it) }
            }
        }

        override fun `init`() = Unit

        override fun filterApp(entry: ApplicationsState.AppEntry): Boolean =
            launcherPackages.contains(entry.info.packageName)
    }

    companion object {
        private const val VIEW_SECTION = 0
        private const val VIEW_APP = 1
    }
}

private class ModeOptions(context: Context) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)
    private val labels: List<CharSequence> = listOf(
        context.getString(R.string.mode_default),
        context.getString(R.string.mode_benchmark),
        context.getString(R.string.mode_browser),
        context.getString(R.string.mode_camera),
        context.getString(R.string.mode_dialer),
        context.getString(R.string.mode_gaming),
        context.getString(R.string.mode_streaming)
    )

    override fun getCount(): Int = labels.size

    override fun getItem(position: Int): Any? = labels[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val text = convertView as? TextView
            ?: inflater.inflate(android.R.layout.simple_spinner_item, parent, false) as TextView
        text.text = labels[position]
        return text
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val text = convertView as? TextView
            ?: inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false) as TextView
        text.text = labels[position]
        return text
    }
}