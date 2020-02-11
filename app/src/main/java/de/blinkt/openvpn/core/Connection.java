/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.text.TextUtils;
import android.util.Log;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.utils.DomainResolver;
import net.ivpn.client.ui.protocol.port.Port;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class Connection implements Serializable, Cloneable {
    public String mServerName = "openvpn.example.com";
    public String mServerPort = "1194";
    public List<String> ipAddresses;
    public boolean mUseUdp = true;
    public String mCustomConfiguration = "";
    public boolean mUseCustomConfig = false;
    public boolean mEnabled = true;
    public int mConnectTimeout = 0;

    @Inject
    transient Settings settings;
    @Inject
    transient DomainResolver domainResolver;

    public Connection() {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
    }

    public String getConnectionBlock() {
        applyAppSettings();
        String cfg = "";

        if (domainResolver.isResolved() || (ipAddresses == null || ipAddresses.isEmpty())) {
            cfg += getServerConnectionConfWithDomain();
        } else {
            cfg += getServerConnectionConfWithIpAddresses();
        }

        if (mConnectTimeout != 0)
            cfg += String.format(Locale.US, " connect-timeout  %d\n", mConnectTimeout);


        if (!TextUtils.isEmpty(mCustomConfiguration) && mUseCustomConfig) {
            cfg += mCustomConfiguration;
            cfg += "\n";
        }
        return cfg;
    }

    private String getServerConnectionConfWithDomain() {
        StringBuilder cfg = new StringBuilder();
        cfg.append("remote ");
        cfg.append(mServerName);
        cfg.append(" ");

        cfg.append(mServerPort);
        if (mUseUdp) {
            cfg.append(" udp\n");
        } else {
            cfg.append(" tcp-client\n");
        }
        Log.d("ConnectionBla", "getServerConnectionConfWithDomain: " + cfg);
        return cfg.toString();
    }

    private String getServerConnectionConfWithIpAddresses() {
        StringBuilder cfg = new StringBuilder();
        for (String ip : ipAddresses) {
            cfg.append("remote ");
            cfg.append(ip);
            cfg.append(" ");

            cfg.append(mServerPort);
            if (mUseUdp) {
                cfg.append(" udp\n");
            } else {
                cfg.append(" tcp-client\n");
            }
        }
        cfg.append("remote-random\n");
        Log.d("ConnectionBla", "getServerConnectionConfWithIpAddresses: " + cfg.toString());
        return cfg.toString();
    }

    private void applyAppSettings() {
        Port port = settings.getOpenVpnPort();
        mServerPort = String.valueOf(port.getPortNumber());
        mUseUdp = port.isUDP();
    }

    @Override
    public Connection clone() throws CloneNotSupportedException {
        return (Connection) super.clone();
    }

    public boolean isOnlyRemote() {
        return TextUtils.isEmpty(mCustomConfiguration) || !mUseCustomConfig;
    }
}