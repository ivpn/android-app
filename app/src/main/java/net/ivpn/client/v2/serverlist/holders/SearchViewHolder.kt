package net.ivpn.client.v2.serverlist.holders

import android.widget.Filter
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import net.ivpn.client.databinding.SearchItemBinding

class SearchViewHolder(
        binding: SearchItemBinding,
        filter: Filter
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.search.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(query: String): Boolean {
                        filter.filter(query)
                        return false
                    }

                })

        binding.executePendingBindings()
    }
}