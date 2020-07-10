package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;

import android.widget.LinearLayout;

import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.rest.data.privateemails.Email;
import net.ivpn.client.ui.privateemails.PrivateEmailsNavigator;
import net.ivpn.client.ui.serverlist.AdapterListener;

public class GestureBindingAdapter {

    @BindingAdapter({"onLongClick", "server"})
    public static void setOnLongClickListener(LinearLayout layout,
                                              final AdapterListener navigator, final Server server) {
        layout.setOnLongClickListener(view -> {
            navigator.onServerLongClick(server);
            return true;
        });
    }

    @BindingAdapter({"onLongClick", "email"})
    public static void setOnLongClickListener(LinearLayout layout,
                                              final PrivateEmailsNavigator navigator, final Email email) {
        layout.setOnLongClickListener(view -> {
            navigator.copyToClipboardEmail(email);
            return true;
        });
    }
}
