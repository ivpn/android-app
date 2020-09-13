package net.ivpn.client.v2.network

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import net.ivpn.client.R
import net.ivpn.client.v2.network.saved.SavedNetworksFragment
import net.ivpn.client.v2.network.scanned.ScannedNetworksFragment
import net.ivpn.client.v2.serverlist.all.ServerListFragment
import net.ivpn.client.v2.serverlist.favourite.FavouriteServersListFragment

class NetworksPagerAdapter(
        private val context: Context,
        fragmentManager: FragmentManager
): FragmentStatePagerAdapter(fragmentManager) {

    private val ITEM_COUNT = 2

    private val registeredFragments = SparseArray<Fragment>()

    override fun getCount(): Int {
        return ITEM_COUNT
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) {
            context.getString(R.string.network_protection_scanned)
        } else context.getString(R.string.network_protection_saved)
    }

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            ScannedNetworksFragment()
        } else SavedNetworksFragment()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        registeredFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        registeredFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }

    fun cancel() {
        if (registeredFragments.size() != ITEM_COUNT) {
            return
        }
    }
}