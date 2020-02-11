package net.ivpn.client.ui.split;

import net.ivpn.client.ui.split.data.ApplicationItem;

public interface OnApplicationItemSelectionChangedListener {
    void onApplicationItemSelectionChanged(ApplicationItem applicationItem, boolean isSelected);
    void onItemsSelectionStateChanged(boolean isAllItemSelected);
}