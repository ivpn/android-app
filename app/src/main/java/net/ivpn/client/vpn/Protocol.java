package net.ivpn.client.vpn;

public enum Protocol {
    WireGuard,
    OpenVPN;

    public boolean isMultihopEnabled() {
        return this == OpenVPN;
    }
}
