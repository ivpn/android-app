package net.ivpn.client.common.pinger;

import net.ivpn.client.rest.data.model.Server;

public interface OnPingFinishListener {
    void onPingFinish(Server server, PingResultFormatter status);
}
