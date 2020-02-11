package net.ivpn.client.common.pinger;

import android.util.Log;

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
