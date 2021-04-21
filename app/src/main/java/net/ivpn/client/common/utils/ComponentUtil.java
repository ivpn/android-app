package net.ivpn.client.common.utils;

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

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.migration.MigrationController;
import net.ivpn.client.common.prefs.Preference;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.updater.UpdateHelper;
import net.ivpn.client.ui.updates.UpdatesJobServiceUtil;
import net.ivpn.client.vpn.GlobalBehaviorController;
import net.ivpn.client.vpn.ProtocolController;
import net.ivpn.client.vpn.local.NetworkController;
import net.ivpn.client.vpn.openvpn.ProfileManager;
import net.ivpn.client.vpn.wireguard.ConfigManager;

import javax.inject.Inject;

@ApplicationScope
public class ComponentUtil {

    private Settings settings;
    private UpdateHelper updateHelper;
    private Preference preference;
    private UpdatesJobServiceUtil updatesJobServiceUtil;
    private ServersRepository serversRepository;
    private GlobalBehaviorController globalBehaviorController;
    private ProtocolController protocolController;
    private NetworkController networkController;
    private ConfigManager configManager;
    private ProfileManager profileManager;
    private MigrationController migrationController;
    private SentryUtil sentryUtil;
    private LogUtil logUtil;

    @Inject
    ComponentUtil(LogUtil logUtil, UpdateHelper updateHelper, Preference preference, Settings settings,
                  UpdatesJobServiceUtil updatesJobServiceUtil, ServersRepository serversRepository,
                  GlobalBehaviorController globalBehaviorController, ProtocolController protocolController,
                  NetworkController networkController, ConfigManager configManager,
                  ProfileManager profileManager, MigrationController migrationController, SentryUtil sentryUtil) {
        this.logUtil = logUtil;
        this.updateHelper = updateHelper;
        this.settings = settings;
        this.preference = preference;
        this.updatesJobServiceUtil = updatesJobServiceUtil;
        this.serversRepository = serversRepository;
        this.globalBehaviorController = globalBehaviorController;
        this.protocolController = protocolController;
        this.networkController = networkController;
        this.configManager = configManager;
        this.profileManager = profileManager;
        this.migrationController = migrationController;
        this.sentryUtil = sentryUtil;
    }

    public void performBaseComponentsInit() {
        initSentry();
        initLogger();
        initWireGuard();
        initProfile();
        initApiAccessImprovement();
        initBillings();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        migrationController.checkForUpdates();
        IVPNApplication.getApplication().appComponent.provideGlobalWireGuardAlarm();
        AppCompatDelegate.setDefaultNightMode(settings.getNightMode().getSystemId());
        initUpdateService();
    }

    public void resetComponents() {
        networkController.finishAll();
        preference.removeAll();
        globalBehaviorController.finishAll();
        updatesJobServiceUtil.clearUpdateJob(IVPNApplication.getApplication());
        updateHelper.skipUpdate();

        NotificationManagerCompat.from(IVPNApplication.getApplication()).cancelAll();
    }

    private void initProfile() {
        protocolController.init();
        profileManager.readDefaultProfile();
        globalBehaviorController.init();
        networkController.init();
    }

    private void initUpdateService() {
        updatesJobServiceUtil.pushUpdateJob(IVPNApplication.getApplication());
    }

    private void initApiAccessImprovement() {
        serversRepository.tryUpdateIpList();
        serversRepository.tryUpdateServerLocations();
    }

    private void initSentry() {
        if (!BuildConfig.BUILD_VARIANT.equals("fdroid")) {
            sentryUtil.init();
        }
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
