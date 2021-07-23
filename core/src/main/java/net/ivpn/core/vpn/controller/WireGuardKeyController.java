package net.ivpn.core.vpn.controller;

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

import com.wireguard.android.crypto.Keypair;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.common.prefs.EncryptedUserPreference;
import net.ivpn.core.common.prefs.ServersRepository;
import net.ivpn.core.common.prefs.Settings;
import net.ivpn.core.common.utils.DateUtil;
import net.ivpn.core.rest.HttpClientFactory;
import net.ivpn.core.rest.RequestListener;
import net.ivpn.core.rest.Responses;
import net.ivpn.core.rest.data.wireguard.AddWireGuardPublicKeyRequestBody;
import net.ivpn.core.rest.data.wireguard.AddWireGuardPublicKeyResponse;
import net.ivpn.core.rest.requests.common.Request;
import net.ivpn.core.rest.requests.common.RequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class WireGuardKeyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WireGuardKeyController.class);

    private WireGuardKeysEventsListener keysEventsListener;
    private Settings settings;
    private EncryptedUserPreference userPreference;
    private HttpClientFactory clientFactory;
    private ServersRepository serversRepository;

    private Request<AddWireGuardPublicKeyResponse> addKeyRequest = null;

    @Inject
    public WireGuardKeyController(Settings settings, EncryptedUserPreference userPreference,
                                  HttpClientFactory clientFactory, ServersRepository serversRepository) {
        this.settings = settings;
        this.userPreference = userPreference;
        this.clientFactory = clientFactory;
        this.serversRepository = serversRepository;
    }

    public void setKeysEventsListener(WireGuardKeysEventsListener keysEventsListener) {
        this.keysEventsListener = keysEventsListener;
    }

    boolean isKeysExpired() {
        if (settings.getWireGuardPublicKey().isEmpty()) return true;

        long currentTimeStamp = System.currentTimeMillis();
        long lastGeneratedV = settings.getGenerationTime();
        int regenerationPeriod = settings.getRegenerationPeriod();

        return currentTimeStamp > lastGeneratedV + regenerationPeriod * DateUtil.DAY;
    }

    boolean isKeysHardExpired() {
        if (settings.getWireGuardPublicKey().isEmpty()) return true;

        long currentTimeStamp = System.currentTimeMillis();
        long lastGeneratedV = settings.getGenerationTime();
        int regenerationPeriod = settings.getRegenerationPeriod();

        return currentTimeStamp > lastGeneratedV + regenerationPeriod * DateUtil.DAY + 3 * DateUtil.DAY;
    }

    public int getRegenerationPeriod() {
        return settings.getRegenerationPeriod();
    }

    public void putRegenerationPeriod(int regenerationPeriodI) {
        settings.setRegenerationPeriod(regenerationPeriodI);
        IVPNApplication.appComponent.provideGlobalWireGuardAlarm().start();
    }

    void startShortPeriodAlarm() {
        IVPNApplication.appComponent.provideGlobalWireGuardAlarm().startShortPeriod();
    }

    public void generateKeys() {
        LOGGER.info("generateKeys");
        keysEventsListener.onKeyGenerating();
        if (!getSessionToken().isEmpty()) {
            setKey(false);
        } else {
            //This case should not happen but for integrity, it should be here.
            keysEventsListener.onKeyGeneratedError(null, null);
        }
    }

    void regenerateLiveKeys() {
        keysEventsListener.onKeyGenerating();
        if (!getSessionToken().isEmpty()) {
            setKey(true);
        } else {
            //This case should not happen but for integrity, it should be here.
            keysEventsListener.onKeyGeneratedError(null, null);
        }
    }

    public void regenerateKeys() {
        LOGGER.info("regenerateKeys");
        keysEventsListener.onKeyGenerating();
        if (!getSessionToken().isEmpty()) {
            setKey(false);
        } else {
            //This case should not happen but for integrity, it should be here.
            keysEventsListener.onKeyGeneratedError(null, null);
        }
    }

    private void setKey(boolean provideOldKey) {
        Keypair keys = settings.generateWireGuardKeys();
        String oldPublicKey = settings.getWireGuardPublicKey();

        AddWireGuardPublicKeyRequestBody requestBody;
        if (provideOldKey) {
            requestBody = new AddWireGuardPublicKeyRequestBody(getSessionToken(),
                    keys.getPublicKey(), oldPublicKey);
        } else {
            settings.removeWireGuardKeys();
            requestBody = new AddWireGuardPublicKeyRequestBody(getSessionToken(),
                    keys.getPublicKey());
        }

        addKeyRequest = new Request<>(settings, clientFactory, serversRepository, Request.Duration.SHORT, RequestWrapper.IpMode.IPv4);
        addKeyRequest.start(api -> api.setWireGuardPublicKey(requestBody),
                new RequestListener<AddWireGuardPublicKeyResponse>() {
            @Override
            public void onSuccess(AddWireGuardPublicKeyResponse response) {
                LOGGER.info("generateKeys onSuccess " + response);
                if (response == null) {
                    keysEventsListener.onKeyGeneratedError(null, null);
                    return;
                }

                if (response.getStatus() == Responses.SUCCESS) {
                    settings.setWireGuardIpAddress(response.getIpAddress());
                    settings.saveWireGuardKeypair(keys);
                    keysEventsListener.onKeyGeneratedSuccess();
                } else {
                    keysEventsListener.onKeyGeneratedError(null, null);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.info("generateKeys onError throwable = " + throwable);
                keysEventsListener.onKeyGeneratedError(null, throwable);
            }

            @Override
            public void onError(String error) {
                LOGGER.info("generateKeys error = " + error);
                keysEventsListener.onKeyGeneratedError(error, null);
            }
        });
    }

    private String getSessionToken() {
        return userPreference.getSessionToken();
    }

    public interface WireGuardKeysEventsListener {
        void onKeyGenerating();

        void onKeyGeneratedSuccess();

        void onKeyGeneratedError(String error, Throwable throwable);
    }
}