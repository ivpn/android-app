package net.ivpn.client.ui.connect;

import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.ui.dialog.Dialogs;

public interface ConnectionNavigator {

    void askConnectionPermission();

    void onAuthFailed();

    void onTimeOut();

    void notifyAnotherPortUsedToConnect();

    void logout();

    void openSessionLimitReachedDialogue();

    void accountVerificationFailed();

    void openNoNetworkDialog();

    void openErrorDialog(Dialogs dialogs);
}
