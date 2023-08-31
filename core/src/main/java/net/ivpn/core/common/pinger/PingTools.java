package net.ivpn.core.common.pinger;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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

import java.io.IOException;
import java.net.InetAddress;

public class PingTools {
    private PingTools() {
    }

    public static PingResult doPing(InetAddress ia, int timeOutMillis) {
        try {
            return doNativePing(ia, timeOutMillis);
        } catch (InterruptedException var4) {
            PingResult pingResult = new PingResult(ia);
            pingResult.isReachable = false;
            pingResult.error = "Interrupted";
            return pingResult;
        } catch (Exception var5) {
            return doJavaPing(ia, timeOutMillis);
        }
    }

    public static PingResult doNativePing(InetAddress ia, int timeOutMillis) throws IOException, InterruptedException {
        return PingNative.ping(ia, timeOutMillis);
    }

    public static PingResult doJavaPing(InetAddress ia, int timeOutMillis) {
        PingResult pingResult = new PingResult(ia);

        try {
            long startTime = System.nanoTime();
            boolean reached = ia.isReachable(timeOutMillis);
            pingResult.timeTaken = (float) (System.nanoTime() - startTime) / 1000000.0F;
            pingResult.isReachable = reached;
            if (!reached) {
                pingResult.error = "Timed Out";
            }
        } catch (IOException var6) {
            pingResult.isReachable = false;
            pingResult.error = "IOException: " + var6.getMessage();
        }

        return pingResult;
    }
}
