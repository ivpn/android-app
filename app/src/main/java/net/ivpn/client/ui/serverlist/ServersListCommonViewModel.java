package net.ivpn.client.ui.serverlist;

import android.content.Context;

import androidx.databinding.ObservableField;

import net.ivpn.client.R;
import net.ivpn.client.common.pinger.PingProvider;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.prefs.Settings;

import javax.inject.Inject;

public class ServersListCommonViewModel {

    public final ObservableField<String> title = new ObservableField<>();

    private Settings settings;
    private PingProvider pingProvider;

    @Inject
    ServersListCommonViewModel(Settings settings,
                               PingProvider pingProvider) {
        this.settings = settings;
        this.pingProvider = pingProvider;
    }

    public void onResume() {
        pingProvider.pingAll(false);
    }

    public void start(Context context, ServerType serverType) {
        boolean isMultiHopEnabled = isMultiHopEnabled();
        String titleStr;
        if (isMultiHopEnabled) {
            if (serverType.equals(ServerType.ENTRY)) {
                titleStr = context.getString(R.string.servers_list_title_entry);
            } else {
                titleStr = context.getString(R.string.servers_list_title_exit);
            }
        } else {
            titleStr = context.getString(R.string.servers_list_title);
        }
        title.set(titleStr);
    }

    private boolean isMultiHopEnabled() {
        return settings.isMultiHopEnabled();
    }
}