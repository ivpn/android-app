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

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;

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

import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Inject;

@ApplicationScope
public class ComponentUtil {

    private Context context;
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

    @Inject
    ComponentUtil(Context context, UpdateHelper updateHelper, Preference preference, Settings settings,
                  UpdatesJobServiceUtil updatesJobServiceUtil, ServersRepository serversRepository,
                  GlobalBehaviorController globalBehaviorController, ProtocolController protocolController,
                  NetworkController networkController, ConfigManager configManager,
                  ProfileManager profileManager, MigrationController migrationController, SentryUtil sentryUtil) {
        this.context = context;
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
    }

    public void resetComponents() {
        preference.removeAll();
        globalBehaviorController.finishAll();
        networkController.finishAll();
        updatesJobServiceUtil.clearUpdateJob(IVPNApplication.getApplication());
        updateHelper.skipUpdate();
    }

    private void initProfile() {
        protocolController.init();
        profileManager.readDefaultProfile();
        globalBehaviorController.init();
        networkController.init();
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
        SLF4JBridgeHandler.install();
    }

    private void initWireGuard() {
        configManager.init();
    }

    private void initBillings() {

    }
}
