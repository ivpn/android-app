/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.os.Build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

public class VpnStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(VpnStatus.class);

    private static Vector<StateListener> listeners;

    public static ConnectionStatus lastLevel = ConnectionStatus.LEVEL_NOTCONNECTED;

    public static boolean isVPNActive() {
        return lastLevel != ConnectionStatus.LEVEL_AUTH_FAILED && lastLevel != ConnectionStatus.LEVEL_NOTCONNECTED
                && lastLevel != ConnectionStatus.LEVEL_NONETWORK && lastLevel != ConnectionStatus.UNKNOWN_LEVEL;
    }

    static {
        listeners = new Vector<>();
        logInformation();
    }

    public interface StateListener {
        void updateState(ConnectionStatus level);
    }

    private static void logInformation() {
        String nativeAPI;
        try {
            nativeAPI = NativeUtils.getNativeAPI();
        } catch (UnsatisfiedLinkError ignore) {
            nativeAPI = "error";
        }

        LOGGER.info(String.format("%10$s %9$s running on %3$s %1$s (%2$s), Android %6$s (%7$s) API %4$d, ABI %5$s, (%8$s)",
                Build.MODEL, Build.BOARD, Build.BRAND, Build.VERSION.SDK_INT,
                nativeAPI, Build.VERSION.RELEASE, Build.ID, Build.FINGERPRINT, "", ""));
    }

    public synchronized static void addStateListener(StateListener stateListener) {
        LOGGER.debug("Add listener");
        if (!listeners.contains(stateListener)) {
            listeners.add(stateListener);
            if (lastLevel != null)
                stateListener.updateState(lastLevel);
        }
    }

    public static ConnectionStatus getLevel(String state) {
        String[] noreplyet = {"CONNECTING", "WAIT", "RECONNECTING", "RESOLVE", "TCP_CONNECT"};
        String[] reply = {"AUTH", "GET_CONFIG", "ASSIGN_IP", "ADD_ROUTES"};
        String[] connected = {"CONNECTED"};
        String[] notconnected = {"DISCONNECTED", "EXITING"};

        for (String x : noreplyet)
            if (state.equals(x))
                return ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET;

        for (String x : reply)
            if (state.equals(x))
                return ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED;

        for (String x : connected)
            if (state.equals(x))
                return ConnectionStatus.LEVEL_CONNECTED;

        for (String x : notconnected)
            if (state.equals(x))
                return ConnectionStatus.LEVEL_NOTCONNECTED;

        return ConnectionStatus.UNKNOWN_LEVEL;
    }

    static void updateStatePause(OpenVPNManagement.pauseReason pauseReason) {
        switch (pauseReason) {
            case noNetwork:
                VpnStatus.updateStateString("NONETWORK", ConnectionStatus.LEVEL_NONETWORK);
                break;
            case screenOff:
                VpnStatus.updateStateString("SCREENOFF", ConnectionStatus.LEVEL_VPNPAUSED);
                break;
            case userPause:
                VpnStatus.updateStateString("USERPAUSE", ConnectionStatus.LEVEL_VPNPAUSED);
                break;
        }
    }

    public synchronized static void removeStateListener(StateListener stateListener) {
        LOGGER.debug("Remove listener");
        listeners.remove(stateListener);
    }

    public synchronized static void updateStateString(String state) {
        updateStateString(state, getLevel(state));
    }

    public synchronized static void updateStateString(String state, ConnectionStatus level) {
        // Workaround for OpenVPN doing AUTH and wait and being connected
        // Simply ignore these state
        if (lastLevel == ConnectionStatus.LEVEL_CONNECTED &&
                (state.equals("WAIT") || state.equals("AUTH"))) {
            LOGGER.debug("Ignoring OpenVPN Status in CONNECTED state %s", state);
            return;
        }

        lastLevel = level;

        for (StateListener stateListener : listeners) {
            stateListener.updateState(level);
        }
        LOGGER.debug(String.format("New OpenVPN InitState %s", state));
    }
}
