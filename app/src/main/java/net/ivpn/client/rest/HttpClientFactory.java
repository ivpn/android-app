package net.ivpn.client.rest;

import net.ivpn.client.BuildConfig;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Singleton
public class HttpClientFactory {
    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static OkHttpClient httpClient;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public HttpClientFactory() {
    }

    public OkHttpClient getHttpClient(int timeOut) {
        if (httpClient != null) {
            return httpClient;
        }
//        if (httpClient != null) {
//            shutdownHttpClient(httpClient);
//        }

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(getInterceptor());

        ConnectionPool connectionPool = new ConnectionPool(1, 3, TimeUnit.SECONDS);
        httpClientBuilder.connectionPool(connectionPool)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS);

//        httpClientBuilder.cache(null);
//        httpClientBuilder.readTimeout(1, TimeUnit.MINUTES);
//        httpClientBuilder.writeTimeout(1, TimeUnit.MINUTES);
//        httpClientBuilder.connectTimeout(1, TimeUnit.MINUTES);

        httpClientBuilder.hostnameVerifier(getHostnameVerifier());
        httpClientBuilder.readTimeout(timeOut, TimeUnit.SECONDS);
        httpClientBuilder.connectTimeout(timeOut, TimeUnit.SECONDS);
        httpClient = httpClientBuilder.build();

        return httpClient;
    }

    private Interceptor getInterceptor() {
        return chain -> {
            Request.Builder requestBuilder = chain.request().newBuilder();
            requestBuilder.header("Content-Type", "application/json");
            requestBuilder.header("Accept", "application/json");
            requestBuilder.header("User-Agent", "ivpn/android");
            return chain.proceed(requestBuilder.build());
        };
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, session) -> HttpsURLConnection.getDefaultHostnameVerifier().verify(BASE_URL, session);
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