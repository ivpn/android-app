package net.ivpn.client.ui.privateemails;

import net.ivpn.client.rest.data.privateemails.Email;
import net.ivpn.client.ui.dialog.Dialogs;

public interface PrivateEmailsNavigator {
    void copyToClipboardEmail(Email email);
    void editEmail(Email email);
    void onEmailAdded(Email email);
    void openErrorDialogue(Dialogs dialog);
    void openNewFeatureDialog(PrivateEmailActionListener listener);
}
