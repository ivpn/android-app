package net.ivpn.core.vpn.openvpn;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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

import androidx.annotation.Nullable;

import net.ivpn.core.common.ProfileStorage;
import net.ivpn.core.common.dagger.ApplicationScope;
import net.ivpn.core.common.multihop.MultiHopController;
import net.ivpn.core.common.prefs.EncryptedUserPreference;
import net.ivpn.core.rest.data.model.ServerType;
import net.ivpn.core.common.prefs.ServersRepository;
import net.ivpn.core.common.prefs.Settings;
import net.ivpn.core.common.utils.StringUtil;
import net.ivpn.core.rest.data.model.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import de.blinkt.openvpn.VpnProfile;

/**
 * Class ProfileManager is used to update and access valid {@link VpnProfile}
 */
@ApplicationScope
public class ProfileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileManager.class);
    private VpnProfile currentProfile;

    private EncryptedUserPreference userPreference;
    private MultiHopController multiHopController;
    private ServersRepository serversRepository;

    @Inject
    public ProfileManager(EncryptedUserPreference userPreference, MultiHopController multiHopController, ServersRepository serversRepository) {
        this.userPreference = userPreference;
        this.multiHopController = multiHopController;
        this.serversRepository = serversRepository;
    }

    /**
     * Get current {@link VpnProfile}
     *
     * @return current profile
     */
    @Nullable
    VpnProfile getVpnProfile() {
        if (currentProfile == null) {
            readDefaultProfile();
        }

        updateGateway();
        updatePassword(currentProfile);
        updateUsername(currentProfile);

        LOGGER.info("getVpnProfile: currentProfile = " + currentProfile.getUUIDString());

        return currentProfile;
    }

    /**
     * Read default profile. It should be used at the start of application to save some time.
     * Because all config have similar options and are different only by the gateways.
     */
    public void readDefaultProfile() {
        LOGGER.info("readDefaultProfile");
        currentProfile = ConfigLoader.load();
        ProfileStorage.writeProfile(currentProfile);
    }

    private void updatePassword(VpnProfile profile) {
        String token = userPreference.getSessionToken();
        String password = "";
        if (token != null && !token.isEmpty()) {
            password = userPreference.getSessionVpnPassword();
        }
        profile.mPassword = password;
    }

    private void updateUsername(VpnProfile profile) {
        LOGGER.info("Updating username...");

        String token = userPreference.getSessionToken();
        String username = "";
        if (token != null && !token.isEmpty()) {
            username = userPreference.getSessionVpnUsername();
        }
        profile.mUsername = username;
    }

    private VpnProfile getProfile() {
        if (currentProfile == null) {
            currentProfile = ProfileStorage.readProfile();
        }

        return currentProfile;
    }

    private void updateGateway() {
        LOGGER.info("Updating gateway... ");
        Server entryServer = serversRepository.getCurrentServer(ServerType.ENTRY);
        Server exitServer = serversRepository.getCurrentServer(ServerType.EXIT);
        if (entryServer == null) {
            return;
        }

        VpnProfile suitableProfile = getProfile().copy(entryServer.getCountry());
        suitableProfile.mServerName = entryServer.getGateway();
        suitableProfile.mPassword = null;
        suitableProfile.mName = entryServer.getDescription();
        suitableProfile.ipAddresses = entryServer.getIpAddresses();
        suitableProfile.moveOptionsToConnection();
        if (multiHopController.isReadyToUse()) {
            suitableProfile.mRemoteCN = StringUtil.getLocationFromGateway(exitServer.getGateway());
        } else {
            suitableProfile.mRemoteCN = StringUtil.getLocationFromGateway(entryServer.getGateway());
        }

        currentProfile = suitableProfile;
    }
}