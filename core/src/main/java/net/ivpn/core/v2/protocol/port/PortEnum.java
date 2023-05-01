package net.ivpn.core.v2.protocol.port;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import net.ivpn.core.vpn.Protocol;

import java.util.List;

import java9.util.stream.Collectors;
import java9.util.J8Arrays;

import static net.ivpn.core.vpn.Protocol.OPENVPN;
import static net.ivpn.core.vpn.Protocol.WIREGUARD;

public enum PortEnum {
    UDP_2049(PortEnum.UDP, 2049, OPENVPN),
    UDP_2050(PortEnum.UDP, 2050, OPENVPN),
    UDP_53(PortEnum.UDP, 53, OPENVPN),
    UDP_1194(PortEnum.UDP, 1194, OPENVPN),
    UDP_80(PortEnum.UDP, 80, OPENVPN),
    UDP_443(PortEnum.UDP, 443, OPENVPN),
    TCP_443(PortEnum.TCP, 443, OPENVPN),
    TCP_1443(PortEnum.TCP, 1443, OPENVPN),
    TCP_80(PortEnum.TCP, 80, OPENVPN),
    WG_UDP_53(PortEnum.UDP, 53, WIREGUARD),
    WG_UDP_1194(PortEnum.UDP, 1194, WIREGUARD),
    WG_UDP_2049(PortEnum.UDP, 2049, WIREGUARD),
    WG_UDP_2050(PortEnum.UDP, 2050, WIREGUARD),
    WG_UDP_30587(PortEnum.UDP, 30587, WIREGUARD),
    WG_UDP_41893(PortEnum.UDP, 41893, WIREGUARD),
    WG_UDP_48574(PortEnum.UDP, 48574, WIREGUARD),
    WG_UDP_58237(PortEnum.UDP, 58237, WIREGUARD),
    WG_UDP_80(PortEnum.UDP, 80, WIREGUARD),
    WG_UDP_443(PortEnum.UDP, 443, WIREGUARD);

    public static final String UDP = "UDP";
    public static final String TCP = "TCP";

    @Expose
    private String protocol;
    @Expose
    private int portNumber;
    @Expose
    private Protocol vpnProtocol;

    public static PortEnum from(String json) {
        if (json == null || json.isEmpty()) return null;
        return new Gson().fromJson(json, PortEnum.class);
    }

    public static PortEnum[] valuesFor(Protocol protocol) {
        if (protocol.equals(WIREGUARD)) {
            return new PortEnum[]{WG_UDP_53, WG_UDP_1194, WG_UDP_2049, WG_UDP_2050, WG_UDP_30587, WG_UDP_41893, WG_UDP_48574, WG_UDP_58237, WG_UDP_80, WG_UDP_443};
        } else {
            return new PortEnum[]{UDP_2049, UDP_2050, UDP_53, UDP_1194, UDP_80, UDP_443, TCP_443, TCP_1443, TCP_80};
        }
    }

    public static PortEnum[] valuesForMultiHop() {
        return new PortEnum[]{UDP_2049, TCP_443};
    }

    PortEnum(String protocol, int portNumber, Protocol vpnProtocol) {
        this.portNumber = portNumber;
        this.protocol = protocol;
        this.vpnProtocol = vpnProtocol;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getProtocol() {
        return protocol;
    }

    public PortEnum next() {
        List<PortEnum> ports = J8Arrays.stream(PortEnum.values())
                .filter(port -> port.vpnProtocol.equals(this.vpnProtocol))
                .collect(Collectors.toList());
        int position = ports.indexOf(this);
        return (position != ports.size() - 1) ? ports.get(position + 1) : ports.get(0);
    }

    public int ordinalForProtocol() {
        List<PortEnum> ports = J8Arrays.stream(PortEnum.values())
                .filter(port -> port.vpnProtocol.equals(this.vpnProtocol))
                .collect(Collectors.toList());
        return ports.indexOf(this);
    }

    public boolean isUDP() {
        return protocol.equalsIgnoreCase(PortEnum.UDP);
    }

    @Override
    public String toString() {
        return "Port{" +
                "protocol='" + protocol + '\'' +
                ", portNumber=" + portNumber +
                ", vpnProtocol=" + vpnProtocol +
                '}';
    }

    public String toThumbnail() {
        return protocol + " " + portNumber;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}