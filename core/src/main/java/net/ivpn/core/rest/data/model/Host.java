package net.ivpn.core.rest.data.model;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Tamim Hossain.
 Copyright (c) 2025 IVPN Limited.

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


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Host {

    @SerializedName("hostname")
    @Expose
    private String hostname;
    @SerializedName("dns_name")
    @Expose
    private String dnsName;
    @SerializedName("host")
    @Expose
    private String host;
    @SerializedName("public_key")
    @Expose
    private String publicKey;
    @SerializedName("local_ip")
    @Expose
    private String localIp;
    @SerializedName("ipv6")
    @Expose
    private Ipv6 ipv6;
    @SerializedName("load")
    @Expose
    private double load;
    @SerializedName("multihop_port")
    @Expose
    private int multihopPort;
    @SerializedName("v2ray")
    @Expose
    private String v2ray;
    @SerializedName("isp")
    @Expose
    private String isp;
    @SerializedName("obfs")
    @Expose
    private Obfs obfs;

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

    public Ipv6 getIpv6() {
        return ipv6;
    }

    public void setIpv6(Ipv6 ipv6) {
        this.ipv6 = ipv6;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getMultihopPort() {
        return multihopPort;
    }

    public void setMultihopPort(int multihopPort) {
        this.multihopPort = multihopPort;
    }

    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }

    public String getV2ray() {
        return v2ray;
    }

    public void setV2ray(String v2ray) {
        this.v2ray = v2ray;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public Obfs getObfs() {
        return obfs;
    }

    public void setObfs(Obfs obfs) {
        this.obfs = obfs;
    }

    @Override
    public String toString() {
        return "Host{" +
                "host='" + host + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", localIp='" + localIp + '\'' +
                ", ipv6=" + ipv6 +
                '}';
    }
}
