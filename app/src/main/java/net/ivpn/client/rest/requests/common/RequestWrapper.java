package net.ivpn.client.rest.requests.common;

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

import android.os.Build;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.IVPNApi;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.Responses;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RequestWrapper<T> implements Callback<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestWrapper.class);

    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static final String HTTPS = "https://";
    private static final String SLASH = "/";

    private final IpMode mode;
    private LinkedList<String> ips;
    private String testingIp;

    private volatile boolean isCancelled;

    private CallBuilder<T> callBuilder;
    private final OkHttpClient httpClient;
    private Call<T> call;
    private RequestListener listener;

    private final Settings settings;
    private final ServersRepository serversRepository;

    private AtomicInteger currentAttempt = new AtomicInteger(0);
    private int maxAttempts;

    RequestWrapper(Settings settings, HttpClientFactory httpClientFactory, ServersRepository serversRepository,
                   int timeOut, IpMode mode) {
        this.settings = settings;
        this.serversRepository = serversRepository;
        this.httpClient = httpClientFactory.getHttpClient(timeOut);
        this.mode = mode;
    }

    void setRequestListener(RequestListener listener) {
        this.listener = listener;
    }

    void setCallBuilder(CallBuilder<T> callBuilder) {
        this.callBuilder = callBuilder;
    }

    void perform() {
        testingIp = getIps().isEmpty() ? null : getIps().getFirst();
        LOGGER.info("Perform with testingIp = " + testingIp);
        String baseUrl = generateURL(testingIp);
        LOGGER.info("Perform with baseUrl = " + baseUrl);
        perform(baseUrl);
    }

    private void perform(String baseUrl) {
        currentAttempt.incrementAndGet();
        call = callBuilder.createCall(generateApi(baseUrl));
        call.enqueue(this);
    }

    private String generateURL(String ip) {
        if (ip == null) {
            return HTTPS + BASE_URL + SLASH;
        }
        return HTTPS + ip + SLASH;
    }

    private void getNextBaseUrlAndTry(Throwable throwable) {
        LOGGER.info("Get next url and try again currentAttempt = " + currentAttempt.get()
                + " maxAttempts = " + maxAttempts);
        if (currentAttempt.get() > maxAttempts || getIps() == null) {
            currentAttempt.set(0);
            listener.onError(throwable);
            return;
        }

        String baseUrl;
        if (testingIp == null) {
            testingIp = getIps().getFirst();
        } else if (testingIp.equals(getIps().getLast())) {
            switch (mode) {
                case IPv6:
                    testingIp = getIps().getFirst();
                    break;
                case IPv4:
                case BOTH:
                    testingIp = null;
                    break;
            }
        } else {
            int index = getIps().indexOf(testingIp);
            testingIp = getIps().get(index + 1);
        }

        baseUrl = generateURL(testingIp);
        perform(baseUrl);
    }

    public void cancel() {
        if (call == null) return;
        isCancelled = true;
        call.cancel();
    }

    private IVPNApi generateApi(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()
                .create(IVPNApi.class);
    }

    @Override
    public void onResponse(@NotNull Call<T> call,@NotNull Response<T> response) {
        LOGGER.info("Response received for " + mode);
        currentAttempt.set(0);
        if (isCancelled || listener == null) return;

        if (response.code() == Responses.SUCCESS) {
            listener.onSuccess(response.body());
        } else {
            String error = null;
            try {
                error = response.errorBody().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            listener.onError(error);
        }
    }

    @Override
    public void onFailure(@NotNull Call<T> call,@NotNull Throwable throwable) {
        LOGGER.error("Failed with ip = " + getTestingIp() + " ", throwable);
        if (isCancelled) {
            return;
        }
        getNextBaseUrlAndTry(throwable);
    }

    public interface CallBuilder<T> {
        Call<T> createCall(IVPNApi api);
    }

    private String getTestingIp() {
        return testingIp;
    }

    private LinkedList<String> getIps() {
        if (ips == null) {
            ips = getIpsModeBased();
        }
        if (ips.isEmpty()) {
            serversRepository.tryUpdateIpList();
            ips = getIpsModeBased();
        }

        return ips;
    }

    private LinkedList<String> getIpsModeBased() {
        LinkedList<String> ips = new LinkedList<>();
        LinkedList<String> tempIps;
        switch (mode) {
            case IPv4:
                tempIps = settings.getIpList();
                if (tempIps != null) {
                    ips.addAll(tempIps);
                }
                break;
            case IPv6:
                tempIps = settings.getIpv6List();
                for (String ip :tempIps) {
                    ips.add("[" + ip +"]");
                }
                break;
            case BOTH:
                tempIps = settings.getIpList();
                if (tempIps != null) {
                    ips.addAll(tempIps);
                }
                tempIps = settings.getIpv6List();
                for (String ip :tempIps) {
                    ips.add("[" + ip +"]");
                }
                break;
        }
        maxAttempts = ips.size();
        LOGGER.info("Sex max attempts count as " + maxAttempts);

        return ips;
    }

    public enum IpMode {
        IPv4,
        IPv6,
        BOTH
    }
}