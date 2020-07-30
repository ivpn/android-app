package net.ivpn.client.v2.map.dialogue

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import net.ivpn.client.R
import net.ivpn.client.databinding.ViewServerLocationBinding
import net.ivpn.client.rest.data.model.ServerLocation

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