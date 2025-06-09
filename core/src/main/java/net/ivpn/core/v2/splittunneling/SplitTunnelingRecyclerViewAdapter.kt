package net.ivpn.core.v2.splittunneling

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.ivpn.core.R
import net.ivpn.core.databinding.ApplicationItemBinding
import net.ivpn.core.databinding.AppsSearchItemBinding
import net.ivpn.core.v2.splittunneling.holder.ApplicationInfoViewHolder
import net.ivpn.core.v2.splittunneling.holder.AppsSearchViewHolder
import net.ivpn.core.v2.splittunneling.items.ApplicationItem
import net.ivpn.core.v2.splittunneling.items.SearchItem
import net.ivpn.core.v2.splittunneling.items.SplitTunnelingItem
import java.util.*
import javax.inject.Inject

class SplitTunnelingRecyclerViewAdapter @Inject internal constructor()
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allApps: ArrayList<ApplicationItem> = ArrayList()
    private var disallowedApps: MutableSet<String> = HashSet()

    private val bindings: MutableList<ApplicationItemBinding> = LinkedList()
    private var searchBinding: AppsSearchItemBinding? = null

    private var listener: OnApplicationItemSelectionChangedListener? = null
    private val itemListener = getApplicationItemActionListener()

    private var isFiltering = false
    private var filteredApps = arrayListOf<SplitTunnelingItem>()

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> SEARCH_ITEM
            else -> APP_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SEARCH_ITEM -> {
                val binding = AppsSearchItemBinding.inflate(layoutInflater, parent, false)
                AppsSearchViewHolder(binding, searchFilter)
            }
            APP_ITEM -> {
                val binding = ApplicationItemBinding.inflate(layoutInflater, parent, false)
                bindings.add(binding)
                ApplicationInfoViewHolder(binding, itemListener)
            }
            else -> {
                DescriptionViewHolder(layoutInflater.inflate(R.layout.description_item, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ApplicationInfoViewHolder) {
            val app = filteredApps[position]
            if (app is ApplicationItem) {
                holder.bind(app)
            }
        } else if (holder is AppsSearchViewHolder) {
            searchBinding = holder.binding
        }
    }

    override fun getItemCount(): Int {
        return filteredApps.size
    }

    fun setSelectionChangedListener(listener: OnApplicationItemSelectionChangedListener?) {
        this.listener = listener
    }

    val menuHandler: MenuHandler
        get() = MenuHandler()

    fun setApps(allApps: ArrayList<ApplicationItem>, disallowedApps: ArrayList<String>) {
        this.allApps = ArrayList(allApps)
        this.disallowedApps = HashSet(disallowedApps)
        for (app in allApps) {
            app.isAllowed = !disallowedApps.contains(app.packageName)
        }
        Collections.sort(this.allApps, ApplicationItem.comparator)
        searchBinding?.search?.let {
            searchFilter.filter(it.query)
        } ?: run {
            filteredApps = prepareDataToShow(this.allApps)
            notifyDataSetChanged()
        }
        listener?.onItemsSelectionStateChanged(disallowedApps.isEmpty())
    }

    private fun getApplicationItemActionListener(): OnApplicationItemAction {
        return object : OnApplicationItemAction {
            override fun onApplicationStateChanged(item: ApplicationItem, isAllowed: Boolean) {
                if (isAllowed) {
                    disallowedApps.remove(item.packageName)
                } else {
                    disallowedApps.add(item.packageName)
                }
                listener?.onItemsSelectionStateChanged(disallowedApps.size == 0)
                listener?.onApplicationItemSelectionChanged(item, isAllowed)
            }
        }
    }

    private fun selectAll() {
        disallowedApps = HashSet()
        for (app in allApps) {
            app.isAllowed = true
        }
        listener?.onItemsSelectionStateChanged(true)
        //Better alternative than notifyItemRangeChanged for our case;
        setSelections(true)
    }

    private fun deselectAll() {
        disallowedApps = HashSet()
        for (app in allApps) {
            app.isAllowed = false
            disallowedApps.add(app.packageName)
        }
        listener?.onItemsSelectionStateChanged(false)
        //Better alternative than notifyItemRangeChanged for our case;
        setSelections(false)
    }

    private fun setSelections(isSelected: Boolean) {
        for (binding in bindings) {
            binding.checkbox.isChecked = isSelected
        }
    }

    private val searchFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            val filteredList = ArrayList<ApplicationItem>()

            if (constraint == null || constraint.isEmpty()) {
                isFiltering = false
                filteredList.addAll(allApps)
            } else {
                isFiltering = true
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                for (app in allApps) {
                    if (app.applicationName.lowercase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(app)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            if (results.values is List<*>) {
                val oldList = filteredApps
                filteredApps = prepareDataToShow(results.values as ArrayList<ApplicationItem>)
                notifyChanges(oldList, filteredApps)
            }
        }
    }

    fun notifyChanges(oldList: List<SplitTunnelingItem>, newList: List<SplitTunnelingItem>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun getOldListSize() = oldList.size
            override fun getNewListSize() = newList.size
        })
        diff.dispatchUpdatesTo(this)
    }

    private fun prepareDataToShow(apps: ArrayList<ApplicationItem>): ArrayList<SplitTunnelingItem> {
        val listToShow = ArrayList<SplitTunnelingItem>()

        listToShow.add(SearchItem())
        listToShow.addAll(apps)

        return listToShow
    }

    internal inner class DescriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class MenuHandler {
        fun selectAll() {
            this@SplitTunnelingRecyclerViewAdapter.selectAll()
        }

        fun deselectAll() {
            this@SplitTunnelingRecyclerViewAdapter.deselectAll()
        }
    }

    companion object {
        private const val APP_ITEM = 0
        private const val SEARCH_ITEM = 1
    }
}