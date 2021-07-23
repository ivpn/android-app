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

import java.net.InetAddress;

public class PingResult {
    public final InetAddress ia;
    public boolean isReachable;
    public String error = null;
    public float timeTaken;
    public String fullString;
    public String result;

    public PingResult(InetAddress ia) {
        this.ia = ia;
    }

    public boolean isReachable() {
        return this.isReachable;
    }

    public boolean hasError() {
        return this.error != null;
    }

    public float getTimeTaken() {
        return this.timeTaken;
    }

    public String getError() {
        return this.error;
    }

    public InetAddress getAddress() {
        return this.ia;
    }

    public String toString() {
        return "PingResult{ia=" + this.ia + ", isReachable=" + this.isReachable + ", error='" + this.error + '\'' + ", timeTaken=" + this.timeTaken + ", fullString='" + this.fullString + '\'' + ", result='" + this.result + '\'' + '}';
    }
}
