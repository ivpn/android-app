package net.ivpn.core.common.utils;

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

import android.content.res.Resources;

import net.ivpn.core.R;

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
