package net.ivpn.core.v2.map.dialogue

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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import net.ivpn.core.R
import net.ivpn.core.databinding.ViewServerLocationBinding
import net.ivpn.core.rest.data.model.ServerLocation

class ServerLocationDialogueAdapter(
        private val context: Context,
        private val locations: ArrayList<ServerLocation>
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding: ViewServerLocationBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.view_server_location, parent, false
        )
        return LocationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is LocationViewHolder) {
            holder.bind(locations[position])
        }
    }

    class LocationViewHolder(
            val binding: ViewServerLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: ServerLocation) {
            binding.location = location
        }
    }
}