package net.ivpn.client.common.dagger;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

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
import net.ivpn.client.v2.network.NetworkCommonFragment;
import net.ivpn.client.v2.network.NetworkRecyclerViewAdapter;
import net.ivpn.client.v2.network.rule.NetworkProtectionRulesFragment;
import net.ivpn.client.v2.network.saved.SavedNetworksFragment;
import net.ivpn.client.v2.network.scanned.ScannedNetworksFragment;
import net.ivpn.client.v2.protocol.ProtocolFragment;
import net.ivpn.client.v2.serverlist.ServerListTabFragment;
import net.ivpn.client.v2.serverlist.all.AllServersRecyclerViewAdapter;
import net.ivpn.client.v2.serverlist.all.ServerListFragment;
import net.ivpn.client.v2.serverlist.fastest.FastestSettingFragment;
import net.ivpn.client.v2.serverlist.favourite.FavouriteServerListRecyclerViewAdapter;
import net.ivpn.client.v2.serverlist.favourite.FavouriteServersListFragment;
import net.ivpn.client.v2.serverlist.holders.ServerViewHolder;
import net.ivpn.client.v2.settings.SettingsFragment;
import net.ivpn.client.v2.signup.SignUpAccountCreatedFragment;
import net.ivpn.client.v2.signup.SignUpPeriodFragment;
import net.ivpn.client.v2.signup.SignUpProductFragment;
import net.ivpn.client.v2.splittunneling.SplitTunnelingFragment;
import net.ivpn.client.v2.sync.SyncFragment;
import net.ivpn.client.v2.updates.UpdatesFragment;
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

    void inject(NetworkRecyclerViewAdapter.WifiItemViewHolder viewHolder);

    void inject(ServerViewHolder viewHolder);

    void inject(Connection connection);

    DialogueCustomDNSViewModel getDialogueViewModel();

    //v2

    void inject(ConnectFragment connectFragment);

    void inject(SettingsFragment settingsFragment);

    void inject(SplitTunnelingFragment fragment);

    void inject(AlwaysOnVPNFragment fragment);

    void inject(NetworkCommonFragment fragment);

    void inject(NetworkProtectionRulesFragment fragment);

    void inject(ProtocolFragment fragment);

    void inject(CustomDNSFragment fragment);

    void inject(ServerListTabFragment fragment);

    void inject(FastestSettingFragment fragment);

    void inject(LoginFragment fragment);

    void inject(SyncFragment fragment);

    void inject(AccountFragment fragment);

    void inject(AntiTrackerFragment fragment);

    void inject(SignUpProductFragment fragment);

    void inject(SignUpPeriodFragment fragment);

    void inject(SignUpAccountCreatedFragment fragment);

    void inject(SavedNetworksFragment fragment);

    void inject(ScannedNetworksFragment fragment);

    void inject(UpdatesFragment fragment);

    void inject(MapView map);

    void inject(AllServersRecyclerViewAdapter adapter);

    void inject(FavouriteServerListRecyclerViewAdapter adapter);

    void inject(NetworkRecyclerViewAdapter adapter);

    void inject(ViewModelCleaner clearer);
}
