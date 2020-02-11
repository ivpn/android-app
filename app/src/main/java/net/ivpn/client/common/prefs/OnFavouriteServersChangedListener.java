package net.ivpn.client.common.prefs;

import net.ivpn.client.rest.data.model.Server;

public interface OnFavouriteServersChangedListener {

    void notifyFavouriteServerAdded(Server server);

    void notifyFavouriteServerRemoved(Server server);

}
