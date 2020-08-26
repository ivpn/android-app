package net.ivpn.client.ui.serverlist;

import net.ivpn.client.rest.data.model.Server;

public interface AdapterListener {

    void onServerSelected(Server server, Server forbiddenServer);

    void onServerLongClick(Server server);

    void onFastestServerSelected();

    void onFastestServerSettingsClick();

    void onRandomServerSelected();

    void changeFavouriteStateFor(Server server, boolean isFavourite);
}
