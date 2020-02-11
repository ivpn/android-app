package net.ivpn.client.common.migration;

import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.NetworkProtectionPreference;
import net.ivpn.client.common.prefs.Preference;
import net.ivpn.client.common.prefs.SettingsPreference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@ApplicationScope
public class MigrationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationController.class);

    private Preference preference;
    private SettingsPreference settingsPreference;
    private NetworkProtectionPreference networkPreference;

    @Inject
    MigrationController(Preference preference, SettingsPreference settingsPreference,
                        NetworkProtectionPreference networkPreference) {
        this.preference = preference;
        this.settingsPreference = settingsPreference;
        this.networkPreference = networkPreference;
    }

    public void checkForUpdates() {
        int currentVersion = preference.getLogicVersion();
        LOGGER.info("checkForUpdates: currentVersion = " + currentVersion);
        boolean isLogicVersionExist = preference.isLogicVersionExist();
        LOGGER.info("checkForUpdates: isLogicVersionExist = " + isLogicVersionExist);
        if (isLogicVersionExist && currentVersion == Preference.LAST_LOGIC_VERSION) {
            //We are up-to-date
            LOGGER.info("checkForUpdates: we are up-to-date");
            return;
        }
        if (!isLogicVersionExist && currentVersion == Preference.LAST_LOGIC_VERSION) {
            LOGGER.info("checkForUpdates: applyMandatoryUpdates");
            applyMandatoryUpdates();
            return;
        }
        applyAllUpdates(currentVersion, Preference.LAST_LOGIC_VERSION);
    }

    private void applyMandatoryUpdates() {
        new UF0T1(settingsPreference, networkPreference).update();
        preference.setLogicVersion(Preference.LAST_LOGIC_VERSION);
    }

    private void applyAllUpdates(int from, int to) {
        LOGGER.info("applyAllUpdates: ");
        if (from == to) {
            return;
        }
    }
}