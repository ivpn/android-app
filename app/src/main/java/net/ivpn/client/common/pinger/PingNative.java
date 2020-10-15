package net.ivpn.client.common.pinger;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class PingNative {
    private PingNative() {
    }

    public static PingResult ping(InetAddress host, int timeOutMillis) throws IOException, InterruptedException {
        PingResult pingResult = new PingResult(host);
        StringBuilder echo = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();
        int timeoutSeconds = timeOutMillis / 1000;
        if (timeoutSeconds < 0) {
            timeoutSeconds = 1;
        }

        String address = host.getHostAddress();
        String pingCommand = "ping";
        if (address != null) {
            if (IPTools.isIPv6Address(address)) {
                pingCommand = "ping6";
            } else if (!IPTools.isIPv4Address(address)) {
                ;
            }
        } else {
            address = host.getHostName();
        }

        Process proc = runtime.exec(pingCommand + " -c 1 -w " + timeoutSeconds + " " + address);
        proc.waitFor();
        int exit = proc.exitValue();
        if (exit != 0) {
            String pingError;
            if (exit == 1) {
                pingError = "failed, exit = 1";
            } else {
                pingError = "error, exit = 2";
            }

            pingResult.error = pingError;
            return pingResult;
        } else {
            InputStreamReader reader = new InputStreamReader(proc.getInputStream());
            BufferedReader buffer = new BufferedReader(reader);

            String line;
            while ((line = buffer.readLine()) != null) {
                echo.append(line).append("\n");
            }

            return getPingStats(pingResult, echo.toString());
        }
    }

    public static PingResult getPingStats(PingResult pingResult, String s) {
        String pingError;
        if (s.contains("0% packet loss")) {
            int start = s.indexOf("/mdev = ");
            int end = s.indexOf(" ms\n", start);
            pingResult.fullString = s;
            if (start != -1 && end != -1) {
                s = s.substring(start + 8, end);
                String[] stats = s.split("/");
                pingResult.isReachable = true;
                pingResult.result = s;
                pingResult.timeTaken = Float.parseFloat(stats[1]);
                return pingResult;
            }

            pingError = "Error: " + s;
        } else if (s.contains("100% packet loss")) {
            pingError = "100% packet loss";
        } else if (s.contains("% packet loss")) {
            pingError = "partial packet loss";
        } else if (s.contains("unknown host")) {
            pingError = "unknown host";
        } else {
            pingError = "unknown error in getPingStats";
        }

        pingResult.error = pingError;
        return pingResult;
    }
}
