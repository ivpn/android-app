package net.ivpn.core.common.dagger;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import com.wireguard.android.backend.WireGuardUiService;

import net.ivpn.core.common.shortcuts.ConnectionShortcutsActivity;
import net.ivpn.core.common.tile.IVPNTileService;
import net.ivpn.core.v2.account.AccountFragment;
import net.ivpn.core.v2.account.LogOutFragment;
import net.ivpn.core.v2.alwaysonvpn.AlwaysOnVPNFragment;
import net.ivpn.core.v2.antitracker.AntiTrackerFragment;
import net.ivpn.core.v2.captcha.CaptchaFragment;
import net.ivpn.core.v2.connect.ConnectFragment;
import net.ivpn.core.v2.customdns.CustomDNSFragment;
import net.ivpn.core.v2.customdns.DialogueCustomDNSViewModel;
import net.ivpn.core.v2.killswitch.KillSwitchFragment;
import net.ivpn.core.v2.login.LoginFragment;
import net.ivpn.core.v2.map.MapView;
import net.ivpn.core.v2.mocklocation.MockLocationFragment;
import net.ivpn.core.v2.mocklocation.MockLocationStep1Fragment;
import net.ivpn.core.v2.mocklocation.MockLocationStep2Fragment;
import net.ivpn.core.v2.mocklocation.MockLocationStep3Fragment;
import net.ivpn.core.v2.network.NetworkCommonFragment;
import net.ivpn.core.v2.network.NetworkRecyclerViewAdapter;
import net.ivpn.core.v2.network.rule.NetworkProtectionRulesFragment;
import net.ivpn.core.v2.network.saved.SavedNetworksFragment;
import net.ivpn.core.v2.network.scanned.ScannedNetworksFragment;
import net.ivpn.core.v2.protocol.ProtocolFragment;
import net.ivpn.core.v2.protocol.port.PortsFragment;
import net.ivpn.core.v2.protocol.wireguard.WireGuardDetailsFragment;
import net.ivpn.core.v2.serverlist.ServerListTabFragment;
import net.ivpn.core.v2.serverlist.all.AllServersRecyclerViewAdapter;
import net.ivpn.core.v2.serverlist.all.ServerListFragment;
import net.ivpn.core.v2.serverlist.fastest.FastestSettingFragment;
import net.ivpn.core.v2.serverlist.favourite.FavouriteServerListRecyclerViewAdapter;
import net.ivpn.core.v2.serverlist.favourite.FavouriteServersListFragment;
import net.ivpn.core.v2.serverlist.holders.ServerViewHolder;
import net.ivpn.core.v2.settings.SettingsFragment;
import net.ivpn.core.v2.splittunneling.SplitTunnelingFragment;
import net.ivpn.core.v2.sync.SyncFragment;
import net.ivpn.core.v2.tfa.TFAFragment;
import net.ivpn.core.v2.timepicker.TimePickerActivity;
import net.ivpn.core.v2.viewmodel.ViewModelCleaner;
import net.ivpn.core.vpn.AlwaysOnVpnService;
import net.ivpn.core.vpn.OnBootBroadcastReceiver;
import net.ivpn.core.vpn.controller.WireGuardKeyBroadcastReceiver;
import net.ivpn.core.vpn.local.PermissionActivity;
import net.ivpn.core.vpn.openvpn.IVPNService;

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

    void inject(ConnectionShortcutsActivity activity);

    void inject(ServerListFragment fragment);

    void inject(FavouriteServersListFragment fragment);

    void inject(WireGuardKeyBroadcastReceiver receiver);

    void inject(OnBootBroadcastReceiver receiver);

    void inject(IVPNTileService service);

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

    void inject(SavedNetworksFragment fragment);

    void inject(ScannedNetworksFragment fragment);

    void inject(TFAFragment fragment);

    void inject(CaptchaFragment fragment);

    void inject(WireGuardDetailsFragment fragment);

    void inject(KillSwitchFragment fragment);

    void inject(MockLocationFragment fragment);

    void inject(MockLocationStep1Fragment fragment);

    void inject(MockLocationStep2Fragment fragment);

    void inject(MockLocationStep3Fragment fragment);

    void inject(LogOutFragment fragment);

    void inject(PortsFragment fragment);

    void inject(MapView map);

    void inject(AllServersRecyclerViewAdapter adapter);

    void inject(FavouriteServerListRecyclerViewAdapter adapter);

    void inject(NetworkRecyclerViewAdapter adapter);

    void inject(ViewModelCleaner clearer);
}
