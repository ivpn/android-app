package net.ivpn.core.common.utils;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import net.ivpn.core.common.dagger.ApplicationScope;
import net.ivpn.core.rest.data.model.ServerType;
import net.ivpn.core.common.prefs.ServersRepository;
import net.ivpn.core.rest.data.model.Server;

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