package net.ivpn.client.rest.requests.common;

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

import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.RequestListener;

public class Request<T> {

    private RequestWrapper<T> requestWrapper;

    public Request(Settings settings, HttpClientFactory httpClientFactory, ServersRepository serversRepository,
                   Duration duration, RequestWrapper.IpMode mode) {
        int timeOut = duration == Duration.SHORT ? 10 : 30;
        requestWrapper = new RequestWrapper<T>(settings, httpClientFactory, serversRepository, timeOut, mode);
    }

    public void start(RequestWrapper.CallBuilder<T> callBuilder, RequestListener listener) {
        requestWrapper.setCallBuilder(callBuilder);
        requestWrapper.setRequestListener(listener);
        requestWrapper.perform();
    }

    public void cancel() {
        if (requestWrapper != null) {
            requestWrapper.cancel();
        }
    }

    public enum Duration {
        SHORT,
        LONG;
    }
}