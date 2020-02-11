package net.ivpn.client.ui.alwaysonvpn;

import android.databinding.ObservableBoolean;
import android.os.Build;
import android.widget.CompoundButton;

import net.ivpn.client.common.prefs.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class AlwaysOnVpnViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlwaysOnVpnViewModel.class);

    public final ObservableBoolean startOnBoot = new ObservableBoolean();
    public final ObservableBoolean isAlwaysOnVpnSupported = new ObservableBoolean();
    public CompoundButton.OnCheckedChangeListener enableStartOnBoot = (compoundButton, value) -> enableStartOnBoot(value);

    private Settings settings;

    @Inject
    AlwaysOnVpnViewModel(Settings settings) {
        this.settings = settings;
    }

    void onResume() {
        startOnBoot.set(isStartOnBootEnabled());
        isAlwaysOnVpnSupported.set(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
        LOGGER.info("Always VPN supported: " + isAlwaysOnVpnSupported.get());
    }

    private void enableStartOnBoot(boolean value) {
        LOGGER.info("Start on boot is enabled: " + value);
        startOnBoot.set(value);
        settings.enableStartOnBoot(value);
    }

    private boolean isStartOnBootEnabled() {
        return settings.isStartOnBootEnabled();
    }
}