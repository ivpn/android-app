package net.ivpn.client.vpn.controller;

import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.ui.connect.ConnectionState;
import net.ivpn.client.ui.dialog.Dialogs;

public interface VpnStateListener {
    void onConnectionStateChanged(ConnectionState state);
    void onAuthFailed();
    void onTimeTick(long millisUntilResumed);
    void onTimerFinish();
    void notifyAnotherPortUsedToConnect();
    void onTimeOut();
    void onFindingFastestServer();
    void onCheckSessionState();
    void onRegeneratingKeys();
    void onRegenerationSuccess();
    void onRegenerationError(Dialogs errorDialog);
    void notifyServerAsFastest(Server server);
    void notifyNoNetworkConnection();
}