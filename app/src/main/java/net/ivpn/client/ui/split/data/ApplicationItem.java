package net.ivpn.client.ui.split.data;

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

import android.graphics.drawable.Drawable;

import java.util.Comparator;

public class ApplicationItem {
    private String applicationName;
    private String packageName;
    private Drawable icon;
    private boolean isAllowed;

    public static Comparator<ApplicationItem> comparator = (item1, item2) -> item1.getApplicationName().toLowerCase().compareTo(item2.getApplicationName().toLowerCase());

    public ApplicationItem(String applicationName, String packageName, Drawable icon) {
        this.applicationName = applicationName;
        this.icon = icon;
        this.packageName = packageName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public void setAllowed(boolean allowed) {
        isAllowed = allowed;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ApplicationItem)) {
            return false;
        }
        ApplicationItem item = (ApplicationItem) obj;
        return this.packageName.equals(item.packageName);
    }
}
