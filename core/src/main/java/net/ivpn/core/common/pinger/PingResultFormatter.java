package net.ivpn.core.common.pinger;

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

import net.ivpn.core.R;

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