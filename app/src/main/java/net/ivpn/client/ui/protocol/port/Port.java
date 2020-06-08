package net.ivpn.client.ui.protocol.port;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import net.ivpn.client.vpn.Protocol;

import java.util.List;

import java9.util.stream.Collectors;
import java9.util.J8Arrays;

import static net.ivpn.client.vpn.Protocol.OpenVPN;
import static net.ivpn.client.vpn.Protocol.WireGuard;

public enum Port {
    UDP_2049(Port.UDP, 2049, OpenVPN),
    UDP_2050(Port.UDP, 2050, OpenVPN),
    UDP_53(Port.UDP, 53, OpenVPN),
    UDP_1194(Port.UDP, 1194, OpenVPN),
    TCP_443(Port.TCP, 443, OpenVPN),
    TCP_1443(Port.TCP, 1443, OpenVPN),
    TCP_80(Port.TCP, 80, OpenVPN),
    WG_UDP_53(Port.UDP, 53, WireGuard),
    WG_UDP_1194(Port.UDP, 1194, WireGuard),
    WG_UDP_2049(Port.UDP, 2049, WireGuard),
    WG_UDP_2050(Port.UDP, 2050, WireGuard),
    WG_UDP_30587(Port.UDP, 30587, WireGuard),
    WG_UDP_41893(Port.UDP, 41893, WireGuard),
    WG_UDP_48574(Port.UDP, 48574, WireGuard),
    WG_UDP_58237(Port.UDP, 58237, WireGuard);

    public static final String UDP = "UDP";
    public static final String TCP = "TCP";

    @Expose
    private String protocol;
    @Expose
    private int portNumber;
    @Expose
    private Protocol vpnProtocol;

    public static Port from(String json) {
        if (json == null || json.isEmpty()) return null;
        return new Gson().fromJson(json, Port.class);
    }

    public static Port[] valuesFor(Protocol protocol) {
        if (protocol.equals(WireGuard)) {
            return new Port[]{WG_UDP_53, WG_UDP_1194, WG_UDP_2049, WG_UDP_2050, WG_UDP_30587, WG_UDP_41893, WG_UDP_48574, WG_UDP_58237};
        } else {
            return new Port[]{UDP_2049, UDP_2050, UDP_53, UDP_1194, TCP_443, TCP_1443, TCP_80};
        }
    }

    Port(String protocol, int portNumber, Protocol vpnProtocol) {
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

    public Port next() {
        List<Port> ports = J8Arrays.stream(Port.values())
                .filter(port -> port.vpnProtocol.equals(this.vpnProtocol))
                .collect(Collectors.toList());
        int position = ports.indexOf(this);
        return (position != ports.size() - 1) ? ports.get(position + 1) : ports.get(0);
    }

    public int ordinalForProtocol() {
        List<Port> ports = J8Arrays.stream(Port.values())
                .filter(port -> port.vpnProtocol.equals(this.vpnProtocol))
                .collect(Collectors.toList());
        return ports.indexOf(this);
    }

    public boolean isUDP() {
        return protocol.equalsIgnoreCase(Port.UDP);
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