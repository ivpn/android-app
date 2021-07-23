package net.ivpn.core.rest;

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

import android.util.Log;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.common.BuildController;
import net.ivpn.core.common.dagger.ApplicationScope;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import okhttp3.CertificatePinner;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@ApplicationScope
public class HttpClientFactory {

    private BuildController buildController;
    private String baseUrl;

    @Inject
    public HttpClientFactory(BuildController buildController) {
        this.buildController = buildController;
        baseUrl = buildController.getBaseUrl();
    }

    public OkHttpClient getHttpClient(int timeOut) {
        Log.d("HttpClientFactory", "getHttpClient: BASE_URL = " + baseUrl);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(getInterceptor());

        httpClientBuilder.hostnameVerifier(getHostnameVerifier());
        httpClientBuilder.certificatePinner(getCertificatePinner());
        httpClientBuilder.readTimeout(timeOut, TimeUnit.SECONDS);
        httpClientBuilder.connectTimeout(timeOut, TimeUnit.SECONDS);

        return httpClientBuilder.build();
    }

    private Interceptor getInterceptor() {
        return chain -> {
            Request.Builder requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader("Content-Type", "application/json");
            requestBuilder.addHeader("Accept", "application/json");
            requestBuilder.addHeader("User-Agent", "ivpn/android");
            Request request = requestBuilder
                    .build();
            return chain.proceed(request);
        };
    }

    private CertificatePinner getCertificatePinner() {
        CertificatePinner.Builder builder = new CertificatePinner.Builder();

        if (IVPNApplication.config.isProductionApi()) {
            builder.add(baseUrl,
                    "sha256/g6WEFnt9DyTi70nW/fufsZNw83vFpcmIhMuDPQ1MFcI=",
                    "sha256/KCcpK9y22OrlapwO1/oP8q3LrcDM9Jy9lcfngg2r+Pk=",
                    "sha256/iRHkSbdOY/YD8EE5fpl8W0P8EqmfkBRTADEegR2/Wnc=",
                    "sha256/47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
        } else {
            builder.add(baseUrl,
                    "sha256/Jl+pK4qpKGVHQAUOvJOpuu3blkJeZNqHrHKTJTvslDY=",
                    "sha256/U9XDB04u2rzA7daBcxHKzCtePOhDSp1x1LY6rf2TRXU=",
                    "sha256/3cEBzcOsAm+pfk5F24jbWulvqtS4ECzAYSjEqOKm4Pw=",
                    "sha256/sTkDAlpsHzTakpXj8SGCE1rXL8qlmYW77vn4WWHnLLc=");
        }

        return builder.build();
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, session) -> HttpsURLConnection.getDefaultHostnameVerifier().verify(baseUrl, session);
    }
}