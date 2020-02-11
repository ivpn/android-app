package net.ivpn.client.rest;

public interface RequestListener<T> {

    void onSuccess(T response);

    void onError(Throwable throwable);

    void onError(String string);

}