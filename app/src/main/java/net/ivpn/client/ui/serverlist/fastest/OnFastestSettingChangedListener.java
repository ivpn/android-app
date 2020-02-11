package net.ivpn.client.ui.serverlist.fastest;

import net.ivpn.client.rest.data.model.Server;

public interface OnFastestSettingChangedListener {
    void onFastestSettingItemChanged(Server server, boolean isSelected);

    void onAttemptRemoveLastServer();
}
