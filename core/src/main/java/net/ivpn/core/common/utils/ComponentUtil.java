package net.ivpn.core.common.utils;

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

import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.common.dagger.ApplicationScope;
import net.ivpn.core.common.migration.MigrationController;
import net.ivpn.core.common.prefs.Preference;
import net.ivpn.core.common.prefs.ServersRepository;
import net.ivpn.core.common.prefs.Settings;
import net.ivpn.core.vpn.GlobalBehaviorController;
import net.ivpn.core.vpn.ProtocolController;
import net.ivpn.core.vpn.local.NetworkController;
import net.ivpn.core.vpn.openvpn.ProfileManager;
import net.ivpn.core.vpn.wireguard.ConfigManager;

import javax.inject.Inject;

@ApplicationScope
public class ComponentUtil {

    private final Settings settings;
    private final Preference preference;
    private final ServersRepository serversRepository;
    private final GlobalBehaviorController globalBehaviorController;
    private final ProtocolController protocolController;
    private final NetworkController networkController;
    private final ConfigManager configManager;
    private final ProfileManager profileManager;
    private final MigrationController migrationController;
    private final LogUtil logUtil;

    @Inject
    ComponentUtil(LogUtil logUtil, Preference preference, Settings settings,
                  ServersRepository serversRepository, GlobalBehaviorController globalBehaviorController,
                  ProtocolController protocolController, NetworkController networkController,
                  ConfigManager configManager, ProfileManager profileManager,
                  MigrationController migrationController) {
        this.logUtil = logUtil;
        this.settings = settings;
        this.preference = preference;
        this.serversRepository = serversRepository;
        this.globalBehaviorController = globalBehaviorController;
        this.protocolController = protocolController;
        this.networkController = networkController;
        this.configManager = configManager;
        this.profileManager = profileManager;
        this.migrationController = migrationController;
    }

    public void performBaseComponentsInit() {
        initLogger();
        initWireGuard();
        initProfile();
        initApiAccessImprovement();
        initBillings();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        migrationController.checkForUpdates();
        IVPNApplication.appComponent.provideGlobalWireGuardAlarm();
        AppCompatDelegate.setDefaultNightMode(settings.getNightMode().getSystemId());
        initUpdateService();
    }

    public void resetComponents() {
        preference.removeAll();
        networkController.finishAll();
        globalBehaviorController.finishAll();
        IVPNApplication.updatesController.resetComponent();

        NotificationManagerCompat.from(IVPNApplication.application).cancelAll();
    }

    private void initProfile() {
        protocolController.init();
        profileManager.readDefaultProfile();
        networkController.init();
    }

    private void initUpdateService() {
        IVPNApplication.updatesController.initUpdateService();
    }

    private void initApiAccessImprovement() {
        serversRepository.tryUpdateIpList();
        serversRepository.tryUpdateServerLocations();
    }

    private void initLogger() {
        Log.d("ComponentUtil", "initLogger: ");
        logUtil.initialize();
    }

    private void initWireGuard() {
        configManager.init();
    }

    private void initBillings() {
    }
}
