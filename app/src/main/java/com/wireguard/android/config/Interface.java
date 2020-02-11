/*
 * Copyright © 2018 Samuel Holland <samuel@sholland.org>
 * Copyright © 2018 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.config;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.wireguard.android.crypto.Keypair;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the configuration for a WireGuard interface (an [Interface] block).
 */

public class Interface implements Parcelable{
    public static final Parcelable.Creator<Interface> CREATOR = new Parcelable.Creator<Interface>() {
        @Override
        public Interface createFromParcel(final Parcel in) {
            return new Interface(in);
        }

        @Override
        public Interface[] newArray(final int size) {
            return new Interface[size];
        }
    };
    private final List<InetNetwork> addressList = new ArrayList<>() ;
    private final List<InetAddress> dnsList = new ArrayList<>();
    private final List<String> excludedApplications = new ArrayList<>();
    @Nullable private Keypair keypair;
    private int listenPort;
    private int mtu;
//    @Nullable private String privateKey;
//    @Nullable private String publicKey;

    public Interface() {
    }

    private Interface(Parcel in) {
        setAddressString(in.readString());
        setDnsString(in.readString());
        String publicKey = in.readString();
        String privateKey = in.readString();
        setPrivateKey(privateKey);
        setListenPortString(in.readString());
        setMtuString(in.readString());
        setExcludedApplicationsString(in.readString());
    }

    private void addAddresses(@Nullable final String[] addresses) {
        if (addresses != null && addresses.length > 0) {
            for (final String addr : addresses) {
                if (addr.isEmpty())
                    throw new IllegalArgumentException("Address is empty");
                addressList.add(new InetNetwork(addr));
            }
        }
    }

    private void addDnses(@Nullable final String[] dnses) {
        if (dnses != null && dnses.length > 0) {
            for (final String dns : dnses) {
                dnsList.add(InetAddresses.parse(dns));
            }
        }
    }

    private void addExcludedApplications(@Nullable final String[] applications) {
        if (applications != null && applications.length > 0) {
            excludedApplications.addAll(Arrays.asList(applications));
        }
    }

    @Nullable
    private String getAddressString() {
        if (addressList.isEmpty())
            return null;
        return Attribute.iterableToString(addressList);
    }

    public InetNetwork[] getAddresses() {
        return addressList.toArray(new InetNetwork[addressList.size()]);
    }

    @Nullable
    private String getDnsString() {
        if (dnsList.isEmpty())
            return null;
        return Attribute.iterableToString(getDnsStrings());
    }

    private List<String> getDnsStrings() {
        final List<String> strings = new ArrayList<>();
        for (final InetAddress addr : dnsList)
            strings.add(addr.getHostAddress());
        return strings;
    }

    public InetAddress[] getDnses() {
        return dnsList.toArray(new InetAddress[dnsList.size()]);
    }

    @Nullable
    private String getExcludedApplicationsString() {
        if (excludedApplications.isEmpty())
            return null;
        return Attribute.iterableToString(excludedApplications);
    }

    public String[] getExcludedApplications() {
        return excludedApplications.toArray(new String[excludedApplications.size()]);
    }

    public int getListenPort() {
        return listenPort;
    }

    @Nullable
    private String getListenPortString() {
        if (listenPort == 0)
            return null;
        return Integer.valueOf(listenPort).toString();
    }

    public int getMtu() {
        return mtu;
    }

    @Nullable
    private String getMtuString() {
        if (mtu == 0)
            return null;
        return Integer.toString(mtu);
    }

    @Nullable
    public String getPrivateKey() {
        if (keypair == null)
            return null;
        return keypair.getPrivateKey();
    }

    @Nullable
    public String getPublicKey() {
        if (keypair == null)
            return null;
        return keypair.getPublicKey();
    }

    public void generateKeyPair() {
        keypair = new Keypair();
    }

    public void parse(final String line) {
        final Attribute key = Attribute.match(line);
        if (key == null)
            throw new IllegalArgumentException(String.format("Unable to parse line: \"%s\"", line));
        switch (key) {
            case ADDRESS:
                addAddresses(key.parseList(line));
                break;
            case DNS:
                addDnses(key.parseList(line));
                break;
            case EXCLUDED_APPLICATIONS:
                addExcludedApplications(key.parseList(line));
                break;
            case LISTEN_PORT:
                setListenPortString(key.parse(line));
                break;
            case MTU:
                setMtuString(key.parse(line));
                break;
            case PRIVATE_KEY:
                setPrivateKey(key.parse(line));
                break;
            default:
                throw new IllegalArgumentException(line);
        }
    }

    public void setAddressString(@Nullable final String addressString) {
        addressList.clear();
        addAddresses(Attribute.stringToList(addressString));
    }

    public void setDnsString(@Nullable final String dnsString) {
        dnsList.clear();
        addDnses(Attribute.stringToList(dnsString));
    }

    private void setExcludedApplicationsString(@Nullable final String applicationsString) {
        excludedApplications.clear();
        addExcludedApplications(Attribute.stringToList(applicationsString));
    }

    private void setListenPort(final int listenPort) {
        this.listenPort = listenPort;
    }

    private void setListenPortString(@Nullable final String port) {
        if (port != null && !port.isEmpty())
            setListenPort(Integer.parseInt(port, 10));
        else
            setListenPort(0);
    }

    private void setMtu(final int mtu) {
        this.mtu = mtu;
    }

    private void setMtuString(@Nullable final String mtu) {
        if (mtu != null && !mtu.isEmpty())
            setMtu(Integer.parseInt(mtu, 10));
        else
            setMtu(0);
    }

    public void setPrivateKey(@Nullable String privateKey) {
        if (privateKey != null && privateKey.isEmpty())
            privateKey = null;
        keypair = privateKey == null ? null : new Keypair(privateKey);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder().append("[Interface]\n");
        if (!addressList.isEmpty())
            sb.append(Attribute.ADDRESS.composeWith(addressList));
        if (!dnsList.isEmpty())
            sb.append(Attribute.DNS.composeWith(getDnsStrings()));
        if (!excludedApplications.isEmpty())
            sb.append(Attribute.EXCLUDED_APPLICATIONS.composeWith(excludedApplications));
        if (listenPort != 0)
            sb.append(Attribute.LISTEN_PORT.composeWith(listenPort));
        if (mtu != 0)
            sb.append(Attribute.MTU.composeWith(mtu));
        if (keypair != null)
            sb.append(Attribute.PRIVATE_KEY.composeWith(keypair.getPrivateKey()));
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getAddressString());
        dest.writeString(getDnsString());
        dest.writeString(keypair.getPrivateKey());
        dest.writeString(getListenPortString());
        dest.writeString(getMtuString());
        dest.writeString(getExcludedApplicationsString());
    }
}
