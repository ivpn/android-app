package net.ivpn.client.common.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.rest.data.model.Server;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.URL;

import javax.inject.Inject;

@ApplicationScope
public class DomainResolver implements Serializable {

    private static final String TAG = DomainResolver.class.getSimpleName();

    private Handler handler;
    private boolean isResolved;

    private ServersRepository serversRepository;

    @Inject
    DomainResolver(ServersRepository serversRepository) {
        this.serversRepository = serversRepository;
    }

    public void tryResolveCurrentServerDomain(OnDomainResolvedListener listener) {
        Server currentServer = serversRepository.getCurrentServer(ServerType.ENTRY);
        tryResolveServerDomain(currentServer, listener);
    }

    public void tryResolveServerDomain(Server server, OnDomainResolvedListener listener) {
        Log.d(TAG, "tryResolveServerDomain: ");
        isResolved = false;
        Handler handler = getHandler();
        handler.removeCallbacksAndMessages(null);
        handler.post(getRunnable(server, listener));
    }

    private Runnable getRunnable(final Server server, final OnDomainResolvedListener listener) {
        return () -> {
            try {
                URL url = new URL("https://" + server.getGateway());
                InetAddress address = InetAddress.getByName(url.getHost());
                isResolved = address != null;
                Log.d(TAG, "run: isResolved = " + isResolved);
                if (listener != null) {
                    listener.onResult(isResolved);
                }
            } catch (Exception exception) {
                Log.d(TAG, "run: isResolved = false");
                exception.printStackTrace();
                if (listener != null) {
                    listener.onResult(false);
                }
            }
        };
    }

    public boolean isResolved() {
        Log.d(TAG, "isResolved: ");
        return isResolved;
    }

    private Handler getHandler() {
        if (handler == null) {
            HandlerThread handlerThread = new HandlerThread(DomainResolver.class.getSimpleName());
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        return handler;
    }

    public interface OnDomainResolvedListener {
        void onResult(boolean isResolved);
    }
}