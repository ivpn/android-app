package net.ivpn.core.v2.serverlist.holders

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Tamim Hossain.
 Copyright (c) 2025 IVPN Limited.

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

import androidx.recyclerview.widget.RecyclerView
import net.ivpn.core.R
import net.ivpn.core.databinding.HostItemBinding
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.v2.serverlist.AdapterListener
import net.ivpn.core.v2.serverlist.items.HostItem

class HostViewHolder(
    val binding: HostItemBinding,
    val navigator: AdapterListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(hostItem: HostItem, forbiddenServer: Server?, isFavouritesEntry: Boolean = false) {
        binding.hostItem = hostItem
        binding.navigator = navigator
        binding.isFavouritesEntry = isFavouritesEntry

        binding.star.setImageResource(if (hostItem.isFavourite) R.drawable.ic_star_on else R.drawable.ic_star_off)
        
        binding.starLayout.setOnClickListener {
            hostItem.isFavourite = !hostItem.isFavourite
            binding.star.setImageResource(if (hostItem.isFavourite) R.drawable.ic_star_on else R.drawable.ic_star_off)
            navigator.changeFavouriteStateForHost(hostItem.host, hostItem.parentServer, hostItem.isFavourite)
        }

        binding.hostLayout.setOnClickListener {
            navigator.onHostSelected(hostItem.host, hostItem.parentServer, forbiddenServer)
        }

        binding.executePendingBindings()
    }
}

