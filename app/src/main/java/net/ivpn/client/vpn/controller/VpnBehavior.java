package net.ivpn.client.vpn.controller;

public interface VpnBehavior {

    void pause(long pauseDuration);

    void resume();

    void stop();

    void startConnecting();

    void startConnecting(boolean force);

    void disconnect();

    void actionByUser();

    void reconnect();

    void regenerateKeys();

    void addStateListener(VpnStateListener vpnStateListener);

    void removeStateListener(VpnStateListener vpnStateListener);

    void destroy();

    void notifyVpnState();

    long getConnectionTime();
}
