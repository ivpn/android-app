package net.ivpn.core.common.prefs

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

import net.ivpn.core.common.dagger.ApplicationScope
import java.util.*
import javax.inject.Inject

@ApplicationScope
class PackagesPreference @Inject constructor(
        private val preference: Preference
) {

    companion object {
        private const val DISALLOWED_PACKAGES = "DISALLOWED_PACKAGES"
    }

    val disallowedPackages: Set<String>?
        get() {
            val sharedPreferences = preference.disallowedAppsSharedPreferences
            return sharedPreferences.getStringSet(DISALLOWED_PACKAGES, HashSet())
        }

    fun disallowPackage(packageName: String?) {
        val disallowedPackages = disallowedPackages
        if (packageName == null || disallowedPackages == null || disallowedPackages.contains(packageName)) {
            return
        }
        val newDisallowedPackages: MutableSet<String> = HashSet(disallowedPackages)
        newDisallowedPackages.add(packageName)
        val sharedPreferences = preference.disallowedAppsSharedPreferences
        sharedPreferences.edit()
                .putStringSet(DISALLOWED_PACKAGES, newDisallowedPackages)
                .apply()
    }

    fun disallowAllPackages(packages: Set<String>?) {
        val disallowedPackages: Set<String> = HashSet(packages)
        val sharedPreferences = preference.disallowedAppsSharedPreferences
        sharedPreferences.edit()
                .putStringSet(DISALLOWED_PACKAGES, disallowedPackages)
                .apply()
    }

    fun allowAllPackages() {
        val sharedPreferences = preference.disallowedAppsSharedPreferences
        sharedPreferences.edit()
                .putStringSet(DISALLOWED_PACKAGES, HashSet())
                .apply()
    }

    fun allowPackage(packageName: String) {
        val disallowedPackages = disallowedPackages
        if (disallowedPackages == null || !disallowedPackages.contains(packageName)) {
            return
        }
        val newDisallowedPackages: MutableSet<String> = HashSet(disallowedPackages)
        newDisallowedPackages.remove(packageName)
        val sharedPreferences = preference.disallowedAppsSharedPreferences
        sharedPreferences.edit()
                .putStringSet(DISALLOWED_PACKAGES, newDisallowedPackages)
                .apply()
    }
}