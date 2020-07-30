package net.ivpn.client.common.pinger;

import android.util.Log;

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
                        Log.d(TAG, "onFinished: pingStats = " + pingStats);
                        Log.d(TAG, "onFinished: ip = " + ipAddress + " ping = " + pingStats.getMinTimeTaken());
//                        if (PingFuture.this.listener != null) {
//                            PingFuture.this.listener.forEach({
//                                    onPingFinish(server, result);
//                            });onPingFinish(server, result);
//                        }
//                        Log.d(TAG, "onFinished: pingStats = " + pingStats);
//                        Log.d(TAG, "onFinished: ip = " + ipAddress + " ping = " + pingStats.getMinTimeTaken());
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
//                        for (OnPingFinishListener listener: listeners) {
//                            listener.onPingFinish(server, result);
//                        }
//                        if (PingFuture.this.listener != null) {
//                            PingFuture.this.listener.onPingFinish(server, result);
//                        }
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
//        listeners.add(listener);
//        this.listener = listener;
    }
}