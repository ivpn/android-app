package net.ivpn.client.common.pinger;

import net.ivpn.client.R;

public class PingResultFormatter {

    private PingResult result;
    private long ping;

    PingResultFormatter(PingResult result, long ping) {
        this.result = result;
        this.ping = ping;
    }

    public String formatPing() {
        return result.equals(PingResult.OFFLINE) ? "" : ping + " ms";
    }

    public boolean isPingAvailable() {
        return !result.equals(PingResult.OFFLINE);
    }

    public long getPing() {
        return ping;
    }

    public int getAppropriateLight() {
        if (result.equals(PingResult.OFFLINE)) return R.drawable.ping_red_light;
        if (ping < 100) return R.drawable.ping_green_light;
        if (ping < 300) return R.drawable.ping_yellow_light;
        return R.drawable.ping_red_light;
    }

    enum PingResult {
        OK,
        OFFLINE;
    }
}