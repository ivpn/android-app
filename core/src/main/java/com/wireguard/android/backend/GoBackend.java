/*
 * Copyright © 2018 Samuel Holland <samuel@sholland.org>
 * Copyright © 2018 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.util.Log;

import androidx.annotation.Nullable;

import com.wireguard.android.config.Config;
import com.wireguard.android.config.Peer;
import com.wireguard.android.config.InetNetwork;
import com.wireguard.android.model.Tunnel;
import com.wireguard.android.model.Tunnel.State;
import com.wireguard.android.util.SharedLibraryLoader;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.R;
import net.ivpn.core.common.dagger.ApplicationScope;
import net.ivpn.core.common.prefs.EncryptedSettingsPreference;
import net.ivpn.core.common.prefs.PackagesPreference;
import net.ivpn.core.common.prefs.ServersRepository;
import net.ivpn.core.common.prefs.ServersPreference;
import net.ivpn.core.common.prefs.Settings;
import net.ivpn.core.vpn.NetworkUtils;
import net.ivpn.core.vpn.controller.VpnBehaviorController;
import net.ivpn.core.vpn.controller.V2rayController;
import net.ivpn.core.vpn.model.ObfuscationType;
import net.ivpn.core.vpn.model.V2RaySettings;
import net.ivpn.core.vpn.wireguard.ConfigManager;
import net.ivpn.core.common.multihop.MultiHopController;
import net.ivpn.core.rest.data.model.Host;
import net.ivpn.core.rest.data.model.Server;
import net.ivpn.core.rest.data.model.ServerType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import de.blinkt.openvpn.core.CIDRIP;
import de.blinkt.openvpn.core.NetworkSpace;

@ApplicationScope
public final class GoBackend implements Backend {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoBackend.class);

    private static GhettoCompletableFuture<WireGuardVpnService> vpnService = new GhettoCompletableFuture<>();

    private final Context context;
    private VpnBehaviorController vpnBehaviorController;
    private PackagesPreference packagesPreference;
    private Settings settings;
    private EncryptedSettingsPreference encryptedSettingsPreference;
    private ServersPreference serversPreference;

    @Nullable
    private Tunnel currentTunnel;
    @Nullable
    private Config currentConfig;
    private int currentTunnelHandle = -1;

    @Inject
    GoBackend(Context context, VpnBehaviorController vpnBehaviorController,
              PackagesPreference packagesPreference, Settings settings,
              EncryptedSettingsPreference encryptedSettingsPreference,
              ServersPreference serversPreference) {
        LOGGER.info("init");
        SharedLibraryLoader.loadSharedLibrary(context, "wg-go");
        this.context = context;
        this.packagesPreference = packagesPreference;
        this.vpnBehaviorController = vpnBehaviorController;
        this.settings = settings;
        this.encryptedSettingsPreference = encryptedSettingsPreference;
        this.serversPreference = serversPreference;

        LOGGER.info("end init");
    }

    private static native String wgGetConfig(int handle);

    private static native int wgGetSocketV4(int handle);

    private static native int wgGetSocketV6(int handle);

    private static native void wgTurnOff(int handle);

    private static native int wgTurnOn(String ifName, int tunFd, String settings);

    private static native String wgVersion();

    @Override
    public String getVersion() {
        return wgVersion();
    }

    @Override
    public State getState(final Tunnel tunnel) {
        return currentTunnel == tunnel ? State.UP : State.DOWN;
    }

    @Override
    public State setState(final Tunnel tunnel, State state, @Nullable final Config config) throws Exception {
        final State originalState = getState(tunnel);
        LOGGER.info("Original state = " + originalState);

        if (state == State.TOGGLE) {
            state = originalState == State.UP ? State.DOWN : State.UP;
        }
        if (state == originalState && tunnel == currentTunnel && config == currentConfig) {
            LOGGER.info("State, config and tunnel was the same");
            return originalState;
        }
        if (state == State.UP) {
            LOGGER.info("Try to establish WireGuard connection");
            final Config originalConfig = currentConfig;
            final Tunnel originalTunnel = currentTunnel;
            if (currentTunnel != null) {
                LOGGER.info("Close previous connection");
                setStateInternal(currentTunnel, null, State.DOWN);
            }
            try {
                LOGGER.info("Start connection config");
                setStateInternal(tunnel, config, state);
            } catch (final Exception e) {
                e.printStackTrace();
                Log.d("Crash", "setState: ");
                if (originalTunnel != null)
                    setStateInternal(originalTunnel, originalConfig, State.UP);
                throw e;
            }
        } else if (state == State.DOWN && tunnel == currentTunnel) {
            LOGGER.info("Close connection");
            setStateInternal(tunnel, null, State.DOWN);
        }
        return getState(tunnel);
    }

    private static final String IPV6DEFAULT = "2000::";
    private static final int IPV6MASK = 3;

    private void fillRoutes(WireGuardVpnService.Builder builder) {
        NetworkSpace routes = getRoutes();
        Collection<NetworkSpace.ipAddress> positiveIPv4Routes = routes.getPositiveIPList();
        NetworkSpace.ipAddress multiCastRange = new NetworkSpace.ipAddress(new CIDRIP("224.0.0.0", 3), true);

        NetworkSpace routesV6 = new NetworkSpace();
        Collection<NetworkSpace.ipAddress> positiveIPv6Routes = routesV6.getPositiveIPList();

        for (NetworkSpace.ipAddress route : positiveIPv4Routes) {
            try {
                if (!multiCastRange.containsNet(route)) {
                    builder.addRoute(route.getIPv4Address(), route.networkMask);
                }
            } catch (IllegalArgumentException ia) {
                LOGGER.error(context.getString(R.string.route_rejected) + route + " " + ia.getLocalizedMessage());
            }
        }
        for (NetworkSpace.ipAddress route6 : positiveIPv6Routes) {
            try {
                builder.addRoute(route6.getIPv6Address(), route6.networkMask);
            } catch (IllegalArgumentException ia) {
                LOGGER.error(context.getString(R.string.route_rejected) + route6 + " " + ia.getLocalizedMessage());
            }
        }
        if (positiveIPv6Routes.isEmpty()) {
            try {
                builder.addRoute(IPV6DEFAULT, IPV6MASK);
            } catch (IllegalArgumentException ia) {
                LOGGER.error(context.getString(R.string.route_rejected) + "2000::" + " " + ia.getLocalizedMessage());
            }
        }

        builder.allowFamily(OsConstants.AF_INET);
        builder.allowFamily(OsConstants.AF_INET6);
    }

    private NetworkSpace getRoutes() {
        NetworkSpace routes = new NetworkSpace();
        routes.addIP(new CIDRIP("0.0.0.0", "0.0.0.0"), true);
        if (settings.getLocalBypass()) {
            for (String net : NetworkUtils.getLocalNetworks(context, false)) {
                String[] netParts = net.split("/");
                String ipAddress = netParts[0];
                int netMask = Integer.parseInt(netParts[1]);
                if (NetworkUtils.isValidLocalNetwork(ipAddress)) {
                    routes.addIP(new CIDRIP(ipAddress, netMask), false);
                }
            }
        }
        
        // Add V2Ray server IP to bypass routes (prevents circular routing)
        if (settings.getObfuscationType() != ObfuscationType.DISABLED) {
            try {
                net.ivpn.core.vpn.model.V2RaySettings v2raySettings = serversPreference.getV2RaySettings();
                if (v2raySettings != null && !v2raySettings.getOutboundIp().isEmpty()) {
                    // Add V2Ray server IP as /32 route that bypasses the VPN tunnel
                    routes.addIP(new CIDRIP(v2raySettings.getOutboundIp(), 32), false);
                    LOGGER.info("Added V2Ray server IP to bypass routes: " + v2raySettings.getOutboundIp());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to add V2Ray server IP to bypass routes: " + e.getMessage(), e);
            }
        }
        
        return routes;
    }

    private void setStateInternal(final Tunnel tunnel, @Nullable final Config config, final State state)
            throws Exception {

        if (state == State.UP) {
            LOGGER.info("Bringing tunnel up");

            Objects.requireNonNull(config, "Trying to bring up a tunnel with no config");

            if (WireGuardVpnService.prepare(context) != null)
                throw new Exception("VPN service not authorized by user");

            if (!vpnService.isDone())
                startVpnService();

            final WireGuardVpnService service;
            try {
                service = vpnService.get(2, TimeUnit.SECONDS);
            } catch (final TimeoutException e) {
                LOGGER.error("Error while starting VPN service", e);
                throw new Exception("Unable to start Android VPN service", e);
            }
            service.setOwner(this);

            if (currentTunnelHandle != -1) {
                LOGGER.info("Tunnel already up");
                return;
            }

            if (encryptedSettingsPreference.getObfuscationType() != ObfuscationType.DISABLED) {
                try {
                    String selectedPeerPublicKey = null;
                    if (config != null && config.getPeers() != null) {
                        for (final Peer peer : config.getPeers()) {
                            selectedPeerPublicKey = peer.getPublicKey();
                            break;
                        }
                    }
                    service.updateV2raySettingsInService(selectedPeerPublicKey);
                } catch (final Exception e) {
                    LOGGER.error("Failed to update V2Ray settings in service", e);
                }
                boolean v2rayOk = service.ensureV2RayStarted();
                if (!v2rayOk) {
                    throw new Exception("Unable to start V2Ray proxy");
                }
                try {
                    final String localEndpoint = service.getV2RayLocalEndpoint();
                    if (config != null && config.getPeers() != null) {
                        for (final Peer peer : config.getPeers()) {
                            peer.setEndpointString(localEndpoint);
                        }
                    }
                    LOGGER.info("Applied V2Ray local endpoint to WireGuard config: " + localEndpoint);
                } catch (final Exception e) {
                    LOGGER.error("Failed to apply V2Ray endpoint to WireGuard config", e);
                    throw new Exception("Failed to apply V2Ray endpoint", e);
                }
            }

            // Build config
            final String goConfig = config.format();

            // Create the vpn tunnel with android API
            final WireGuardVpnService.Builder builder = service.getBuilder();
            builder.setSession(tunnel.getName());

            addNotAllowedApps(builder);

            for (final InetNetwork addr : config.getInterface().getAddresses())
                builder.addAddress(addr.getAddress(), addr.getMask());

            for (final InetAddress addr : config.getInterface().getDnses())
                builder.addDnsServer(addr.getHostAddress());

            fillRoutes(builder);

            int mtu = config.getInterface().getMtu();
            if (mtu == 0)
                mtu = 1280;
            builder.setMtu(mtu);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                builder.setMetered(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                service.setUnderlyingNetworks(null);

            builder.setBlocking(true);
            try (final ParcelFileDescriptor tun = builder.establish()) {
                if (tun == null)
                    throw new Exception("Go backend v" + wgVersion());
                LOGGER.info("Tunnel already up");
                currentTunnelHandle = wgTurnOn(tunnel.getName(), tun.detachFd(), goConfig);
            }
            if (currentTunnelHandle < 0)
                throw new Exception("Unable to turn tunnel on (wgTurnOn return " + currentTunnelHandle + ')');

            currentTunnel = tunnel;
            currentConfig = config;

            service.protect(wgGetSocketV4(currentTunnelHandle));
            service.protect(wgGetSocketV6(currentTunnelHandle));
        } else {
            LOGGER.info("Bringing tunnel down");

            if (currentTunnelHandle == -1) {
                LOGGER.info("Tunnel already down");
                return;
            }

            wgTurnOff(currentTunnelHandle);
            currentTunnel = null;
            currentTunnelHandle = -1;
            currentConfig = null;

            try {
                if (vpnService.isDone()) {
                    final WireGuardVpnService service = vpnService.get(1, TimeUnit.SECONDS);
                    if (service != null) {
                        service.stopV2RayIfRunning();
                    }
                }
            } catch (final Exception ignored) {
            }
        }

        tunnel.onStateChange(state);
    }

    private void addNotAllowedApps(android.net.VpnService.Builder builder) {
        Set<String> disallowedApps = packagesPreference.getDisallowedPackages();
        for (String app : disallowedApps) {
            try {
                builder.addDisallowedApplication(app);
            } catch (PackageManager.NameNotFoundException exception) {
                exception.printStackTrace();
                packagesPreference.allowPackage(app);
            }
        }
    }

    private void startVpnService() {
        LOGGER.info("Requesting to start WireGuardVpnService");
        context.startService(new Intent(context, WireGuardVpnService.class));
    }



    public static class WireGuardVpnService extends android.net.VpnService {

        @Nullable
        private GoBackend owner;

        @Inject
        VpnBehaviorController vpnBehaviorController;
        @Inject
        ConfigManager configManager;
        @Inject
        V2rayController v2rayController;
        @Inject
        Settings settings;
        @Inject
        ServersRepository serversRepository;
        @Inject
        EncryptedSettingsPreference encryptedSettingsPreference;
        @Inject
        ServersPreference serversPreference;
        @Inject
        MultiHopController multiHopController;

        public Builder getBuilder() {
            return new Builder();
        }

        @Override
        public void onRevoke() {
            LOGGER.info("onRevoke");
            vpnBehaviorController.disconnect();
            super.onRevoke();
        }

        @Override
        public void onCreate() {
            IVPNApplication.appComponent.inject(this);
            LOGGER.info("onCreate");
            vpnService.complete(this);
            super.onCreate();
        }

        @Override
        public void onDestroy() {
            LOGGER.info("onDestroy");
            configManager.onTunnelStateChanged(State.DOWN);
            try {
                stopV2RayIfRunning();
            } catch (final Exception ignored) {
            }
            if (owner != null) {
                final Tunnel tunnel = owner.currentTunnel;
                if (tunnel != null) {
                    if (owner.currentTunnelHandle != -1)
                        wgTurnOff(owner.currentTunnelHandle);
                    owner.currentTunnel = null;
                    owner.currentTunnelHandle = -1;
                    owner.currentConfig = null;
                }
            }

            vpnService = vpnService.newIncompleteFuture();
            super.onDestroy();
        }

        @Override
        public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
            LOGGER.info("onStartCommand");
            vpnService.complete(this);
            if (intent == null || intent.getComponent() == null || !intent.getComponent().getPackageName().equals(getPackageName())) {
                LOGGER.info("Service started by Always-on VPN feature");
            }
            return super.onStartCommand(intent, flags, startId);
        }

        public void updateV2raySettingsInService(@Nullable final String selectedPeerPublicKey) {
            final ObfuscationType obfuscationType = encryptedSettingsPreference.getObfuscationType();
            serversRepository.loadV2raySettings();
            if (obfuscationType == ObfuscationType.DISABLED) {
                LOGGER.debug("V2Ray obfuscation disabled, skipping settings update (service)");
                return;
            }

            final java.util.List<Server> servers = serversRepository.getServers(false);
            if (servers == null || servers.isEmpty()) {
                LOGGER.error("No servers available, cannot configure V2Ray (service)");
                return;
            }

            V2RaySettings currentSettings = serversPreference.getV2RaySettings();
            if (currentSettings == null) {
                LOGGER.error("V2Ray base configuration not found (service)");
                return;
            }
            if (currentSettings.getId().isEmpty()) {
                LOGGER.error("V2Ray user ID is empty, authentication will fail (service)");
                return;
            }

            Server entryServer = serversRepository.getCurrentServer(ServerType.ENTRY);
            if (entryServer == null || entryServer.getHosts().isEmpty()) {
                LOGGER.error("Entry server not available, cannot configure V2Ray (service)");
                return;
            }

            Host entryHost = null;
            if (selectedPeerPublicKey != null) {
                for (Host h : entryServer.getHosts()) {
                    if (h != null && selectedPeerPublicKey.equals(h.getPublicKey())) {
                        entryHost = h;
                        break;
                    }
                }
            }
            if (entryHost == null) {
                entryHost = selectHostWithV2ray(entryServer.getHosts());
            }
            if (entryHost == null) {
                LOGGER.error("Entry server has no host with V2Ray configured, attempting offline refresh (service)");
                try {
                    serversRepository.updateServerListOffline();
                    java.util.List<Server> refreshed = serversRepository.getServers(false);
                    if (refreshed != null) {
                        for (Server s : refreshed) {
                            if (s != null && s.equals(entryServer)) {
                                serversRepository.setCurrentServer(ServerType.ENTRY, s);
                                break;
                            }
                        }
                    }
                    entryServer = serversRepository.getCurrentServer(ServerType.ENTRY);
                    if (entryServer != null && !entryServer.getHosts().isEmpty()) {
                        entryHost = selectHostWithV2ray(entryServer.getHosts());
                    }
                } catch (Throwable t) {
                    LOGGER.error("Failed to refresh servers list offline (service)", t);
                }
                if (entryHost == null) {
                    LOGGER.error("Entry server has no host with V2Ray configuration after refresh (service)");
                    return;
                }
            }

            String v2rayInboundIp = entryHost.getHost() != null ? entryHost.getHost() : "";
            int v2rayInboundPort = currentSettings.getSingleHopInboundPort();
            String v2rayOutboundIp = entryHost.getV2ray();

            int v2rayOutboundPort = switch (obfuscationType) {
                case V2RAY_TCP -> 80;
                case V2RAY_QUIC -> 443;
                default -> settings.getWireGuardPort().getPortNumber();
            };

            String v2rayDnsName = entryHost.getDnsName() != null ? entryHost.getDnsName()
                    : (entryHost.getHostname() != null ? entryHost.getHostname() : "");

            if (v2rayInboundIp.isEmpty() || v2rayOutboundIp.isEmpty()) {
                LOGGER.error("Critical V2Ray IPs are empty (service) - inbound: '" + v2rayInboundIp + "', outbound: '" + v2rayOutboundIp + "'");
                return;
            }

            if (multiHopController.isReadyToUse()) {
                Server exitServer = serversRepository.getCurrentServer(ServerType.EXIT);
                if (exitServer != null && !exitServer.getHosts().isEmpty()) {
                    Host exitHost = null;
                    if (selectedPeerPublicKey != null) {
                        for (Host h : exitServer.getHosts()) {
                            if (h != null && selectedPeerPublicKey.equals(h.getPublicKey())) {
                                exitHost = h;
                                break;
                            }
                        }
                    }
                    if (exitHost == null) {
                        exitHost = exitServer.getHosts().get(0);
                    }
                    v2rayInboundIp = exitHost.getHost() != null ? exitHost.getHost() : "";
                    v2rayInboundPort = settings.getWireGuardPort().getPortNumber();
                    LOGGER.info("Multi-hop V2Ray inbound set to ExitServer WG endpoint (service): " + exitHost.getHost() + ":" + v2rayInboundPort);
                } else {
                    LOGGER.error("Multi-hop enabled but no exit server available (service)");
                    return;
                }
            }

            LOGGER.info("V2Ray endpoints (service) → outbound (entry VMess): " + v2rayOutboundIp + ":" + v2rayOutboundPort
                    + ", inbound (WG target): " + v2rayInboundIp + ":" + v2rayInboundPort
                    + ", obfuscation: " + obfuscationType);

            V2RaySettings v2raySettings = new V2RaySettings(
                    currentSettings.getId(),
                    v2rayOutboundIp,
                    v2rayOutboundPort,
                    v2rayInboundIp,
                    v2rayInboundPort,
                    v2rayDnsName,
                    currentSettings.getWireguard()
            );

            serversPreference.putV2RaySettings(v2raySettings);
        }

        private Host selectHostWithV2ray(java.util.List<Host> hosts) {
            if (hosts == null || hosts.isEmpty()) return null;
            for (Host h : hosts) {
                if (h != null && h.getV2ray() != null && !h.getV2ray().isEmpty()) {
                    return h;
                }
            }
            return null;
        }

        public boolean ensureV2RayStarted() {
            try {
                return v2rayController.startIfEnabled();
            } catch (final Exception e) {
                LOGGER.error("Failed to start V2Ray in service", e);
                return false;
            }
        }

        public String getV2RayLocalEndpoint() {
            try {
                return v2rayController.getLocalProxyEndpoint();
            } catch (final Exception e) {
                LOGGER.error("Failed to get V2Ray local endpoint", e);
                return "127.0.0.1:0";
            }
        }

        public void stopV2RayIfRunning() {
            try {
                v2rayController.stop();
            } catch (final Exception e) {
                LOGGER.error("Failed to stop V2Ray in service", e);
            }
        }

        public void setOwner(final GoBackend owner) {
            this.owner = owner;
        }
    }

    private static final class GhettoCompletableFuture<V> {
        private final LinkedBlockingQueue<V> completion = new LinkedBlockingQueue<>(1);
        private final FutureTask<V> result = new FutureTask<>(completion::peek);

        public boolean complete(final V value) {
            final boolean offered = completion.offer(value);
            if (offered)
                result.run();
            return offered;
        }

        public V get() throws ExecutionException, InterruptedException {
            return result.get();
        }

        public V get(final long timeout, final TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
            return result.get(timeout, unit);
        }

        public boolean isDone() {
            return !completion.isEmpty();
        }

        public GhettoCompletableFuture<V> newIncompleteFuture() {
            return new GhettoCompletableFuture<>();
        }
    }
}