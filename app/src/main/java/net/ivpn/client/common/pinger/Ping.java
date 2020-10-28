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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class Ping {
    public static final int PING_JAVA = 0;
    public static final int PING_NATIVE = 1;
    public static final int PING_HYBRID = 2;
    private String addressString = null;
    private InetAddress address;
    private int timeOutMillis = 1000;
    private int delayBetweenScansMillis = 0;
    private int times = 1;
    private boolean cancelled = false;
    private ExecutorService executor;

    private Ping() {
    }

    public static Ping onAddress(String address, ExecutorService executor) {
        Ping ping = new Ping();
        ping.setAddressString(address);
        ping.executor = executor;
        return ping;
    }

    public static Ping onAddress(InetAddress ia, ExecutorService executor) {
        Ping ping = new Ping();
        ping.setAddress(ia);
        ping.executor = executor;
        return ping;
    }

    public Ping setTimeOutMillis(int timeOutMillis) {
        if (timeOutMillis < 0) {
            throw new IllegalArgumentException("Times cannot be less than 0");
        } else {
            this.timeOutMillis = timeOutMillis;
            return this;
        }
    }

    public Ping setDelayMillis(int delayBetweenScansMillis) {
        if (delayBetweenScansMillis < 0) {
            throw new IllegalArgumentException("Delay cannot be less than 0");
        } else {
            this.delayBetweenScansMillis = delayBetweenScansMillis;
            return this;
        }
    }

    public Ping setTimes(int noTimes) {
        if (noTimes < 0) {
            throw new IllegalArgumentException("Times cannot be less than 0");
        } else {
            this.times = noTimes;
            return this;
        }
    }

    private void setAddress(InetAddress address) {
        this.address = address;
    }

    private void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    private void resolveAddressString() throws UnknownHostException {
        if (this.address == null && this.addressString != null) {
            this.address = InetAddress.getByName(this.addressString);
        }
    }

    public void cancel() {
        this.cancelled = true;
    }

    public PingResult doPing() throws UnknownHostException {
        this.cancelled = false;
        this.resolveAddressString();
        return PingTools.doPing(this.address, this.timeOutMillis);
    }

    public Ping doPing(final Ping.PingListener pingListener) {
        executor.execute(() -> {
            try {
                Ping.this.resolveAddressString();
            } catch (UnknownHostException var12) {
                pingListener.onError(var12);
                return;
            }

            if (Ping.this.address == null) {
                pingListener.onError(new NullPointerException("Address is null"));
            } else {
                long pingsCompleted = 0L;
                long noLostPackets = 0L;
                float totalPingTime = 0.0F;
                float minPingTime = -1.0F;
                float maxPingTime = -1.0F;
                Ping.this.cancelled = false;
                int noPings = Ping.this.times;

                while (noPings > 0 || Ping.this.times == 0) {
                    PingResult pingResult = PingTools.doPing(Ping.this.address, Ping.this.timeOutMillis);
                    if (pingListener != null) {
                        pingListener.onResult(pingResult);
                    }

                    ++pingsCompleted;
                    if (pingResult.hasError()) {
                        ++noLostPackets;
                    } else {
                        float timeTaken = pingResult.getTimeTaken();
                        totalPingTime += timeTaken;
                        if (maxPingTime == -1.0F || timeTaken > maxPingTime) {
                            maxPingTime = timeTaken;
                        }

                        if (minPingTime == -1.0F || timeTaken < minPingTime) {
                            minPingTime = timeTaken;
                        }
                    }

                    --noPings;
                    if (Ping.this.cancelled) {
                        break;
                    }

                    try {
                        Thread.sleep((long) Ping.this.delayBetweenScansMillis);
                    } catch (InterruptedException var11) {
                        var11.printStackTrace();
                    }
                }

                if (pingListener != null) {
                    pingListener.onFinished(
                            new PingStats(
                                    Ping.this.address, pingsCompleted, noLostPackets,
                                    totalPingTime, minPingTime, maxPingTime
                            )
                    );
                }

            }
        });
        return this;
    }

    public interface PingListener {
        void onResult(PingResult var1);

        void onFinished(PingStats var1);

        void onError(Exception var1);
    }
}
