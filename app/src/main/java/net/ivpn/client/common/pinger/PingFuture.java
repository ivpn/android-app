package net.ivpn.client.common.pinger;

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

import net.ivpn.client.rest.data.model.Server;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

class PingFuture {

    private static final String TAG = PingFuture.class.getSimpleName();
    private static final int TIMES = 2;
    private static final int TIMEOUT = 1000;

    private volatile boolean isFinished;
    private volatile PingResultFormatter result;
    private volatile Server server;
    private volatile ArrayList<OnPingFinishListener> listeners = new ArrayList<>();
    private ExecutorService executor;

    PingFuture(ExecutorService executor) {
        this.executor = executor;
        isFinished = false;
    }

    Runnable getPingRunnable(Server server, final String ipAddress, final OnPingFinishListener listener) {
        this.server = server;
        synchronized (this) {
            if (listener != null) {
                this.listeners.add(listener);
            }
        }
        return new Thread(() -> Ping.onAddress(ipAddress, executor)
                .setTimeOutMillis(TIMEOUT)
                .setTimes(TIMES)
                .doPing(new Ping.PingListener() {
                    @Override
                    public void onResult(PingResult pingResult) {
                    }

                    @Override
                    public void onFinished(PingStats pingStats) {
                        isFinished = true;
                        if (pingStats.getPacketsLost() == TIMES) {
                            result = new PingResultFormatter(PingResultFormatter.PingResult.OFFLINE, -1);
                        } else {
                            result = new PingResultFormatter(PingResultFormatter.PingResult.OK, (long) pingStats.getMinTimeTaken());
                        }
                        synchronized (this) {
                            for (OnPingFinishListener listener: listeners) {
                                listener.onPingFinish(server, result);
                            }
                            listeners.clear();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        isFinished = true;
                        result = new PingResultFormatter(PingResultFormatter.PingResult.OFFLINE, -1);
                        synchronized (this) {
                            for (OnPingFinishListener listener: listeners) {
                                listener.onPingFinish(server, result);
                            }
                            listeners.clear();
                        }
                        e.printStackTrace();
                    }
                }));
    }

    boolean isFinished() {
        return isFinished;
    }

    PingResultFormatter getResult() {
        return result;
    }

    void addOnPingFinishListener(OnPingFinishListener listener) {
        synchronized (this) {
            if (listener != null) {
                if (isFinished) {
                    listener.onPingFinish(server, result);
                } else {
                    listeners.add(listener);
                }
            }
        }
    }
}