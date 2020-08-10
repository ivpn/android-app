package net.ivpn.client.common.dagger;

import com.wireguard.android.backend.WireGuardUiService;

import net.ivpn.client.common.shortcuts.ConnectionShortcutsActivity;
import net.ivpn.client.ui.billing.BillingActivity;
import net.ivpn.client.ui.customdns.DialogueCustomDNSViewModel;
import net.ivpn.client.ui.subscription.SubscriptionActivity;
import net.ivpn.client.ui.subscription.monthly.MonthlySubscriptionFragment;
import net.ivpn.client.ui.subscription.yearly.YearlySubscriptionFragment;
import net.ivpn.client.ui.timepicker.TimePickerActivity;
import net.ivpn.client.v2.account.AccountFragment;
import net.ivpn.client.v2.alwaysonvpn.AlwaysOnVPNFragment;
import net.ivpn.client.v2.antitracker.AntiTrackerFragment;
import net.ivpn.client.v2.connect.ConnectFragment;
import net.ivpn.client.v2.customdns.CustomDNSFragment;
import net.ivpn.client.v2.login.LoginFragment;
import net.ivpn.client.v2.map.MapView;
import net.ivpn.client.v2.network.NetworkProtectionFragment;
import net.ivpn.client.v2.network.NetworkRecyclerViewAdapter;
import net.ivpn.client.v2.network.rule.NetworkProtectionRulesFragment;
import net.ivpn.client.v2.protocol.ProtocolFragment;
import net.ivpn.client.v2.serverlist.ServerListTabFragment;
import net.ivpn.client.v2.serverlist.all.AllServersRecyclerViewAdapter;
import net.ivpn.client.v2.serverlist.all.ServerListFragment;
import net.ivpn.client.v2.serverlist.fastest.FastestSettingFragment;
import net.ivpn.client.v2.serverlist.favourite.FavouriteServerListRecyclerViewAdapter;
import net.ivpn.client.v2.serverlist.favourite.FavouriteServersListFragment;
import net.ivpn.client.v2.serverlist.holders.ServerViewHolder;
import net.ivpn.client.v2.settings.SettingsFragment;
import net.ivpn.client.v2.splittunneling.SplitTunnelingFragment;
import net.ivpn.client.v2.sync.SyncFragment;
import net.ivpn.client.v2.viewmodel.ViewModelCleaner;
import net.ivpn.client.vpn.AlwaysOnVpnService;
import net.ivpn.client.vpn.OnBootBroadcastReceiver;
import net.ivpn.client.vpn.controller.WireGuardKeyBroadcastReceiver;
import net.ivpn.client.vpn.local.PermissionActivity;
import net.ivpn.client.vpn.openvpn.IVPNService;

import dagger.Subcomponent;
import de.blinkt.openvpn.core.Connection;

@ActivityScope
@Subcomponent
public interface ActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        ActivityComponent create();
    }

    void inject(TimePickerActivity activity);

    void inject(PermissionActivity activity);

    void inject(SubscriptionActivity activity);

    void inject(BillingActivity activity);

    void inject(ConnectionShortcutsActivity activity);

    void inject(ServerListFragment fragment);

    void inject(FavouriteServersListFragment fragment);

    void inject(YearlySubscriptionFragment fragment);

    void inject(MonthlySubscriptionFragment fragment);

    void inject(WireGuardKeyBroadcastReceiver receiver);

    void inject(OnBootBroadcastReceiver receiver);

    void inject(IVPNService service);

    void inject(WireGuardUiService service);

    void inject(AlwaysOnVpnService service);

    void inject(NetworkRecyclerViewAdapter.CommonNetworkViewHolder viewHolder);

    void inject(NetworkRecyclerViewAdapter.WifiItemViewHolder viewHolder);

//    void inject(ServersRecyclerViewAdapter.ServerViewHolder viewHolder);

    void inject(ServerViewHolder viewHolder);

    void inject(Connection connection);

    DialogueCustomDNSViewModel getDialogueViewModel();

    //v2

    void inject(ConnectFragment connectFragment);

    void inject(SettingsFragment settingsFragment);

    void inject(SplitTunnelingFragment fragment);

    void inject(AlwaysOnVPNFragment fragment);

    void inject(NetworkProtectionFragment fragment);

    void inject(NetworkProtectionRulesFragment fragment);

    void inject(ProtocolFragment fragment);

    void inject(CustomDNSFragment fragment);

    void inject(ServerListTabFragment fragment);

    void inject(FastestSettingFragment fragment);

    void inject(LoginFragment fragment);

    void inject(SyncFragment fragment);

    void inject(AccountFragment fragment);

    void inject(AntiTrackerFragment fragment);

    void inject(MapView map);

    void inject(AllServersRecyclerViewAdapter adapter);

    void inject(FavouriteServerListRecyclerViewAdapter adapter);

    void inject(ViewModelCleaner clearer);
}
