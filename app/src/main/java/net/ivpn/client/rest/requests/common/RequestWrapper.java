package net.ivpn.client.rest.requests.common;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.IVPNApi;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.Responses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;

import javax.inject.Inject;

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

    private LinkedList<String> ips;
    private String lastUsedIp;
    private String testingIp;

    private boolean isCancelled;

    private CallBuilder<T> callBuilder;
    private OkHttpClient httpClient;
    private Call<T> call;
    private RequestListener listener;

    private Settings settings;
    private ServersRepository serversRepository;

    RequestWrapper(Settings settings, HttpClientFactory httpClientFactory, ServersRepository serversRepository,
                   int timeOut) {
        this.settings = settings;
        this.serversRepository = serversRepository;
        this.httpClient = httpClientFactory.getHttpClient(timeOut);
    }

    void setRequestListener(RequestListener listener) {
        this.listener = listener;
    }

    void setCallBuilder(CallBuilder<T> callBuilder) {
        this.callBuilder = callBuilder;
    }

    void perform() {
        LOGGER.info("Perform with testingIp = " + testingIp);
        String baseUrl = generateURL(testingIp);
        perform(baseUrl);
    }

    private void perform(String baseUrl) {
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
        LOGGER.info("Get next url and try again");
        if (getIps() == null) {
            listener.onError(throwable);
        }
        testingIp = getTestingIp();

        String baseUrl;
        if (testingIp == null) {
            testingIp = getIps().getFirst();
        } else if (testingIp.equals(getIps().getLast())) {
            testingIp = null;
        } else {
            int indexOf = getIps().indexOf(testingIp);
            testingIp = getIps().get(indexOf + 1);
        }

        if ((testingIp == null && getLastUsedIp() == null)
                || (testingIp != null && testingIp.equals(getLastUsedIp()))) {
            listener.onError(throwable);
            return;
        }

        LOGGER.info("Perform with testingIp = " + testingIp);
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
    public void onResponse(Call<T> call, Response<T> response) {
        LOGGER.info("Response received");
        if (isCancelled || response == null || listener == null) return;

        LOGGER.info("getTestingIp = " + getTestingIp() + " lastUsedIp = " + getLastUsedIp());
        if ((getTestingIp() == null && getLastUsedIp() != null) || (getTestingIp() != null && !getTestingIp().equals(getLastUsedIp()))) {
            LOGGER.info("Set " + getTestingIp() + " as stable");
            settings.setLastUsedIp(getTestingIp());
        }

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
    public void onFailure(Call<T> call, Throwable throwable) {
        LOGGER.error("Failed with ip = " + getTestingIp() + " ", throwable);
        if (isCancelled) {
            return;
        }
        getNextBaseUrlAndTry(throwable);
    }

    public interface CallBuilder<T> {
        Call<T> createCall(IVPNApi api);
    }

    private String getLastUsedIp() {
        if (lastUsedIp == null) {
            lastUsedIp = settings.getLastUsedIp();
        }

        return lastUsedIp;
    }

    private String getTestingIp() {
//        if (testingIp == null) {
//            testingIp = getLastUsedIp();
//        }

        return testingIp;
    }

    private LinkedList<String> getIps() {
        if (ips == null) {
            ips = settings.getIpList();
        }
        if (ips == null) {
            serversRepository.tryUpdateIpList();
            ips = settings.getIpList();
        }

        return ips;
    }
}