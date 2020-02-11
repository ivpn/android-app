package net.ivpn.client.common.pinger;

import net.ivpn.client.rest.data.model.Server;

public interface OnFastestServerDetectorListener {
    void onFastestServerDetected(Server server);

    void onDefaultServerApplied(Server server);
}
