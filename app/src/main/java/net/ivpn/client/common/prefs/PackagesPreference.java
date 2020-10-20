package net.ivpn.client.common.prefs;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class PackagesPreference {

    private static final String DISALLOWED_PACKAGES = "DISALLOWED_PACKAGES";

    private Preference preference;

    @Inject
    public PackagesPreference(Preference preference) {
        this.preference = preference;
    }

    public Set<String> getDisallowedPackages() {
        SharedPreferences sharedPreferences = preference.getDisallowedAppsSharedPreferences();
        return sharedPreferences.getStringSet(DISALLOWED_PACKAGES, new HashSet<>());
    }

    public void disallowPackage(String packageName) {
        Set<String> disallowedPackages = getDisallowedPackages();
        if (packageName == null || disallowedPackages.contains(packageName)) {
            return;
        }
        Set<String> newDisallowedPackages = new HashSet<>(disallowedPackages);
        newDisallowedPackages.add(packageName);

        SharedPreferences sharedPreferences = preference.getDisallowedAppsSharedPreferences();
        sharedPreferences.edit()
                .putStringSet(DISALLOWED_PACKAGES, newDisallowedPackages)
                .apply();
    }

    public void disallowAllPackages(Set<String> packages) {
        Set<String> disallowedPackages = new HashSet<>(packages);

        SharedPreferences sharedPreferences = preference.getDisallowedAppsSharedPreferences();
        sharedPreferences.edit()
                .putStringSet(DISALLOWED_PACKAGES, disallowedPackages)
                .apply();
    }

    public void allowAllPackages() {
        SharedPreferences sharedPreferences = preference.getDisallowedAppsSharedPreferences();
        sharedPreferences.edit()
                .putStringSet(DISALLOWED_PACKAGES, new HashSet<String>())
                .apply();
    }

    public void allowPackage(String packageName) {
        Set<String> disallowedPackages = getDisallowedPackages();
        if (!disallowedPackages.contains(packageName)) {
            return;
        }
        Set<String> newDisallowedPackages = new HashSet<>(disallowedPackages);
        newDisallowedPackages.remove(packageName);

        SharedPreferences sharedPreferences = preference.getDisallowedAppsSharedPreferences();
        sharedPreferences.edit()
                .putStringSet(DISALLOWED_PACKAGES, newDisallowedPackages)
                .apply();
    }
}
