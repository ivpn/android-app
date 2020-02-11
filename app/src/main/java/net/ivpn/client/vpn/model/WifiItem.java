package net.ivpn.client.vpn.model;

import net.ivpn.client.common.utils.StringUtil;

public class WifiItem {

    private String ssid;
    private String title;
    private NetworkState state;

    public WifiItem(String ssid, NetworkState state) {
        this.state = state;
        this.ssid = ssid;
        title = StringUtil.formatWifiSSID(ssid);
    }

    public String getSsid() {
        return ssid;
    }

    public String getTitle() {
        return title;
    }

    public NetworkState getNetworkState() {
        return state;
    }

    public void setNetworkState(NetworkState state) {
        this.state = state;
    }
}