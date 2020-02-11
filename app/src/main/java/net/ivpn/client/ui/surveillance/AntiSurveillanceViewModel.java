package net.ivpn.client.ui.surveillance;

import android.databinding.ObservableBoolean;
import android.os.Handler;
import android.widget.CompoundButton;

import net.ivpn.client.common.prefs.Settings;

import javax.inject.Inject;

public class AntiSurveillanceViewModel {

    public final ObservableBoolean isAntiSurveillanceEnabled = new ObservableBoolean();
    public final ObservableBoolean isHardcoreModeEnabled = new ObservableBoolean();
    public final ObservableBoolean isHardcoreModeUIEnabled = new ObservableBoolean();

    public CompoundButton.OnCheckedChangeListener enableAntiSurveillance = (compoundButton, value) -> enableAntiSurveillance(value);
    public CompoundButton.OnCheckedChangeListener enableHardcoreMode = (compoundButton, value) -> enableHardcoreMode(value);

    private Settings settings;

    @Inject
    AntiSurveillanceViewModel(Settings settings) {
        this.settings = settings;

        isAntiSurveillanceEnabled.set(settings.isAntiSurveillanceEnabled());
        isHardcoreModeEnabled.set(settings.isAntiSurveillanceHardcoreEnabled());
        isHardcoreModeUIEnabled.set(isAntiSurveillanceEnabled.get());
    }

    private void enableAntiSurveillance(boolean value) {
        isAntiSurveillanceEnabled.set(value);
        settings.enableAntiSurveillance(value);

        isHardcoreModeUIEnabled.set(value);
        if (!value) {
            isHardcoreModeEnabled.set(false);
            settings.enableAntiSurveillanceHardcore(false);
        }
    }

    private void enableHardcoreMode(boolean value) {
        isHardcoreModeEnabled.set(value);
        settings.enableAntiSurveillanceHardcore(value);
    }
}