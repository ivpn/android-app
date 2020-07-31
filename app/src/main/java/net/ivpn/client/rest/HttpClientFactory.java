package net.ivpn.client.rest;

import android.util.Log;

import net.ivpn.client.BuildConfig;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Singleton
public class HttpClientFactory {
    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static OkHttpClient httpClient;

    @Inject
    public HttpClientFactory() {
    }

    public OkHttpClient getHttpClient(int timeOut, LinkedList<String> ips) {
//        if (httpClient != null) {
//            return httpClient;
//        }
//        this.ips = ips;

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(getInterceptor());

//        ConnectionPool connectionPool = new ConnectionPool(1, 3, TimeUnit.SECONDS);
//        httpClientBuilder.connectionPool(connectionPool);

//        httpClientBuilder.cache(null);
//        httpClientBuilder.readTimeout(1, TimeUnit.MINUTES);
//        httpClientBuilder.writeTimeout(1, TimeUnit.MINUTES);
//        httpClientBuilder.connectTimeout(1, TimeUnit.MINUTES);

        httpClientBuilder.hostnameVerifier(getHostnameVerifier());
        httpClientBuilder.readTimeout(timeOut, TimeUnit.SECONDS);
        httpClientBuilder.connectTimeout(3, TimeUnit.SECONDS);
        httpClient = httpClientBuilder.build();

        return httpClient;
    }

    private Interceptor getInterceptor() {
        return chain -> {
            Request.Builder requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader("Content-Type", "application/json");
            requestBuilder.addHeader("Accept", "application/json");
            requestBuilder.addHeader("User-Agent", "ivpn/android");
//            requestBuilder.addHeader("Cache-Control", "no-cache");
            Request request = requestBuilder
                    .build();
            Log.d("HttpClientFactory", "getInterceptor: request " + request + " \nrequestBuilder = " + requestBuilder);
            return chain.proceed(request);
        };
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, session) -> HttpsURLConnection.getDefaultHostnameVerifier().verify("api.ivpn.net", session);
    }

    private static void shutdownHttpClient(OkHttpClient client) {
//        executor.submit(() -> {
//            try {
//                client.dispatcher().executorService().shutdown();
//                client.connectionPool().evictAll();
//                if (client.cache() != null) {
//                    client.cache().close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
    }
}