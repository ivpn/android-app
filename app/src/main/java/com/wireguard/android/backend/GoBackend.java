/*
 * Copyright © 2018 Samuel Holland <samuel@sholland.org>
 * Copyright © 2018 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;

import com.wireguard.android.config.Config;
import com.wireguard.android.config.InetNetwork;
import com.wireguard.android.config.Interface;
import com.wireguard.android.config.Peer;
import com.wireguard.android.crypto.KeyEncoding;
import com.wireguard.android.model.Tunnel;
import com.wireguard.android.model.Tunnel.State;
import com.wireguard.android.util.SharedLibraryLoader;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.PackagesPreference;
import net.ivpn.client.ui.connect.ConnectActivity;
import net.ivpn.client.vpn.controller.VpnBehaviorController;
import net.ivpn.client.vpn.wireguard.ConfigManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Formatter;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import java9.util.concurrent.CompletableFuture;

@ApplicationScope
public final class GoBackend implements Backend {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoBackend.class);

    private static CompletableFuture<WireGuardVpnService> vpnService = new CompletableFuture<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Context context;
    private VpnBehaviorController vpnBehaviorController;
    private PackagesPreference packagesPreference;

    @Nullable
    private Tunnel currentTunnel;
    private int currentTunnelHandle = -1;

    @Inject
    GoBackend(Context context, VpnBehaviorController vpnBehaviorController,
              PackagesPreference packagesPreference) {
        LOGGER.info("init");
        SharedLibraryLoader.loadSharedLibrary(context, "wg-go");
        this.context = context;
        this.packagesPreference = packagesPreference;
        this.vpnBehaviorController = vpnBehaviorController;

        LOGGER.info("end init");
    }

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
    public State setState(final Tunnel tunnel, State state) {
        LOGGER.info("setState tunnel = " + tunnel);
        LOGGER.info("setState state = " + state);
        final State originalState = getState(tunnel);
        LOGGER.info("setState originalState = " + originalState);
        if (state == State.TOGGLE)
            state = originalState == State.UP ? State.DOWN : State.UP;
        if (state == originalState)
            return originalState;
        if (state == State.UP && currentTunnel != null)
            throw new IllegalStateException("Only one userspace tunnel can run at a time");
        LOGGER.info("Changing tunnel " + tunnel.getName() + " to state " + state);
        State finalState = state;
        executorService.execute(() -> {
            try {
                setStateInternal(tunnel, tunnel.getConfig(), finalState);
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        });
        return getState(tunnel);
    }

    private void setStateInternal(final Tunnel tunnel, @Nullable final Config config, final State state)
            throws Exception {

        if (state == State.UP) {
            LOGGER.info("Bringing tunnel up");

            Objects.requireNonNull(config, "Trying to bring up a tunnel with no config");

            if (WireGuardVpnService.prepare(context) != null)
                throw new Exception("VPN service not authorized by user");

            final WireGuardVpnService service;
            if (!vpnService.isDone())
                startVpnService();

            try {
                service = vpnService.get(2, TimeUnit.SECONDS);
            } catch (final TimeoutException e) {
                LOGGER.error("Error while starting VPN service", e);
                throw new Exception("Unable to start Android VPN service", e);
            }

            if (currentTunnelHandle != -1) {
                LOGGER.info("Tunnel already up");
                return;
            }

            // Build config
            final String goConfig = config.format();

            // Create the vpn tunnel with android API
            final WireGuardVpnService.Builder builder = service.getBuilder();
            builder.setSession(tunnel.getName());

            final Intent configureIntent = new Intent(context, ConnectActivity.class);
            configureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            builder.setConfigureIntent(PendingIntent.getActivity(context, 0, configureIntent, 0));

            addNotAllowedApps(builder);
//            for (final String excludedApplication : config.getInterface().getExcludedApplications())
//                builder.addDisallowedApplication(excludedApplication);

            for (final InetNetwork addr : config.getInterface().getAddresses())
                builder.addAddress(addr.getAddress(), addr.getMask());

            for (final InetAddress addr : config.getInterface().getDnses())
                builder.addDnsServer(addr.getHostAddress());

            for (final Peer peer : config.getPeers()) {
                for (final InetNetwork addr : peer.getAllowedIPs())
                    builder.addRoute(addr.getAddress(), addr.getMask());
            }

            int mtu = config.getInterface().getMtu();
            if (mtu == 0)
                mtu = 1280;
            builder.setMtu(mtu);

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
            vpnBehaviorController.connectActionByRules();

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
        }
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

        @Inject
        VpnBehaviorController vpnBehaviorController;
        @Inject
        ConfigManager configManager;

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
            IVPNApplication.getApplication().appComponent.inject(this);
            LOGGER.info("onCreate");
            vpnService.complete(this);
            super.onCreate();
        }

        @Override
        public void onDestroy() {
            LOGGER.info("onDestroy");
            configManager.onTunnelStateChanged(State.DOWN);

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
    }
}