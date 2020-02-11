package net.ivpn.client.ui.customdns;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.widget.CompoundButton;

import net.ivpn.client.common.prefs.Settings;

import javax.inject.Inject;

public class CustomDNSViewModel {

    public final ObservableBoolean isCustomDNSEnabled = new ObservableBoolean();
    public final ObservableField<String> dns = new ObservableField<>();

    private static final String EMPTY_DNS = "0.0.0.0";

    public CompoundButton.OnCheckedChangeListener enableCustomDNS = (compoundButton, value) -> enableCustomDNS(value);

    private Settings settings;

    @Inject
    CustomDNSViewModel(Settings settings) {
        this.settings = settings;
        init();
    }

    private void init() {
        isCustomDNSEnabled.set(settings.isCustomDNSEnabled());
        String customDNS = settings.getCustomDNSValue();
        dns.set(customDNS.isEmpty() ? EMPTY_DNS : customDNS);
    }

    void setDnsAs(String dns) {
        this.dns.set(dns);
    }

    private void enableCustomDNS(boolean value) {
        settings.enableCustomDNS(value);
    }
}
