package net.ivpn.client.rest;

import net.ivpn.client.BuildConfig;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Singleton
public class HttpClientFactory {
    private static final String BASE_URL = BuildConfig.BASE_URL;

    @Inject
    public HttpClientFactory() {
    }

    public OkHttpClient getHttpClient(int timeOut) {

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(getInterceptor());

        httpClientBuilder.hostnameVerifier(getHostnameVerifier());
        httpClientBuilder.readTimeout(timeOut, TimeUnit.SECONDS);
        httpClientBuilder.connectTimeout(3, TimeUnit.SECONDS);

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

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, session) -> HttpsURLConnection.getDefaultHostnameVerifier().verify(BASE_URL, session);
    }
}