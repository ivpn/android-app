package net.ivpn.client.common.prefs;

import net.ivpn.client.rest.data.model.Server;

import java.util.List;

public interface OnServerListUpdatedListener {
    void onSuccess(List<Server> servers, boolean isForced);

    void onError(Throwable throwable);

    void onError();
}
