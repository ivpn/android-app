package net.ivpn.client.vpn;

public enum Protocol {
    WIREGUARD,
    OPENVPN;

    public boolean isMultihopEnabled() {
        return this == OPENVPN;
    }
}
