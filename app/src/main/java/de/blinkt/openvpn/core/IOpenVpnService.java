package de.blinkt.openvpn.core;

import android.content.Context;
import android.os.ParcelFileDescriptor;

public interface IOpenVpnService {
    void processDied();

    Context getContext();

    boolean protectSocket(int socket);

    void addDNS(String dns);

    void setDomain(String domain);

    void addRoute(String dest, String mask, String gateway, String device);

    void addRoutev6(String network, String device);

    void setLocalIP(String local, String netmask, int mtu, String mode);

    void setLocalIPv6(String ipv6addr);

    String getTunReopenStatus();

    ParcelFileDescriptor openTun();
}
