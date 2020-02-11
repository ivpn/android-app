package net.ivpn.client.ui.customdns;

import android.content.Context;
import android.databinding.ObservableField;

import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.utils.ToastUtil;

import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

public class DialogueCustomDNSViewModel {

    private static final String DOT = ".";
    private static final Pattern PATTERN = Pattern.compile(
            "^(?:(?:^|\\.)(?:2(?:5[0-5]|[0-4]\\d)|1?\\d?\\d)){4}$");

    public final ObservableField<String> first = new ObservableField<>();
    public final ObservableField<String> second = new ObservableField<>();
    public final ObservableField<String> third = new ObservableField<>();
    public final ObservableField<String> forth = new ObservableField<>();

    private Context context;
    private Settings settings;
    private OnDNSChangedListener listener;

    @Inject
    public DialogueCustomDNSViewModel(Context context, Settings settings) {
        this.context = context;
        this.settings = settings;
        init();
    }

    private void init() {
        String dnsAddress = settings.getCustomDNSValue();
        if (dnsAddress != null && !dnsAddress.isEmpty()) {
            String[] splitDNS = dnsAddress.split("\\.");
            first.set(splitDNS[0]);
            second.set(splitDNS[1]);
            third.set(splitDNS[2]);
            forth.set(splitDNS[3]);
        }
    }

    public void setOnDnsChangedListener(OnDNSChangedListener listener) {
        this.listener = listener;
    }

    public boolean validateDNS() {
        StringBuilder dnsAddressBuilder = new StringBuilder();
        dnsAddressBuilder.append(first.get()).append(DOT)
                .append(second.get()).append(DOT)
                .append(third.get()).append(DOT)
                .append(forth.get());
        String dns = dnsAddressBuilder.toString();
        if (validate(dns)) {
            settings.setCustomDNSValue(dns);
            listener.onCustomDNSChanged(dns);
            return true;
        }

        ToastUtil.toast(context, "The IP Address " + dns + " is invalid. Please, check and update settings.");
        return false;
    }

    private boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }
}
