package net.ivpn.core.rest.data.model;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

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

import net.ivpn.core.v2.serverlist.dialog.Filters;
import net.ivpn.core.v2.serverlist.items.ConnectionOption;
import net.ivpn.core.vpn.Protocol;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

public class Server implements ConnectionOption {

    public static Comparator<Server> comparator = (server1, server2) -> {
        int countryCode = server1.countryCode.compareTo(server2.countryCode);
        if (countryCode != 0) {
            return countryCode;
        }
        return server1.city.compareTo(server2.city);
    };

    @SerializedName("gateway")
    @Expose
    private String gateway;
    @SerializedName("country_code")
    @Expose
    private String countryCode;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("latitude")
    @Expose
    private double latitude;

    @SerializedName("longitude")
    @Expose
    private double longitude;
    @SerializedName("ip_addresses")
    @Expose
    private List<String> ipAddresses = null;
    @SerializedName("hosts")
    @Expose
    private List<Host> hosts = null;
    @SerializedName("protocol")
    @Expose
    private Protocol type;

    private boolean isFavourite = false;
    private long latency = Long.MAX_VALUE;
    private float distance = Long.MAX_VALUE;

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<String> getIpAddresses() {
        if (hosts == null || hosts.isEmpty()) return "";
        List<String> addresses = new ArrayList<String>();
        for (Host host : hosts) {
            addresses.add(host.getHost());
        }
        return addresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public String getDescription() {
        return city + ", " + countryCode;
    }

    public String getDescription(Filters filter) {
        if (filter == Filters.COUNTRY) return countryCode + ", " + city;
        return getDescription();
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    public Protocol getType() {
        return type;
    }

    public void setType(Protocol type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public boolean canBeUsedAsMultiHopWith(Server server) {
        if (server == null) return true;
        return !this.countryCode.equalsIgnoreCase(server.countryCode);
    }

    public boolean isPingInfoSameWith(Server server) {
        return this.equals(server) && this.latency == server.latency;
    }

    public boolean isIPv6Enabled() {
        if (hosts == null || hosts.isEmpty()) return false;
        return hosts.get(0).getIpv6() != null
                && hosts.get(0).getIpv6().getLocal_ip() != null
                && !hosts.get(0).getIpv6().getLocal_ip().isEmpty();
    }

    public String getIpAddress() {
        if (hosts == null || hosts.isEmpty()) return "";
        return hosts.get(0).getHost();
    }

    @Override
    public String toString() {
        return "Server{" +
                "gateway='" + gateway + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", ipAddresses=" + ipAddresses +
                ", hosts=" + hosts +
                ", type=" + type +
                ", isFavourite=" + isFavourite +
                ", latency=" + latency +
                ", distance=" + distance +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Server)) {
            return false;
        }
        Server other = (Server) obj;
        if (!Objects.equals(this.countryCode, other.countryCode)) {
            return false;
        }
        return Objects.equals(this.city, other.city);
    }

    @Override
    public int hashCode() {
        if (gateway != null) {
            return gateway.hashCode();
        }
        return super.hashCode();
    }
}
