package net.ivpn.client.ui.privateemails.edit;

import net.ivpn.client.ui.dialog.Dialogs;

public interface EditPrivateEmailNavigator {
    void toEmailsList();
    void tryRemoveEmail();
    void openErrorDialogue(Dialogs dialogs);
}
