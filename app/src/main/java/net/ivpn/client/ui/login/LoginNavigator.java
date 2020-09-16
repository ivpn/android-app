package net.ivpn.client.ui.login;

import net.ivpn.client.ui.dialog.Dialogs;

public interface LoginNavigator {
    void onLogin();
    void onLoginWithInactiveAccount();
    void openSubscriptionScreen();
    void openSite();
    void openAccountNotActiveDialogue();
    void openSessionLimitReachedDialogue();
    void openErrorDialogue(Dialogs dialogs);
    void openCustomErrorDialogue(String title, String message);
    void openAccountNotActiveBetaDialogue();
}
