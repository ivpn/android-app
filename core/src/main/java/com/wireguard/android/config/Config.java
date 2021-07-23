/*
 * Copyright © 2018 Samuel Holland <samuel@sholland.org>
 * Copyright © 2018 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.config;

import androidx.databinding.ObservableArrayList;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import com.wireguard.android.crypto.KeyEncoding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Random;

/**
 * Represents a wg-quick configuration file, its name, and its connection state.
 */

public class Config implements Parcelable{
    public static final Parcelable.Creator<Config> CREATOR = new Parcelable.Creator<Config>() {
        @Override
        public Config createFromParcel(final Parcel in) {
            return new Config(in);
        }

        @Override
        public Config[] newArray(final int size) {
            return new Config[size];
        }
    };

    public static Config from(final InputStream stream) throws IOException {
        return from(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
    }

    public static Config from(final BufferedReader reader) throws IOException {
        final Config config = new Config();
        Peer currentPeer = null;
        String line;
        boolean inInterfaceSection = false;
        while ((line = reader.readLine()) != null) {
            final int commentIndex = line.indexOf('#');
            if (commentIndex != -1)
                line = line.substring(0, commentIndex);
            line = line.trim();
            if (line.isEmpty())
                continue;
            if ("[Interface]".toLowerCase().equals(line.toLowerCase())) {
                currentPeer = null;
                inInterfaceSection = true;
            } else if ("[Peer]".toLowerCase().equals(line.toLowerCase())) {
                currentPeer = new Peer();
                config.peers.add(currentPeer);
                inInterfaceSection = false;
            } else if (inInterfaceSection) {
                config.interfaceSection.parse(line);
            } else if (currentPeer != null) {
                currentPeer.parse(line);
            } else {
//                throw new IllegalArgumentException("Invalid configuration line: " + line);
            }
        }
        if (!inInterfaceSection && currentPeer == null) {
            throw new IllegalArgumentException("Could not find any config information");
        }
        return config;
    }

    @Nullable private String name;
    private Interface interfaceSection;
    private List<Peer> peers;

    public Config() {
        peers = new ArrayList<>();
        interfaceSection = new Interface();
    }

    private Config(Parcel in) {
        name = in.readString();
        interfaceSection = in.readParcelable(Interface.class.getClassLoader());
        peers = new ObservableArrayList<>();
        in.readTypedList(peers, Peer.CREATOR);
    }

    public Interface getInterface() {
        return interfaceSection;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public Peer gerRandomPeer() {
        return peers.get(new Random().nextInt(peers.size()));
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }

    public String format() throws Exception {
        Peer peer;

        try (final Formatter fmt = new Formatter(new StringBuilder())) {
            fmt.format("replace_peers=true\n");
            if (interfaceSection.getPrivateKey() != null)
                fmt.format("private_key=%s\n", KeyEncoding.keyToHex(KeyEncoding.keyFromBase64(interfaceSection.getPrivateKey())));
            if (interfaceSection.getListenPort() != 0)
                fmt.format("listen_port=%d\n", interfaceSection.getListenPort());

            peer = gerRandomPeer();
            if (peer.getPublicKey() != null)
                fmt.format("public_key=%s\n", KeyEncoding.keyToHex(KeyEncoding.keyFromBase64(peer.getPublicKey())));
            if (peer.getPreSharedKey() != null)
                fmt.format("preshared_key=%s\n", KeyEncoding.keyToHex(KeyEncoding.keyFromBase64(peer.getPreSharedKey())));
            if (peer.getEndpoint() != null)
                fmt.format("endpoint=%s\n", peer.getResolvedEndpointString());
            if (peer.getPersistentKeepalive() != 0)
                fmt.format("persistent_keepalive_interval=%d\n", peer.getPersistentKeepalive());
            for (final InetNetwork addr : peer.getAllowedIPs()) {
                fmt.format("allowed_ip=%s\n", addr.toString());
            }

            return fmt.toString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(name);
        dest.writeParcelable(interfaceSection, flags);
        dest.writeTypedList(peers);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder().append(interfaceSection);
        for (final Peer peer : peers)
            sb.append('\n').append(peer);
        return sb.toString();
    }
}