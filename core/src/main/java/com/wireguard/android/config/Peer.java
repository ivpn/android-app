/*
 * Copyright © 2018 Samuel Holland <samuel@sholland.org>
 * Copyright © 2018 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.config;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import com.wireguard.android.crypto.KeyEncoding;

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the configuration for a WireGuard peer (a [Peer] block).
 */

public class Peer implements Parcelable{
    public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {
        @Override
        public Peer createFromParcel(final Parcel in) {
            return new Peer(in);
        }

        @Override
        public Peer[] newArray(final int size) {
            return new Peer[size];
        }
    };
    private final List<InetNetwork> allowedIPsList = new ArrayList<>();
    @Nullable private InetSocketAddress endpoint;
    private int persistentKeepalive;
    @Nullable private String preSharedKey;
    @Nullable private String publicKey;

    public Peer() {
    }

    private Peer(Parcel in) {
        setAllowedIPsString(in.readString());
        setEndpointString(in.readString());
        setPersistentKeepaliveString(in.readString());
        preSharedKey = in.readString();
        publicKey = in.readString();
    }

    private void addAllowedIPs(@Nullable final String[] allowedIPs) {
        if (allowedIPs != null && allowedIPs.length > 0) {
            for (final String allowedIP : allowedIPs) {
                try {
                    allowedIPsList.add(InetNetwork.parse(allowedIP));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public InetNetwork[] getAllowedIPs() {
        return allowedIPsList.toArray(new InetNetwork[allowedIPsList.size()]);
    }

    @Nullable
    private String getAllowedIPsString() {
        if (allowedIPsList.isEmpty())
            return null;
        return Attribute.iterableToString(allowedIPsList);
    }

    @Nullable
    public InetSocketAddress getEndpoint() {
        return endpoint;
    }

    @Nullable
    private String getEndpointString() {
        if (endpoint == null)
            return null;
        if (endpoint.getHostString().contains(":") && !endpoint.getHostString().contains("["))
            return String.format("[%s]:%d", endpoint.getHostString(), endpoint.getPort());
        else
            return String.format("%s:%d", endpoint.getHostString(), endpoint.getPort());
    }

    public int getPersistentKeepalive() {
        return persistentKeepalive;
    }

    @Nullable
    private String getPersistentKeepaliveString() {
        if (persistentKeepalive == 0)
            return null;
        return Integer.valueOf(persistentKeepalive).toString();
    }

    @Nullable
    public String getPreSharedKey() {
        return preSharedKey;
    }

    @Nullable
    public String getPublicKey() {
        return publicKey;
    }

    public String getResolvedEndpointString() throws UnknownHostException {
        if (endpoint == null)
            throw new UnknownHostException("{empty}");
        if (endpoint.isUnresolved())
            endpoint = new InetSocketAddress(endpoint.getHostString(), endpoint.getPort());
        if (endpoint.isUnresolved())
            throw new UnknownHostException(endpoint.getHostString());
        if (endpoint.getAddress() instanceof Inet6Address)
            return String.format("[%s]:%d",
                    endpoint.getAddress().getHostAddress(),
                    endpoint.getPort());
        return String.format("%s:%d",
                endpoint.getAddress().getHostAddress(),
                endpoint.getPort());
    }

    public void parse(final String line) {
        final Attribute key = Attribute.match(line);
        if (key == null)
            throw new IllegalArgumentException(String.format("Unable to parse line: \"%s\"", line));
        switch (key) {
            case ALLOWED_IPS:
                addAllowedIPs(key.parseList(line));
                break;
            case ENDPOINT:
                setEndpointString(key.parse(line));
                break;
            case PERSISTENT_KEEPALIVE:
                setPersistentKeepaliveString(key.parse(line));
                break;
            case PRESHARED_KEY:
                setPreSharedKey(key.parse(line));
                break;
            case PUBLIC_KEY:
                setPublicKey(key.parse(line));
                break;
            default:
                throw new IllegalArgumentException(line);
        }
    }

    public void setAllowedIPsString(@Nullable final String allowedIPsString) {
        allowedIPsList.clear();
        addAllowedIPs(Attribute.stringToList(allowedIPsString));
    }

    public void setEndpoint(@Nullable final InetSocketAddress endpoint) {
        this.endpoint = endpoint;
    }

    public void setEndpointString(@Nullable final String endpoint) {
        if (endpoint != null && !endpoint.isEmpty()) {
            final InetSocketAddress constructedEndpoint;
            if (endpoint.indexOf('/') != -1 || endpoint.indexOf('?') != -1 || endpoint.indexOf('#') != -1)
                throw new IllegalArgumentException("Forbidden characters in endpoint");
            final URI uri;
            try {
                uri = new URI("wg://" + endpoint);
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
            constructedEndpoint = InetSocketAddress.createUnresolved(uri.getHost(), uri.getPort());
            setEndpoint(constructedEndpoint);
        } else {
            setEndpoint(null);
        }
    }

    private void setPersistentKeepalive(final int persistentKeepalive) {
        this.persistentKeepalive = persistentKeepalive;
    }

    private void setPersistentKeepaliveString(@Nullable final String persistentKeepalive) {
        if (persistentKeepalive != null && !persistentKeepalive.isEmpty())
            setPersistentKeepalive(Integer.parseInt(persistentKeepalive, 10));
        else
            setPersistentKeepalive(0);
    }

    private void setPreSharedKey(@Nullable String preSharedKey) {
        if (preSharedKey != null && preSharedKey.isEmpty())
            preSharedKey = null;
        if (preSharedKey != null)
            KeyEncoding.keyFromBase64(preSharedKey);
        this.preSharedKey = preSharedKey;
    }

    public void setPublicKey(@Nullable String publicKey) {
        if (publicKey != null && publicKey.isEmpty())
            publicKey = null;
        if (publicKey != null)
            KeyEncoding.keyFromBase64(publicKey);
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder().append("[Peer]\n");
        if (!allowedIPsList.isEmpty())
            sb.append(Attribute.ALLOWED_IPS.composeWith(allowedIPsList));
        if (endpoint != null)
            sb.append(Attribute.ENDPOINT.composeWith(getEndpointString()));
        if (persistentKeepalive != 0)
            sb.append(Attribute.PERSISTENT_KEEPALIVE.composeWith(persistentKeepalive));
        if (preSharedKey != null)
            sb.append(Attribute.PRESHARED_KEY.composeWith(preSharedKey));
        if (publicKey != null)
            sb.append(Attribute.PUBLIC_KEY.composeWith(publicKey));
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(getAllowedIPsString());
        dest.writeString(getEndpointString());
        dest.writeString(getPersistentKeepaliveString());
        dest.writeString(preSharedKey);
        dest.writeString(publicKey);
    }

    public static class Observable extends BaseObservable implements Parcelable {
        public static final Creator<Observable> CREATOR = new Creator<Observable>() {
            @Override
            public Observable createFromParcel(final Parcel in) {
                return new Observable(in);
            }

            @Override
            public Observable[] newArray(final int size) {
                return new Observable[size];
            }
        };
        @Nullable private String allowedIPs;
        @Nullable private String endpoint;
        @Nullable private String persistentKeepalive;
        @Nullable private String preSharedKey;
        @Nullable private String publicKey;
        private final List<String> interfaceDNSRoutes = new ArrayList<>();
        private int numSiblings;

        public Observable(final Peer parent) {
            loadData(parent);
        }

        private Observable(final Parcel in) {
            allowedIPs = in.readString();
            endpoint = in.readString();
            persistentKeepalive = in.readString();
            preSharedKey = in.readString();
            publicKey = in.readString();
            numSiblings = in.readInt();
            in.readStringList(interfaceDNSRoutes);
        }

        public static Observable newInstance() {
            return new Observable(new Peer());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Bindable @Nullable
        public String getPublicKey() {
            return publicKey;
        }

        private void loadData(final Peer parent) {
            allowedIPs = parent.getAllowedIPsString();
            endpoint = parent.getEndpointString();
            persistentKeepalive = parent.getPersistentKeepaliveString();
            preSharedKey = parent.getPreSharedKey();
            publicKey = parent.getPublicKey();
        }

        public void setPublicKey(final String publicKey) {
            this.publicKey = publicKey;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeString(allowedIPs);
            dest.writeString(endpoint);
            dest.writeString(persistentKeepalive);
            dest.writeString(preSharedKey);
            dest.writeString(publicKey);
            dest.writeInt(numSiblings);
            dest.writeStringList(interfaceDNSRoutes);
        }
    }
}
