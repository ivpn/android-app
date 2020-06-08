package net.ivpn.client.rest.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.ivpn.client.rest.data.model.Config;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.vpn.Protocol;

import java.util.List;

public class ServersListResponse {
    @SerializedName("wireguard")
    @Expose
    private List<Server> wireGuard = null;
    @SerializedName("openvpn")
    @Expose
    private List<Server> openvpn = null;

    @SerializedName("config")
    @Expose
    private Config config = null;

    public List<Server> getWireGuardServerList() {
        return wireGuard;
    }

    public void setWireGuardServerList(List<Server> wireguard) {
        this.wireGuard = wireguard;
    }

    public List<Server> getOpenVpnServerList() {
        return openvpn;
    }

    public void setOpenVpnServerList(List<Server> openvpn) {
        this.openvpn = openvpn;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void markServerTypes() {
        for (Server server : wireGuard) {
            server.setType(Protocol.WireGuard);
        }
        for (Server server : openvpn) {
            server.setType(Protocol.OpenVPN);
        }
    }
}
