package net.ivpn.client.common.utils;

import android.content.res.Resources;

import net.ivpn.client.R;

import de.blinkt.openvpn.VpnProfile;

/**
 *  Class BytesFormat contains methods are used to formatDate internet speed and total traffic.
 */
public class BytesFormat {

    /**
     *  Format internet speed string
     */
    public static String formatSpeed(long bytes, Resources res) {
        int unit = 1000;
        int exp = exp(bytes * 8, unit);
        float bytesUnit = (float) (bytes / Math.pow(unit, exp));

        switch (exp) {
            case 0:
                return res.getString(R.string.bits_per_second, bytesUnit);
            case 1:
                return res.getString(R.string.kbits_per_second, bytesUnit);
            case 2:
                return res.getString(R.string.mbits_per_second, bytesUnit);
            default:
                return res.getString(R.string.gbits_per_second, bytesUnit);
        }
    }

    /**
     *  Format total traffic string
     */
    public static String formatTraffic(long bytes, Resources res) {
        int unit = 1024;
        int exp = exp(bytes, unit);
        float bytesUnit = (float) (bytes / Math.pow(unit, exp));

        switch (exp) {
            case 0:
                return res.getString(R.string.volume_byte, bytesUnit);
            case 1:
                return res.getString(R.string.volume_kbyte, bytesUnit);
            case 2:
                return res.getString(R.string.volume_mbyte, bytesUnit);
            default:
                return res.getString(R.string.volume_gbyte, bytesUnit);

        }
    }

    private static int exp(long bytes, int unit) {
        return Math.max(0, Math.min((int) (Math.log(bytes) / Math.log(unit)), 3));
    }
}
