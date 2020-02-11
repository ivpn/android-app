package net.ivpn.client.ui.serverlist;

import net.ivpn.client.rest.data.model.Server;

public interface ServersListNavigator {

    void onServerSelected(Server server, Server forbiddenServer);

    void onServerLongClick(Server server);

    void onFastestServerSelected();

    void onFastestServerSettings();
}
