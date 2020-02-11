package net.ivpn.client.rest.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AntiTracker {

    @SerializedName("default")
    @Expose
    private Default _default;
    @SerializedName("hardcore")
    @Expose
    private Hardcore hardcore;

    public Default getDefault() {
        return _default;
    }

    public void setDefault(Default _default) {
        this._default = _default;
    }

    public Hardcore getHardcore() {
        return hardcore;
    }

    public void setHardcore(Hardcore hardcore) {
        this.hardcore = hardcore;
    }

    public class Hardcore {

        @SerializedName("ip")
        @Expose
        private String ip;
        @SerializedName("multihop-ip")
        @Expose
        private String multihopIp;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getMultihopIp() {
            return multihopIp;
        }

        public void setMultihopIp(String multihopIp) {
            this.multihopIp = multihopIp;
        }

    }

    public class Default {

        @SerializedName("ip")
        @Expose
        private String ip;
        @SerializedName("multihop-ip")
        @Expose
        private String multihopIp;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getMultihopIp() {
            return multihopIp;
        }

        public void setMultihopIp(String multihopIp) {
            this.multihopIp = multihopIp;
        }

    }
}