package net.ivpn.client.ui.customdns;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
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

    public void setDnsAs(String dns) {
        this.dns.set(dns);
    }

    private void enableCustomDNS(boolean value) {
        settings.enableCustomDNS(value);
    }
}
