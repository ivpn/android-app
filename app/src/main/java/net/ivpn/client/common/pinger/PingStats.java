package net.ivpn.client.common.pinger;

import java.net.InetAddress;

public class PingStats {
    private final InetAddress ia;
    private final long noPings;
    private final long packetsLost;
    private final float averageTimeTaken;
    private final float minTimeTaken;
    private final float maxTimeTaken;
    private final boolean isReachable;

    public PingStats(InetAddress ia, long noPings, long packetsLost, float totalTimeTaken, float minTimeTaken, float maxTimeTaken) {
        this.ia = ia;
        this.noPings = noPings;
        this.packetsLost = packetsLost;
        this.averageTimeTaken = totalTimeTaken / (float) noPings;
        this.minTimeTaken = minTimeTaken;
        this.maxTimeTaken = maxTimeTaken;
        this.isReachable = noPings - packetsLost > 0L;
    }

    public InetAddress getAddress() {
        return this.ia;
    }

    public long getNoPings() {
        return this.noPings;
    }

    public long getPacketsLost() {
        return this.packetsLost;
    }

    public float getAverageTimeTaken() {
        return this.averageTimeTaken;
    }

    public float getMinTimeTaken() {
        return this.minTimeTaken;
    }

    public float getMaxTimeTaken() {
        return this.maxTimeTaken;
    }

    public boolean isReachable() {
        return this.isReachable;
    }

    public long getAverageTimeTakenMillis() {
        return (long) (this.averageTimeTaken * 1000.0F);
    }

    public long getMinTimeTakenMillis() {
        return (long) (this.minTimeTaken * 1000.0F);
    }

    public long getMaxTimeTakenMillis() {
        return (long) (this.maxTimeTaken * 1000.0F);
    }

    public String toString() {
        return "PingStats{ia=" + this.ia + ", noPings=" + this.noPings + ", packetsLost=" + this.packetsLost + ", averageTimeTaken=" + this.averageTimeTaken + ", minTimeTaken=" + this.minTimeTaken + ", maxTimeTaken=" + this.maxTimeTaken + '}';
    }
}
