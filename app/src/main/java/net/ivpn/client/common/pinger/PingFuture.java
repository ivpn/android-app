package net.ivpn.client.common.pinger;

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

class PingFuture {

    private static final String TAG = PingFuture.class.getSimpleName();
    private static final int TIMES = 2;
    private static final int TIMEOUT = 1000;

    private boolean isFinished;
    private volatile PingResultFormatter result;
    private volatile OnPingFinishListener listener;
    private ExecutorService executor;

    PingFuture(ExecutorService executor) {
        this.executor = executor;
        isFinished = false;
    }

    Runnable getPingRunnable(final String ipAddress, final OnPingFinishListener listener) {
        this.listener = listener;
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
                        if (PingFuture.this.listener != null) {
                            PingFuture.this.listener.onPingFinish(result);
                        }
//                        Log.d(TAG, "onFinished: pingStats = " + pingStats);
//                        Log.d(TAG, "onFinished: ip = " + ipAddress + " ping = " + pingStats.getMinTimeTaken());
                    }

                    @Override
                    public void onError(Exception e) {
                        isFinished = true;
                        result = new PingResultFormatter(PingResultFormatter.PingResult.OFFLINE, -1);
                        if (PingFuture.this.listener != null) {
                            PingFuture.this.listener.onPingFinish(result);
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

    void updateOnPingFinishListener(OnPingFinishListener listener) {
        this.listener = listener;
    }
}