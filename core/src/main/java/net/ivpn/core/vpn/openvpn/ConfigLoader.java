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

import net.ivpn.core.IVPNApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;

/**
 *  Class ConfigLoader provides functionality to read config file from assets
 *  and create appropriate {@link VpnProfile}
 */
public class ConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String PROFILE_CONFIG = "config.ovpn";

    /**
     *  Load default config file from assets and create appropriate profile
     *  @return {@link VpnProfile} instance
     */
    public static VpnProfile load() {
        LOGGER.info("load");
        ConfigParser configParser = new ConfigParser();
        VpnProfile profile = null;
        try {
            InputStream inputStream = IVPNApplication.application.getAssets().open(PROFILE_CONFIG);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            configParser.parseConfig(inputStreamReader);
            profile = configParser.convertProfile();
        } catch (IOException | ConfigParser.ConfigParseError e) {
            LOGGER.error("Error while loading OpenVpn profile", e);
            e.printStackTrace();
        }
        return profile;
    }
}