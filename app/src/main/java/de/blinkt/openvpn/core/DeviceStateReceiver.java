/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.preference.PreferenceManager;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static de.blinkt.openvpn.core.OpenVPNManagement.pauseReason;

public class DeviceStateReceiver extends BroadcastReceiver implements OpenVPNManagement.PausedStateCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceStateReceiver.class);

    private final Handler mDisconnectHandler;
    private int lastNetwork = -1;
    private OpenVPNManagement mManagement;

    connectState network = connectState.DISCONNECTED;
    connectState screen = connectState.SHOULDBECONNECTED;
    connectState userpause = connectState.SHOULDBECONNECTED;

    private String lastStateMsg = null;
    private Runnable mDelayDisconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (!(network == connectState.PENDINGDISCONNECT))
                return;

            network = connectState.DISCONNECTED;

            // Set screen state to be disconnected if disconnect pending
            if (screen == connectState.PENDINGDISCONNECT)
                screen = connectState.DISCONNECTED;

            mManagement.pause(getPauseReason());
        }
    };
    private NetworkInfo lastConnectedNetwork;

    @Override
    public boolean shouldBeRunning() {
        return shouldBeConnected();
    }

    private enum connectState {
        SHOULDBECONNECTED,
        PENDINGDISCONNECT,
        DISCONNECTED
    }

    public DeviceStateReceiver(OpenVPNManagement management) {
        super();
        mManagement = management;
        mManagement.setPauseCallback(this);
        mDisconnectHandler = new Handler();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            networkStateChange(context);
        } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            // Network was disabled because screen off
            boolean connected = shouldBeConnected();
            screen = connectState.SHOULDBECONNECTED;

            /* We should connect now, cancel any outstanding disconnect timer */
            mDisconnectHandler.removeCallbacks(mDelayDisconnectRunnable);
            /* should be connected has changed because the screen is on now, connect the VPN */
            if (shouldBeConnected() != connected)
                mManagement.resume();
            else if (!shouldBeConnected())
                /*Update the reason why we are still paused */
                mManagement.pause(getPauseReason());
        }
    }

    public static boolean equalsObj(Object a, Object b) {
        return Objects.equals(a, b);
    }

    public void networkStateChange(Context context) {
        LOGGER.info("networkStateChange: ");
        NetworkInfo networkInfo = getCurrentNetworkInfo(context);

        String netstatestring;
        if (networkInfo == null) {
            netstatestring = "not connected";
        } else {
            String subtype = networkInfo.getSubtypeName();
            LOGGER.info("networkStateChange: subtype = " + subtype);
            if (subtype == null)
                subtype = "";
            String extrainfo = networkInfo.getExtraInfo();
            if (extrainfo == null)
                extrainfo = "";

            netstatestring = String.format("%2$s %3$s to %1$s", networkInfo.getTypeName(),
                    networkInfo.getDetailedState(), subtype);
            LOGGER.info("networkStateChange: netstatestring = " + netstatestring);
        }

        if (networkInfo != null && networkInfo.getState() == State.CONNECTED) {
            int newnet = networkInfo.getType();
            LOGGER.info("networkStateChange: newnet = " + newnet);

            boolean pendingDisconnect = (network == connectState.PENDINGDISCONNECT);
            LOGGER.info("networkStateChange: pendingDisconnect = " + pendingDisconnect);
            network = connectState.SHOULDBECONNECTED;

            boolean sameNetwork;
            if (lastConnectedNetwork == null
                    || lastConnectedNetwork.getType() != networkInfo.getType()
                    || !equalsObj(lastConnectedNetwork.getExtraInfo(), networkInfo.getExtraInfo())
                    ) {
                sameNetwork = false;
                LOGGER.info("networkStateChange: lastNetwork = " + lastNetwork);
                if (lastNetwork != -1) {
                    disconnect(context);
                }
            } else {
                sameNetwork = true;
            }
            /* Same network, connection still 'established' */
            LOGGER.info("networkStateChange: pendingDisconnect && sameNetwork = " + (pendingDisconnect && sameNetwork));
            if (pendingDisconnect && sameNetwork) {
//                mDisconnectHandler.removeCallbacks(mDelayDisconnectRunnable);
                // Reprotect the sockets just be sure
                mManagement.networkChange(true);
            } else {
                /* Different network or connection not established anymore */

                if (screen == connectState.PENDINGDISCONNECT)
                    screen = connectState.DISCONNECTED;
                LOGGER.info("networkStateChange: screen = " + screen);
                if (shouldBeConnected()) {
                    LOGGER.info("networkStateChange: shouldBeConnected = true");

                    if (pendingDisconnect || !sameNetwork) {
                        mManagement.networkChange(sameNetwork);
                        LOGGER.info("networkStateChange: mManagement.networkChange(" + sameNetwork + ") called");
                    } else {
                        mManagement.resume();
                        LOGGER.info("networkStateChange: mManagement.resume() called");
                    }
                }

                lastNetwork = newnet;
                lastConnectedNetwork = networkInfo;
            }
        } else if (networkInfo == null) {
            disconnect(context);
        }

        if (!netstatestring.equals(lastStateMsg)) {
            LOGGER.info(String.format(getString(R.string.netstatus), netstatestring));
        }
        lastStateMsg = netstatestring;
    }

    private void disconnect(Context context) {
        LOGGER.info("disconnect: ");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean sendusr1 = prefs.getBoolean("netchangereconnect", true);
        LOGGER.info("networkStateChange: networkInfo == null");
        // Not connected, stop openvpn, set last connected network to no network
        lastNetwork = -1;
        LOGGER.info("networkStateChange: ");
        if (sendusr1) {
            network = connectState.DISCONNECTED;

            // Set screen state to be disconnected if disconnect pending
            if (screen == connectState.PENDINGDISCONNECT)
                screen = connectState.DISCONNECTED;

            mManagement.pause(getPauseReason());
        }
        LOGGER.info("networkStateChange: sendusr1 = " + sendusr1);
    }

    private boolean shouldBeConnected() {
        return (screen == connectState.SHOULDBECONNECTED && userpause == connectState.SHOULDBECONNECTED &&
                network == connectState.SHOULDBECONNECTED);
    }

    private pauseReason getPauseReason() {
        if (userpause == connectState.DISCONNECTED)
            return pauseReason.userPause;

        if (screen == connectState.DISCONNECTED)
            return pauseReason.screenOff;

        if (network == connectState.DISCONNECTED)
            return pauseReason.noNetwork;

        return pauseReason.userPause;
    }

    private NetworkInfo getCurrentNetworkInfo(Context context) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return conn.getActiveNetworkInfo();
    }

    private static String getString(int resId) {
        return IVPNApplication.getApplication().getString(resId);
    }
}