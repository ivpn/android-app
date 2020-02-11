package net.ivpn.client.vpn.model;

import net.ivpn.client.R;

public enum NetworkState {
    TRUSTED(R.color.color_trusted_text, R.color.color_trusted_background, R.string.network_trusted),
    UNTRUSTED(R.color.color_untrusted_text, R.color.color_untrusted_background, R.string.network_untrusted),
    NONE(R.color.color_none_text, android.R.color.white, R.string.network_state_none),
    DEFAULT(R.color.color_default_text, R.color.color_default_untrusted_background, R.string.network_default);

    private int backgroundColor;
    private int textRes;
    private int textColor;

    NetworkState(int textColor, int backgroundColor, int textRes) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.textRes = textRes;
    }

    public int getBackgroundColor(NetworkState defaultState) {
        if (this.equals(DEFAULT)) {
            switch (defaultState) {
                case TRUSTED: {
                    return R.color.color_default_trusted_background;
                }
                case UNTRUSTED: {
                    return R.color.color_default_untrusted_background;
                }
                default: {
                    return android.R.color.white;
                }
            }
        }
        return backgroundColor;
    }

    public int getTextRes() {
        return textRes;
    }

    public int getTextColor() {
        return textColor;
    }

    public static NetworkState[] getDefaultStates() {
        return new NetworkState[]{TRUSTED, UNTRUSTED, NONE};
    }

    public static NetworkState[] getActiveState() {
        return new NetworkState[]{TRUSTED, UNTRUSTED, DEFAULT};
    }
}