package net.ivpn.core.v2.splittunneling.holder

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

import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import net.ivpn.core.databinding.ApplicationItemBinding
import net.ivpn.core.v2.splittunneling.OnApplicationItemAction
import net.ivpn.core.v2.splittunneling.items.ApplicationItem

class ApplicationInfoViewHolder(
        private val binding: ApplicationItemBinding,
        private val listener: OnApplicationItemAction
) : RecyclerView.ViewHolder(binding.root), CompoundButton.OnCheckedChangeListener {

    private var applicationItem: ApplicationItem? = null

    fun bind(applicationItem: ApplicationItem) {
        this.applicationItem = applicationItem
        binding.application = applicationItem
        binding.checkbox.isChecked = applicationItem.isAllowed
        binding.checkbox.setOnCheckedChangeListener(this)
        binding.executePendingBindings()
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isSelected: Boolean) {
        applicationItem?.let {
            it.isAllowed = isSelected
            listener.onApplicationStateChanged(it, isSelected)
        }
    }
}