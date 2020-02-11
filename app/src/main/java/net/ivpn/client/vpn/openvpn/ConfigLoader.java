package net.ivpn.client.vpn.openvpn;

import net.ivpn.client.IVPNApplication;

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
            InputStream inputStream = IVPNApplication.getApplication().getAssets().open(PROFILE_CONFIG);
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