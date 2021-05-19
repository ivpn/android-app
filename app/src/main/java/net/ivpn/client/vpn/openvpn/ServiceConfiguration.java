package net.ivpn.client.vpn.openvpn;

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

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.VpnService.Builder;
import android.os.Build;
import android.text.TextUtils;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.prefs.PackagesPreference;
import net.ivpn.client.common.prefs.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Set;
import java.util.Vector;

import javax.inject.Inject;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.CIDRIP;
import de.blinkt.openvpn.core.NativeUtils;
import de.blinkt.openvpn.core.NetworkSpace;

/**
 * Class ServiceConfiguration contains menu_connect options that are used in tunnel creation
 */
public class ServiceConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfiguration.class);

    private static final String IPV6DEFAULT = "2000::";
    private static final int IPV6MASK = 3;

    private final Vector<String> dnsList = new Vector<>();
    private final NetworkSpace routes = new NetworkSpace();
    private final NetworkSpace routesV6 = new NetworkSpace();
    private String domain = null;
    private CIDRIP localIP = null;
    private int mtu;
    private String localIPv6 = null;
    private String lastTunCfg;
    private String remoteGW;

    private Settings settings;
    private PackagesPreference packagesPreference;
    /**
     * Create Tun config string based on options. The formatDate of the string is not important,
     * only that two identical configurations produce the same result.
     *
     * @return config string
     */
    @Inject
    public ServiceConfiguration(Settings settings, PackagesPreference packagesPreference) {
        this.settings = settings;
        this.packagesPreference = packagesPreference;
    }

    private String getTunConfigString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TUNCFG UNQIUE STRING ips:");

        if (localIP != null)
            builder.append(localIP.toString());
        if (localIPv6 != null)
            builder.append(localIPv6);

        builder.append("routes: ").append(TextUtils.join("|", routes.getNetworks(true)))
                .append(TextUtils.join("|", routesV6.getNetworks(true)))
                .append("excl. routes:").append(TextUtils.join("|", routes.getNetworks(false)))
                .append(TextUtils.join("|", routesV6.getNetworks(false)))
                .append("dns: ").append(getDNS())
                .append("domain: ").append(domain)
                .append("mtu: ").append(mtu);
        return builder.toString();
    }

    private String getDNS() {
        String dns = settings.getDns();
        if (dns != null) {
            return dns;
        }

        return TextUtils.join("|", dnsList);
    }

    private Vector<String> getDnsList() {
        Vector<String> result;

        String dns = settings.getDns();
        if (dns != null) {
            result = new Vector<>();
            result.add(dns);
            return result;
        }

        return dnsList;
    }

    boolean isLocalBypassEnabled() {
        return settings.getLocalBypass();
    }

    /**
     * Fill {@link Builder} with necessary parameters.
     *  @param context service's context
     * @param builder the entity that will be filled
     * @param profile current profile
     */
    void fillBuilder(Context context, Builder builder, VpnProfile profile) {
        LOGGER.info(context.getString(R.string.last_openvpn_tun_config));

        if (localIP == null && localIPv6 == null) {
            LOGGER.error(context.getString(R.string.opentun_no_ipaddr));
            return;
        }

        if (!addLocalIps(builder, profile.mAllowLocalLAN)) {
            return;
        }

        addDnsList(builder);
        addMtu(builder);
        addNotAllowedApps(builder);

        Collection<NetworkSpace.ipAddress> positiveIPv4Routes = routes.getPositiveIPList();
        Collection<NetworkSpace.ipAddress> positiveIPv6Routes = routesV6.getPositiveIPList();

        samsungWorkAround(positiveIPv4Routes);
        fillRoutes(builder, context, positiveIPv4Routes, positiveIPv6Routes);

        if (domain != null)
            builder.addSearchDomain(domain);

        logCommonInfo(positiveIPv4Routes, positiveIPv6Routes);

        // No DNS Server, log a warning
        if (dnsList.size() == 0) {
            LOGGER.info(context.getString(R.string.warn_no_dns));
        }

        lastTunCfg = getTunConfigString();
        reset();
    }

    private void addNotAllowedApps(Builder builder) {
        Set<String> disallowedApps = packagesPreference.getDisallowedPackages();
        for (String app : disallowedApps) {
            try {
                builder.addDisallowedApplication(app);
            } catch (PackageManager.NameNotFoundException exception) {
                exception.printStackTrace();
                packagesPreference.allowPackage(app);
            }
        }
    }

    /**
     * Format session string with local ip v4 or v4 and v6
     *
     * @param context services context
     * @param session basic session string that will be formatted
     * @return formatted session string
     */
    String getSessionFormatted(Context context, String session) {
        String formattedSession = null;
        if (localIP != null && localIPv6 != null)
            formattedSession = context.getString(R.string.session_ipv6string, session, localIP, localIPv6);
        else if (localIP != null)
            formattedSession = context.getString(R.string.session_ipv4string, session, localIP);
        return formattedSession;
    }

    private void addMtu(Builder builder) {
        builder.setMtu(mtu);
    }

    private void addLocalNetworksToRoutes(boolean isLocalLanAllow) {
        // Add local network interfaces
        String[] localRoutes = NativeUtils.getIfconfig();

        // The formatDate of localRoutes is kind of broken because I don't really like JNI
        for (int i = 0; i < localRoutes.length; i += 3) {
            String intf = localRoutes[i];
            String ipAddr = localRoutes[i + 1];
            String netMask = localRoutes[i + 2];

            if (intf == null || intf.equals("lo") ||
                    intf.startsWith("tun") || intf.startsWith("rmnet"))
                continue;

            if (ipAddr == null || netMask == null) {
                LOGGER.error("Local routes are broken?! (Report to author) " + TextUtils.join("|", localRoutes));
                continue;
            }

            if (localIP != null && ipAddr.equals(localIP.mIp))
                continue;

            if (isLocalLanAllow)
                routes.addIP(new CIDRIP(ipAddr, netMask), false);
        }
    }

    private boolean addLocalIps(Builder builder, boolean isLocalLanAllow) {
        if (localIP != null) {
            addLocalNetworksToRoutes(isLocalLanAllow);
            try {
                builder.addAddress(localIP.mIp, localIP.len);
            } catch (IllegalArgumentException iae) {
                LOGGER.error(String.format(getString(R.string.dns_add_error), localIP, iae.getLocalizedMessage()));
                return false;
            }
        }

        if (localIPv6 != null) {
            String[] ipv6parts = localIPv6.split("/");
            try {
                builder.addAddress(ipv6parts[0], Integer.parseInt(ipv6parts[1]));
            } catch (IllegalArgumentException iae) {
                LOGGER.error(String.format(getString(R.string.ip_add_error), localIPv6, iae.getLocalizedMessage()));
                return false;
            }
        }
        return true;
    }

    private void addDnsList(Builder builder) {
        for (String dns : getDnsList()) {
            try {
                builder.addDnsServer(dns);
            } catch (IllegalArgumentException iae) {
                LOGGER.error(String.format(getString(R.string.dns_add_error), dns, iae.getLocalizedMessage()));
            }
        }
    }

    private void fillRoutes(Builder builder, Context context, Collection<NetworkSpace.ipAddress> positiveIPv4Routes,
                            Collection<NetworkSpace.ipAddress> positiveIPv6Routes) {
        NetworkSpace.ipAddress multiCastRange = new NetworkSpace.ipAddress(new CIDRIP("224.0.0.0", 3), true);

        for (NetworkSpace.ipAddress route : positiveIPv4Routes) {
            try {
                if (multiCastRange.containsNet(route)) {
                    LOGGER.debug(String.format(getString(R.string.ignore_multicast_route), route.toString()));
                } else {
                    builder.addRoute(route.getIPv4Address(), route.networkMask);
                }
            } catch (IllegalArgumentException ia) {
                LOGGER.error(context.getString(R.string.route_rejected) + route + " " + ia.getLocalizedMessage());
            }
        }

        for (NetworkSpace.ipAddress route6 : positiveIPv6Routes) {
            try {
                builder.addRoute(route6.getIPv6Address(), route6.networkMask);
            } catch (IllegalArgumentException ia) {
                LOGGER.error(context.getString(R.string.route_rejected) + route6 + " " + ia.getLocalizedMessage());
            }
        }
        if (positiveIPv6Routes.isEmpty()) {
            try {
                builder.addRoute(IPV6DEFAULT, IPV6MASK);
            } catch (IllegalArgumentException ia) {
                LOGGER.error(context.getString(R.string.route_rejected) + "2000::" + " " + ia.getLocalizedMessage());
            }
        }
    }

    private void samsungWorkAround(Collection<NetworkSpace.ipAddress> positiveIPv4Routes) {
        if ("samsung".equals(Build.BRAND) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && dnsList.size() >= 1) {
            // Check if the first DNS Server is in the VPN range
            try {
                NetworkSpace.ipAddress dnsServer = new NetworkSpace.ipAddress(new CIDRIP(getDnsList().get(0), 32), true);
                boolean dnsIncluded = false;
                for (NetworkSpace.ipAddress net : positiveIPv4Routes) {
                    if (net.containsNet(dnsServer)) {
                        dnsIncluded = true;
                    }
                }
                if (!dnsIncluded) {
                    String samsungWarning = String.format("Warning Samsung Android 5.0+ devices ignore" +
                            " DNS servers outside the VPN range. To enable DNS resolution a route" +
                            " to your DNS Server (%s) has been added.", getDnsList().get(0));
                    LOGGER.warn(samsungWarning);
                    positiveIPv4Routes.add(dnsServer);
                }
            } catch (Exception e) {
                LOGGER.error("Error parsing DNS Server IP: " + getDnsList().get(0));
            }
        }
    }

    private void logCommonInfo(Collection<NetworkSpace.ipAddress> positiveIPv4Routes,
                               Collection<NetworkSpace.ipAddress> positiveIPv6Routes) {
        if (localIP != null) {
            LOGGER.info(String.format(getString(R.string.local_ip_info), localIP.mIp, localIP.len, localIPv6, mtu));
        }

        LOGGER.info(String.format(getString(R.string.dns_server_info), TextUtils.join(", ", dnsList), domain));
        if (routes != null && routesV6 != null) {
            LOGGER.info(String.format(getString(R.string.routes_info_incl),
                    TextUtils.join(", ", routes.getNetworks(true)),
                    TextUtils.join(", ", routesV6.getNetworks(true))));
            LOGGER.info(String.format(getString(R.string.routes_info_excl), TextUtils.join(", ",
                    routes.getNetworks(false)),
                    TextUtils.join(", ", routesV6.getNetworks(false))));
        }
        LOGGER.debug(String.format(getString(R.string.routes_debug), TextUtils.join(", ", positiveIPv4Routes),
                TextUtils.join(", ", positiveIPv6Routes)));
    }

    private void reset() {
        dnsList.clear();
        routes.clear();
        routesV6.clear();
        localIP = null;
        localIPv6 = null;
        domain = null;
    }

    void addDNS(String dns) {
        dnsList.add(dns);
    }

    void setDomain(String domain) {
        if (this.domain == null) {
            this.domain = domain;
        }
    }

    void addRoute(CIDRIP route) {
        routes.addIP(route, true);
    }

    void addRoute(String dest, String mask, String gateway, String device) {
        CIDRIP route = new CIDRIP(dest, mask);
        boolean include = isAndroidTunDevice(device);

        NetworkSpace.ipAddress gatewayIP = new NetworkSpace.ipAddress(new CIDRIP(gateway, 32), false);

        if (localIP == null) {
            LOGGER.error("Local IP address unset and received. Neither pushed server config nor local config specifies an IP addresses." +
                    " Opening tun device is most likely going to fail.");
            return;
        }
        NetworkSpace.ipAddress localNet = new NetworkSpace.ipAddress(localIP, true);
        if (localNet.containsNet(gatewayIP))
            include = true;

        if (gateway != null &&
                (gateway.equals("255.255.255.255") || gateway.equals(remoteGW)))
            include = true;


        if (route.len == 32 && !mask.equals("255.255.255.255")) {
            LOGGER.warn(String.format(getString(R.string.route_not_cidr), dest, mask));
        }

        if (route.normalise()) {
            LOGGER.warn(String.format(getString(R.string.route_not_netip), dest, route.len, route.mIp));
        }

        routes.addIP(route, include);
    }

    public void addRouteV6(String network, String device) {
        String[] v6parts = network.split("/");
        boolean included = isAndroidTunDevice(device);
        // Tun is opened after ROUTE6, no device name may be present
        try {
            Inet6Address ip = (Inet6Address) InetAddress.getAllByName(v6parts[0])[0];
            int mask = Integer.parseInt(v6parts[1]);
            routesV6.addIPv6(ip, mask, included);

        } catch (UnknownHostException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }

    void setMtu(int mtu) {
        this.mtu = mtu;
    }

    void setLocalIP(CIDRIP cdrip) {
        localIP = cdrip;
    }

    void setLocalIP(String local, String netmask, int mtu, String mode) {
        this.localIP = new CIDRIP(local, netmask);
        this.mtu = mtu;
        this.remoteGW = null;

        long netMaskAsInt = CIDRIP.getInt(netmask);

        if (localIP.len == 32 && !netmask.equals("255.255.255.255")) {
            // get the netmask as IP
            int masklen;
            long mask;
            if ("net30".equals(mode)) {
                masklen = 30;
                mask = 0xfffffffc;
            } else {
                masklen = 31;
                mask = 0xfffffffe;
            }
            // Netmask is Ip address +/-1, assume net30/p2p with small net
            if ((netMaskAsInt & mask) == (localIP.getInt() & mask)) {
                localIP.len = masklen;
            } else {
                localIP.len = 32;
                if (!"p2p".equals(mode)) {
                    LOGGER.warn(String.format(String.format(getString(R.string.ip_not_cidr), local, netmask, mode)));
                }
            }
        }
        if (("p2p".equals(mode) && localIP.len < 32) || ("net30".equals(mode) && localIP.len < 30)) {
            LOGGER.warn(String.format(getString(R.string.ip_looks_like_subnet), local, netmask, mode));
        }

        /* Workaround for Lollipop, it  does not route traffic to the VPNs own network mask */
        if (localIP.len <= 31 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CIDRIP interfaceRoute = new CIDRIP(localIP.mIp, localIP.len);
            interfaceRoute.normalise();
            addRoute(interfaceRoute);
        }
        // Configurations are sometimes really broken...
        remoteGW = netmask;
    }

    void setLocalIPv6(String ipv6addr) {
        localIPv6 = ipv6addr;
    }

    String getTunReopenStatus() {
        String currentConfiguration = getTunConfigString();
        if (currentConfiguration.equals(lastTunCfg)) {
            return "NOACTION";
        } else {
            String release = Build.VERSION.RELEASE;
            return "OPEN_BEFORE_CLOSE";
        }
    }

    private boolean isAndroidTunDevice(String device) {
        return device != null &&
                (device.startsWith("tun") || "(null)".equals(device) || "vpnservice-tun".equals(device));
    }

    private static String getString(int resId) {
        return IVPNApplication.getApplication().getString(resId);
    }
}