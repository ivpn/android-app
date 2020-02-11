package net.ivpn.client.rest.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Host {

    @SerializedName("host")
    @Expose
    private String host;
    @SerializedName("public_key")
    @Expose
    private String publicKey;
    @SerializedName("local_ip")
    @Expose
    private String localIp;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    @Override
    public String toString() {
        return "Host{" +
                "host='" + host + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", localIp='" + localIp + '\'' +
                '}';
    }
}
