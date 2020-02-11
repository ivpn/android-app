package net.ivpn.client.rest.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Config {

    @SerializedName("antitracker")
    @Expose
    private AntiTracker antitracker;
    @SerializedName("api")
    @Expose
    private Api api;

    public AntiTracker getAntiTracker() {
        return antitracker;
    }

    public void setAntiTracker(AntiTracker antitracker) {
        this.antitracker = antitracker;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

}