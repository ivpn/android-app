package net.ivpn.core.v2.network

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

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import net.ivpn.core.R
import net.ivpn.core.v2.network.saved.SavedNetworksFragment
import net.ivpn.core.v2.network.scanned.ScannedNetworksFragment

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