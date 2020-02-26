package net.ivpn.client.ui.login;

import net.ivpn.client.ui.dialog.Dialogs;

public interface LoginNavigator {
    void onLogin();
    void openSubscriptionScreen();
    void openSite();
    void openSessionLimitReachedDialogue();
    void openErrorDialogue(Dialogs dialogs);
    void openCustomErrorDialogue(String title, String message);
}
