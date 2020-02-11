package net.ivpn.client.rest.requests.common;

import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.requests.common.RequestWrapper;

public class Request<T> {

    private RequestWrapper<T> requestWrapper;

    public Request(Settings settings, HttpClientFactory httpClientFactory, ServersRepository serversRepository,
                   Duration duration) {
        int timeOut = duration == Duration.SHORT ? 10 : 30;
        requestWrapper = new RequestWrapper<T>(settings, httpClientFactory, serversRepository, timeOut);
    }

    public void start(RequestWrapper.CallBuilder<T> callBuilder, RequestListener listener) {
        requestWrapper.setCallBuilder(callBuilder);
        requestWrapper.setRequestListener(listener);
        requestWrapper.perform();
    }

    public void cancel() {
        if (requestWrapper != null) {
            requestWrapper.cancel();
        }
    }

    public enum Duration {
        SHORT,
        LONG;
    }
}