package net.ivpn.client.common.prefs;

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
