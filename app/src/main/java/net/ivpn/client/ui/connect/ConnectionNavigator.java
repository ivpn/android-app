package net.ivpn.client.ui.connect;

import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.ui.dialog.Dialogs;

interface ConnectionNavigator {

    void openSettings();

    void openPrivateEmails();

    void chooseServer(ServerType serverType);

    void openInfoDialogue();

    void onAuthFailed();

    void onTimeOut();

    void notifyAnotherPortUsedToConnect();

    void logout();

    void openSessionLimitReachedDialogue();

    void accountVerificationFailed();

    void onConnectionStateChanged(ConnectionState state);

    void openNoNetworkDialog();

    void openErrorDialog(Dialogs dialogs);
}
