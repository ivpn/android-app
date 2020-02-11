package net.ivpn.client.rest.data.proofs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationResponse {
    @SerializedName("ip_address")
    @Expose
    private String ipAddress;
    @SerializedName("country_code")
    @Expose
    private String countryCode;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("isIvpnServer")
    @Expose
    private Boolean isIvpnServer;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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

    public Boolean getIsIvpnServer() {
        return isIvpnServer;
    }

    public void setIsIvpnServer(Boolean isIvpnServer) {
        this.isIvpnServer = isIvpnServer;
    }

    @Override
    public String toString() {
        return "LocationResponse{" +
                "ipAddress='" + ipAddress + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", isIvpnServer=" + isIvpnServer +
                '}';
    }
}
