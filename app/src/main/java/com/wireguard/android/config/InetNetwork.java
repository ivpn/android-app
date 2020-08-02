/*
 * Copyright © 2018 Samuel Holland <samuel@sholland.org>
 * Copyright © 2018 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.config;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Objects;

public class InetNetwork {
    private final InetAddress address;
    private final int mask;

    InetNetwork(final InetAddress address, final int mask) {
        this.address = address;
        this.mask = mask;
    }

    public static InetNetwork parse(final String network) throws ParseException {
        final int slash = network.lastIndexOf('/');
        final String maskString;
        final int rawMask;
        final String rawAddress;
        if (slash >= 0) {
            maskString = network.substring(slash + 1);
            try {
                rawMask = Integer.parseInt(maskString, 10);
            } catch (final NumberFormatException ignored) {
                throw new ParseException(Integer.class, maskString);
            }
            rawAddress = network.substring(0, slash);
        } else {
            maskString = "";
            rawMask = -1;
            rawAddress = network;
        }
        final InetAddress address = InetAddresses.parse(rawAddress);
        final int maxMask = (address instanceof Inet4Address) ? 32 : 128;
        if (rawMask > maxMask)
            throw new ParseException(InetNetwork.class, maskString, "Invalid network mask");
        final int mask = rawMask >= 0 ? rawMask : maxMask;
        return new InetNetwork(address, mask);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof InetNetwork))
            return false;
        final InetNetwork other = (InetNetwork) obj;
        return Objects.equals(address, other.address) && mask == other.mask;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getMask() {
        return mask;
    }

    @Override
    public int hashCode() {
        return address.hashCode() ^ mask;
    }

    @Override
    public String toString() {
        return address.getHostAddress() + '/' + mask;
    }
}
