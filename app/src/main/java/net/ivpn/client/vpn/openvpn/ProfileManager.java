package net.ivpn.client.vpn.openvpn;

import android.support.annotation.Nullable;

import net.ivpn.client.common.ProfileStorage;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.prefs.UserPreference;
import net.ivpn.client.common.utils.StringUtil;
import net.ivpn.client.rest.data.model.Server;

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

    private UserPreference userPreference;
    private Settings settings;
    private ServersRepository serversRepository;

    @Inject
    public ProfileManager(UserPreference userPreference, Settings settings, ServersRepository serversRepository) {
        this.userPreference = userPreference;
        this.settings = settings;
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

        boolean isMultiHopEnabled = settings.isMultiHopEnabled();
        if (isMultiHopEnabled) {
            Server exitServer = serversRepository.getCurrentServer(ServerType.EXIT);
            if (exitServer == null) return;
            profile.mUsername = username + "@" + StringUtil.getLocationFromGateway(exitServer.getGateway());
        } else {
            profile.mUsername = username;
        }
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
        if (entryServer == null) {
            return;
        }

        VpnProfile suitableProfile = getProfile().copy(entryServer.getCountry());
        suitableProfile.mServerName = entryServer.getGateway();
        suitableProfile.mRemoteCN = StringUtil.getLocationFromGateway(entryServer.getGateway());
        suitableProfile.mPassword = null;
        suitableProfile.mName = entryServer.getDescription();
        suitableProfile.ipAddresses = entryServer.getIpAddresses();
        suitableProfile.moveOptionsToConnection();

        currentProfile = suitableProfile;
    }
}