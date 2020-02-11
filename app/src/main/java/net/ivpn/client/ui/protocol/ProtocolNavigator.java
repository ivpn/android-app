package net.ivpn.client.ui.protocol;

import android.view.View;

import net.ivpn.client.ui.dialog.Dialogs;

public interface ProtocolNavigator {

    void notifyUser(int msgId, int actionId, View.OnClickListener listener);

    void openDialogueError(Dialogs dialog);

    void openCustomDialogueError(String title, String message);

}
