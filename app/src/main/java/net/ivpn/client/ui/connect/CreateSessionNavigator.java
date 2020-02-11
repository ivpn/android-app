package net.ivpn.client.ui.connect;

public interface CreateSessionNavigator {

    void onForceLogout();

    void tryAgain();

    void cancel();
}