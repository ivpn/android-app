package net.ivpn.core.v2.viewmodel;

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

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import net.ivpn.core.common.prefs.PackagesPreference;
import net.ivpn.core.v2.splittunneling.OnApplicationItemSelectionChangedListener;
import net.ivpn.core.v2.splittunneling.SplitTunnelingRecyclerViewAdapter;
import net.ivpn.core.v2.splittunneling.items.ApplicationItem;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class SplitTunnelingViewModel {

    public final ObservableBoolean dataLoading = new ObservableBoolean();
    public final ObservableBoolean isAllItemsAllowed = new ObservableBoolean();
    public final ObservableArrayList<ApplicationItem> apps = new ObservableArrayList<>();
    public final ObservableArrayList<String> disallowedApps = new ObservableArrayList<String>();
    public final ObservableField<SplitTunnelingRecyclerViewAdapter> adapter = new ObservableField<>();
    public final OnApplicationItemSelectionChangedListener selectionChangedListener = new OnApplicationItemSelectionChangedListener() {
        @Override
        public void onApplicationItemSelectionChanged(ApplicationItem applicationItem, boolean isSelected) {
            if (isSelected) {
                allowPackage(applicationItem.getPackageName());
            } else {
                disallowPackage(applicationItem.getPackageName());
            }
        }

        @Override
        public void onItemsSelectionStateChanged(boolean isAllItemSelected) {
            SplitTunnelingViewModel.this.isAllItemsAllowed.set(isAllItemSelected);
        }
    };

    private SplitTunnelingRecyclerViewAdapter.MenuHandler menuHandler;
    private PackagesPreference preference;

    @Inject
    SplitTunnelingViewModel(SplitTunnelingRecyclerViewAdapter adapter, PackagesPreference preference) {
        this.adapter.set(adapter);
        this.menuHandler = adapter.getMenuHandler();
        this.preference = preference;

        disallowedApps.clear();
        disallowedApps.addAll(getDisallowedPackages());

        isAllItemsAllowed.set(disallowedApps.size() == 0);
    }

    public void getApplicationsList(PackageManager packageManager) {
        new InflateApplicationInfoAsyncTask(packageManager).execute();
    }

    public void selectAll() {
        allowAllPackages();
        menuHandler.selectAll();
    }

    public void deselectAll() {
        disallowAllApps(new HashSet<>(apps));
        menuHandler.deselectAll();
    }

    private void disallowAllApps(Set<ApplicationItem> applicationItems) {
        Set<String> disallowedPackages = new HashSet<>();
        for (ApplicationItem app : applicationItems) {
            disallowedPackages.add(app.getPackageName());
        }
        disallowAllPackages(disallowedPackages);
    }

    void allowAllPackages() {
        preference.allowAllPackages();
    }

    private void disallowAllPackages(Set<String> packages) {
        preference.disallowAllPackages(packages);
    }

    private void allowPackage(String packageName) {
        preference.allowPackage(packageName);
    }

    private void disallowPackage(String packageName) {
        preference.disallowPackage(packageName);
    }

    private Set<String> getDisallowedPackages() {
        return preference.getDisallowedPackages();
    }

    private class InflateApplicationInfoAsyncTask extends AsyncTask<Void, Void, List<ApplicationItem>> {

        private List<ApplicationInfo> applicationInfoList;
        private PackageManager packageManager;

        InflateApplicationInfoAsyncTask(PackageManager packageManager) {
            this.packageManager = packageManager;
            try {
                this.applicationInfoList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dataLoading.set(true);
        }

        @Override
        protected List<ApplicationItem> doInBackground(Void... voids) {
            List<ApplicationItem> items = new LinkedList<>();
            for (ApplicationInfo info : applicationInfoList) {
                try {
                    if (null != packageManager.getLaunchIntentForPackage(info.packageName) ||
                            null != packageManager.getLeanbackLaunchIntentForPackage(info.packageName) ||
                            null != packageManager.getInstallerPackageName(info.packageName)
                    ) {
                        items.add(new ApplicationItem(info.loadLabel(packageManager).toString(), info.packageName,
                                info.loadIcon(packageManager)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<ApplicationItem> applicationItems) {
            apps.clear();
            apps.addAll(applicationItems);
            dataLoading.set(false);
        }
    }
}