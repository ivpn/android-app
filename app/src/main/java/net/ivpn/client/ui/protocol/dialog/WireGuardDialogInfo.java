package net.ivpn.client.ui.protocol.dialog;

import net.ivpn.client.common.utils.DateUtil;

public class WireGuardDialogInfo {

    private String publicKey;
    private String ipAddress;
    private long lastGeneratedTime;
    private long regenerationPeriod;

    public WireGuardDialogInfo(String publicKey, String ipAddress, long lastGeneratedTime, long regenerationPeriod) {
        this.publicKey = publicKey;
        this.ipAddress = ipAddress;
        this.lastGeneratedTime = lastGeneratedTime;
        this.regenerationPeriod = regenerationPeriod;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLastGenerated() {
        return DateUtil.formatDateTime(lastGeneratedTime);
    }

    public String getNextRegenerationDate() {
        return DateUtil.formatDateTime(lastGeneratedTime + regenerationPeriod * DateUtil.DAY);
    }

    public String getValidUntil() {
        return DateUtil.formatDateTime(lastGeneratedTime + 40 * DateUtil.DAY);
    }
}
