package net.ivpn.client.ui.split.holder

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

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

import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import net.ivpn.client.databinding.ApplicationItemBinding
import net.ivpn.client.ui.split.OnApplicationItemAction
import net.ivpn.client.ui.split.items.ApplicationItem

class ApplicationInfoViewHolder(
        private val binding: ApplicationItemBinding,
        private val listener: OnApplicationItemAction
) : RecyclerView.ViewHolder(binding.root), CompoundButton.OnCheckedChangeListener {
//    View.OnClickListener

    private var applicationItem: ApplicationItem? = null

    fun bind(applicationItem: ApplicationItem) {
        this.applicationItem = applicationItem
        binding.application = applicationItem
//        val isChecked = !disallowedApps.contains(applicationItem.packageName)
        binding.checkbox.isChecked = applicationItem.isAllowed
        binding.checkbox.setOnCheckedChangeListener(this)
//        binding.contentLayout.setOnClickListener(this)
        binding.executePendingBindings()
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isSelected: Boolean) {
        applicationItem?.let {
            it.isAllowed = isSelected
            listener.onApplicationStateChanged(it, isSelected)
        }
    }

//    override fun onClick(view: View) {
//        val isNotAllowed = disallowedApps.contains(applicationItem!!.packageName)
//        binding.checkbox.isChecked = isNotAllowed
//    }
}