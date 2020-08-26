package net.ivpn.client.vpn.model;

import android.content.res.Resources;
import android.util.Log;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.utils.StringUtil;

public enum NetworkSource {
    WIFI,
    MOBILE_DATA,
    NO_NETWORK,
    UNDEFINED;

    private String ssid;
    private NetworkState state;
    private NetworkState defaultState;

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setState(NetworkState state) {
        this.state = state;
    }

    public void setDefaultState(NetworkState defaultState) {
        this.defaultState = defaultState;
    }

    public NetworkState getDefaultState() {
        return defaultState;
    }

    public String getSsid() {
        if (this.equals(MOBILE_DATA)) {
            return null;
        }
        return ssid;
    }

    public NetworkState getState() {
        return state;
    }

    public NetworkState getFinalState() {
        Log.d("NetworkSource", "getFinalState: state = " + state + " defaultState = " + defaultState);
        if (state == NetworkState.DEFAULT) {
            return defaultState;
        }

        return state;
    }

    public String getTitle() {
        if (this.equals(MOBILE_DATA)) {
            Resources resources = IVPNApplication.getApplication().getResources();
            return resources.getString(R.string.network_mobile_data);
        } else if (this.equals(NO_NETWORK) || this.equals(UNDEFINED)) {
            Resources resources = IVPNApplication.getApplication().getResources();
            return resources.getString(R.string.network_none);
        }
        return StringUtil.formatWifiSSID(ssid);
    }

    public int getIcon() {
        if (this.equals(MOBILE_DATA)) {
            return R.drawable.ic_network_cell;
        } else {
            return R.drawable.ic_wifi;
        }
    }
}
