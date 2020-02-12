package net.ivpn.client.ui.serverlist;

import android.content.Context;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import net.ivpn.client.R;
import net.ivpn.client.common.pinger.PingProvider;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.rest.data.model.Server;

import java.util.List;

import javax.inject.Inject;

public class ServersListCommonViewModel {

    public final ObservableBoolean isFavouriteServersListEmpty = new ObservableBoolean();
    public final ObservableField<String> title = new ObservableField<>();

    private Settings settings;
    private ServersRepository serversRepository;
    private PingProvider pingProvider;

    @Inject
    ServersListCommonViewModel(ServersRepository serversRepository, Settings settings,
                               PingProvider pingProvider) {
        this.settings = settings;
        this.serversRepository = serversRepository;
        this.pingProvider = pingProvider;
    }

    void onResume() {
        pingProvider.pingAll(false);
    }

    void start(Context context, ServerType serverType) {
        isFavouriteServersListEmpty.set(getFavouriteServersList().isEmpty());
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

    private List<Server> getFavouriteServersList() {
        return serversRepository.getFavouritesServers();
    }

    private boolean isMultiHopEnabled() {
        return settings.isMultiHopEnabled();
    }
}