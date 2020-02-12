package net.ivpn.client.ui.startonboot;

import androidx.databinding.ObservableBoolean;
import android.widget.CompoundButton;

import net.ivpn.client.common.prefs.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class StartOnBootViewModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartOnBootViewModel.class);

    public final ObservableBoolean startOnBoot = new ObservableBoolean();
    public CompoundButton.OnCheckedChangeListener enableStartOnBoot = (compoundButton, value) -> enableStartOnBoot(value);

    private Settings settings;

    @Inject
    StartOnBootViewModel(Settings settings) {
        this.settings = settings;
    }

    void onResume() {
        startOnBoot.set(isStartOnBootEnabled());
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
