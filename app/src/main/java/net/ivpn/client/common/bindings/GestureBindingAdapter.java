package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;
import android.view.View;
import android.widget.LinearLayout;

import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.rest.data.privateemails.Email;
import net.ivpn.client.ui.privateemails.PrivateEmailsNavigator;
import net.ivpn.client.ui.serverlist.ServersListNavigator;

public class GestureBindingAdapter {

    @BindingAdapter({"app:onLongClick", "app:server"})
    public static void setOnLongClickListener(LinearLayout layout,
                                              final ServersListNavigator navigator, final Server server) {
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                navigator.onServerLongClick(server);
                return true;
            }
        });
    }

    @BindingAdapter({"app:onLongClick", "app:email"})
    public static void setOnLongClickListener(LinearLayout layout,
                                              final PrivateEmailsNavigator navigator, final Email email) {
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                navigator.copyToClipboardEmail(email);
                return true;
            }
        });
    }
}
