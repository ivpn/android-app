package net.ivpn.client.common.pinger;

import android.os.Handler;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.OnServerListUpdatedListener;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.utils.DateUtil;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.vpn.OnProtocolChangedListener;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@ApplicationScope
public class PingProvider {

    private static final long VALIDITY_PERIOD = DateUtil.HOUR;
    private static final long CALCULATION_PERIOD = 3 * 1000;
    private static final int THREAD_COUNTS = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(PingProvider.class);
    private long lastCalculationTimeStamp;

    private HashMap<Server, PingFuture> pings;
    private ExecutorService pingExecutor;
    private ExecutorService featureExecutor;
    private Protocol lastPingedProtocol;
    private boolean needToFindNewlyFastestServer = false;

    private ProtocolController protocolController;
    private ServersRepository serversRepository;

    @Inject
    PingProvider(ProtocolController protocolController, ServersRepository serversRepository) {
        this.protocolController = protocolController;
        this.serversRepository = serversRepository;

        pings = new HashMap<>();
        //In the library that used to ping servers for every "ping" request creates new background thread,
        // so don't need to do it by ourselves.
        featureExecutor = Executors.newSingleThreadExecutor();
        pingExecutor = Executors.newFixedThreadPool(THREAD_COUNTS);

        serversRepository.addOnServersListUpdatedListener(getOnServerListUpdatedListener());
        protocolController.addOnProtocolChangedListener(getOnProtocolChangedListener());
    }

    public void pingAll(boolean shouldUseHardReset) {
        LOGGER.info("Ping servers if needed: should be reset " + shouldUseHardReset);
        Protocol currentProtocol = protocolController.getCurrentProtocol();
        if (currentProtocol.equals(lastPingedProtocol) && !(needToFindNewlyFastestServer
                || shouldUseHardReset || isFrequencyLimitationSatisfied())) {
            return;
        }
        List<Server> servers = serversRepository.getServers(false);
        if (servers == null) {
            return;
        }
        lastPingedProtocol = currentProtocol;
        pings = new HashMap<>();
        lastCalculationTimeStamp = System.currentTimeMillis();
        pingAll(servers);
    }

    public void ping(Server server, OnPingFinishListener listener) {
        if (server == null) {
            return;
        }
        PingFuture pingFutures = pings.get(server);
        if (pingFutures == null) {
            pingFutures = new PingFuture(pingExecutor);
            String ipAddress;
            if (server.getType() == null || server.getType().equals(Protocol.OpenVPN)) {
                ipAddress = server.getIpAddresses().get(0);
            } else {
                ipAddress = server.getHosts().get(0).getHost();
            }
            featureExecutor.execute(pingFutures.getPingRunnable(ipAddress, listener));
            pings.put(server, pingFutures);
        } else if (pingFutures.isFinished()) {
            if (listener != null)
                listener.onPingFinish(pingFutures.getResult());
        } else {
            pingFutures.updateOnPingFinishListener(listener);
        }
    }

    public void findFastestServer(final OnFastestServerDetectorListener listener) {
        long currentTime = System.currentTimeMillis();
        if (isFinished() || (currentTime - lastCalculationTimeStamp) > CALCULATION_PERIOD) {
            sendFastestServer(listener);
            return;
        }

        new Handler().postDelayed(() -> sendFastestServer(listener),
                CALCULATION_PERIOD - (currentTime - lastCalculationTimeStamp));
    }

    private boolean isFrequencyLimitationSatisfied() {
        long currentTimeStamp = System.currentTimeMillis();
        return lastCalculationTimeStamp == 0 || (currentTimeStamp - lastCalculationTimeStamp > VALIDITY_PERIOD);
    }

    private void pingAll(List<Server> servers) {
        LOGGER.info("Pinging servers...");
        if (servers == null) {
            return;
        }
        for (Server server : servers) {
            ping(server, null);
        }
    }

    private boolean isFinished() {
        for (PingFuture future : pings.values()) {
            if (!future.isFinished()) {
                return false;
            }
        }
        return true;
    }

    private Server findFastestServer() {
        LOGGER.info("Finding fastest server...");
        List<Server> possibleServersList = serversRepository.getPossibleServersList();
        Server fastestServer = possibleServersList.get(0);
        long fastestPing = -1;
        long serverPing;
        for (Server server : pings.keySet()) {
            if (!possibleServersList.contains(server)) {
                continue;
            }
            serverPing = getPingFor(server);
            if (serverPing == -1) {
                continue;
            }
            if (fastestPing == -1 || fastestPing > serverPing) {
                fastestPing = serverPing;
                fastestServer = server;
            }
        }
        return fastestServer;
    }

    private long getPingFor(Server server) {
        PingFuture future = pings.get(server);
        if (!future.isFinished()) {
            return -1;
        }
        if (future.getResult() == null || !future.getResult().isPingAvailable()) {
            return -1;
        }
        return future.getResult().getPing();
    }

    private void sendFastestServer(OnFastestServerDetectorListener listener) {
        Server fastestServer = findFastestServer();
        if (fastestServer == null) {
            LOGGER.info("Send default server as fastest one");
            needToFindNewlyFastestServer = true;
            fastestServer = getDefaultServer();
            listener.onDefaultServerApplied(fastestServer);
        } else {
            LOGGER.info("Send fastest server");
            needToFindNewlyFastestServer = false;
            listener.onFastestServerDetected(fastestServer);
        }
    }

    private Server getDefaultServer() {
        return serversRepository.getDefaultServer(ServerType.ENTRY);
    }

    private OnServerListUpdatedListener getOnServerListUpdatedListener() {
        return new OnServerListUpdatedListener() {
            @Override
            public void onSuccess(List<Server> servers, boolean isForced) {
                if (isForced) {
                    pingAll(true);
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onError() {
            }
        };
    }

    private OnProtocolChangedListener getOnProtocolChangedListener() {
        return protocol -> {
            if (protocol == null) {
                return;
            }
            pingAll(true);
        };
    }
}