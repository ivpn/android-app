package net.ivpn.client.common.bindings;

import android.widget.LinearLayout;

import androidx.databinding.BindingAdapter;

import net.ivpn.client.rest.data.model.Server;
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
}
