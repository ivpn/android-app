package net.ivpn.client.ui.split;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import net.ivpn.client.common.prefs.PackagesPreference;
import net.ivpn.client.ui.split.data.ApplicationItem;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class SplitTunnelingViewModel {

    public final ObservableBoolean dataLoading = new ObservableBoolean();
    public final ObservableBoolean isAllItemsAllowed = new ObservableBoolean();
    public final ObservableList<ApplicationItem> apps = new ObservableArrayList<>();
    public final ObservableList<String> disallowedApps = new ObservableArrayList<String>();
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
//            refreshCommands();
        }
    };
//    public CommandVM selectAllCommand = new CommandVM() {
//        @Override
//        public void execute() {
//            allowAllPackages();
//            menuHandler.selectAll();
//        }
//
//        @Override
//        public void refresh() {
//            isEnabled(!isAllItemsAllowed);
//        }
//    };
//
//    public CommandVM deselectAllCommand = new CommandVM() {
//        @Override
//        public void execute() {
//            disallowAllApps(new HashSet<>(apps));
//            menuHandler.deselectAll();
//        }
//
//        @Override
//        public void refresh() {
//            isEnabled(isAllItemsAllowed);
//        }
//    };

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
//        updateMenuFlag();
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

    private void updateMenuFlag() {
//        isAllItemsAllowed = disallowedApps.size() == 0;
//        refreshCommands();
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
            this.applicationInfoList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
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
                    if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
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