/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.security.KeyChain;
import android.security.KeyChainException;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import de.blinkt.openvpn.core.Connection;
import de.blinkt.openvpn.core.NativeUtils;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.X509Utils;

public class VpnProfile implements Serializable, Cloneable {

    private static final Logger LOGGER = LoggerFactory.getLogger(VpnProfile.class);
    // Note that this class cannot be moved to core where it belongs since
    // the profile loading depends on it being here
    // The Serializable documentation mentions that class name change are possible
    // but the how is unclear
    //
    // Don't change this, not all parts of the program use this constant
    public static final String INLINE_TAG = "[[INLINE]]";
    private static final String DISPLAYNAME_TAG = "[[NAME]]";

    private static final long serialVersionUID = 7085688938959334563L;
    private static final int MAXLOGLEVEL = 4;
    private static final int CURRENT_PROFILE_VERSION = 6;
    public static String DEFAULT_DNS1 = "8.8.8.8";
    private static String DEFAULT_DNS2 = "8.8.4.4";

    public static final int TYPE_CERTIFICATES = 0;
    private static final int TYPE_PKCS12 = 1;
    public static final int TYPE_KEYSTORE = 2;
    public static final int TYPE_USERPASS = 3;
    public static final int TYPE_STATICKEYS = 4;
    public static final int TYPE_USERPASS_CERTIFICATES = 5;
    private static final int TYPE_USERPASS_PKCS12 = 6;
    public static final int TYPE_USERPASS_KEYSTORE = 7;
    public static final int X509_VERIFY_TLSREMOTE = 0;
    public static final int X509_VERIFY_TLSREMOTE_COMPAT_NOREMAPPING = 1;
    public static final int X509_VERIFY_TLSREMOTE_DN = 2;
    public static final int X509_VERIFY_TLSREMOTE_RDN = 3;
    public static final int X509_VERIFY_TLSREMOTE_RDN_PREFIX = 4;

    public static final int AUTH_RETRY_NONE_FORGET = 0;
    public static final int AUTH_RETRY_NOINTERACT = 2;
    // variable named wrong and should haven beeen transient
    // but needs to keep wrong name to guarante loading of old
    // profiles
    public int mAuthenticationType = TYPE_KEYSTORE;
    public String mName;
    public String mAlias;
    public String mClientCertFilename;
    public String mTLSAuthDirection = "";
    public String mTLSAuthFilename;
    public String mClientKeyFilename;
    public String mCaFilename;
    public boolean mUseLzo = true;
    public String mPKCS12Filename;
    public String mPKCS12Password;
    public boolean mUseTLSAuth = false;

    public String mDNS1 = DEFAULT_DNS1;
    public String mDNS2 = DEFAULT_DNS2;
    public String mIPv4Address;
    public boolean mOverrideDNS = false;
    public String mSearchDomain = "blinkt.de";
    public boolean mUseDefaultRoute = true;
    public boolean mUsePull = true;
    public String mCustomRoutes;
    public boolean mCheckRemoteCN = true;
    public boolean mExpectTLSCert = false;
    public String mRemoteCN = "";
    public String mPassword = "";
    public String mUsername = "";
    public boolean mRoutenopull = false;
    public boolean mUseRandomHostname = false;
    public boolean mUseFloat = false;
    public boolean mUseCustomConfig = false;
    public String mCustomConfigOptions = "";
    public String mVerb = "1";  //ignored
    public String mCipher = "";
    public boolean mNobind = false;
    public boolean mUseDefaultRoutev6 = true;
    public String mCustomRoutesv6 = "";
    public boolean mPersistTun = false;
    public String mConnectRetryMax = "-1";
    public String mConnectRetry = "2";
    public String mConnectRetryMaxTime = "300";
    public String mAuth = "";
    public int mX509AuthType = X509_VERIFY_TLSREMOTE_RDN;
    public String mx509UsernameField = null;

    private transient PrivateKey mPrivateKey;
    // Public attributes, since I got mad with getter/setter
    // set members to default values
    private UUID mUuid;
    public boolean mAllowLocalLAN;
    private int mProfileVersion;
    public String mExcludedRoutes;
    public int mMssFix = 0; // -1 is default,
    public Connection[] mConnections;
    public boolean mRemoteRandom = false;
    private HashSet<String> mAllowedAppsVpn = new HashSet<>();

    public String mCrlFilename;

    public int mAuthRetry = AUTH_RETRY_NONE_FORGET;
    public int mTunMtu;

    public boolean mPushPeerInfo = false;
    private static final boolean mIsOpenVPN22 = false;

    public int mVersion = 0;

    /* Options no longer used in new profiles */
    public String mServerName = "openvpn.example.com";
    public String mServerPort = "1194";
    private boolean mUseUdp = true;
    public List<String> ipAddresses;

    public VpnProfile(String name) {
        mUuid = UUID.randomUUID();
        mName = name;
        mProfileVersion = CURRENT_PROFILE_VERSION;

        mConnections = new Connection[1];
        mConnections[0] = new Connection();
    }

    public static String openVpnEscape(String unescaped) {
        if (unescaped == null)
            return null;
        String escapedString = unescaped.replace("\\", "\\\\");
        escapedString = escapedString.replace("\"", "\\\"");
        escapedString = escapedString.replace("\n", "\\n");

        if (escapedString.equals(unescaped) && !escapedString.contains(" ") &&
                !escapedString.contains("#") && !escapedString.contains(";")
                && !escapedString.equals(""))
            return unescaped;
        else
            return '"' + escapedString + '"';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VpnProfile) {
            VpnProfile vpnProfile = (VpnProfile) obj;
            return mUuid.equals(vpnProfile.mUuid);
        } else {
            return false;
        }
    }

    public void clearDefaults() {
        mServerName = "unknown";
        mUsePull = false;
        mUseLzo = false;
        mUseDefaultRoute = false;
        mUseDefaultRoutev6 = false;
        mExpectTLSCert = false;
        mCheckRemoteCN = false;
        mPersistTun = false;
        mAllowLocalLAN = true;
        mPushPeerInfo = false;
        mMssFix = 0;
    }

    public UUID getUUID() {
        return mUuid;
    }

    public String getName() {
        if (TextUtils.isEmpty(mName))
            return "No profile name";
        return mName;
    }

    public void upgradeProfile() {
        if (mProfileVersion < 2) {
            /* default to the behaviour the OS used */
            mAllowLocalLAN = false;
        }

        if (mProfileVersion < 4) {
            moveOptionsToConnection();
        }
        if (mAllowedAppsVpn == null)
            mAllowedAppsVpn = new HashSet<>();
        if (mConnections == null)
            mConnections = new Connection[0];

        mProfileVersion = CURRENT_PROFILE_VERSION;
    }

    public void moveOptionsToConnection() {
        mConnections = new Connection[1];
        Connection conn = new Connection();

        conn.mServerName = mServerName;
        conn.mServerPort = mServerPort;
        conn.mUseUdp = mUseUdp;
        conn.mCustomConfiguration = "";
        conn.ipAddresses = ipAddresses;

        mConnections[0] = conn;

    }

    private String getConfigFile(Context context, boolean configForOvpn3) {

        File cacheDir = context.getCacheDir();
        StringBuilder cfg = new StringBuilder();

        // Enable management interface
        cfg.append("# Enables connection to GUI\n");
        cfg.append( "management ");

        cfg.append(cacheDir.getAbsolutePath()).append("/").append("mgmtsocket");
        cfg.append( " unix\n");
        cfg.append( "management-client\n");
        // Not needed, see updated man page in 2.3
        //cfg.append( "management-signal\n";
        cfg.append( "management-query-passwords\n");
        cfg.append( "management-hold\n\n");

        if (!configForOvpn3) {
            cfg.append( String.format("setenv IV_GUI_VER %s \n", openVpnEscape(getVersionEnvString(context))));
            String versionString = String.format(Locale.US, "%d %s %s %s %s %s", Build.VERSION.SDK_INT, Build.VERSION.RELEASE,
                    NativeUtils.getNativeAPI(), Build.BRAND, Build.BOARD, Build.MODEL);
            cfg.append( String.format("setenv IV_PLAT_VER %s\n", openVpnEscape(versionString)));
        }

        cfg.append( "machine-readable-output\n");
        cfg.append( "allow-recursive-routing\n");

        // Users are confused by warnings that are misleading...
        cfg.append( "ifconfig-nowarn\n");

        boolean useTLSClient = (mAuthenticationType != TYPE_STATICKEYS);

        if (useTLSClient && mUsePull)
            cfg.append( "client\n");
        else if (mUsePull)
            cfg.append( "pull\n");
        else if (useTLSClient)
            cfg.append( "tls-client\n");


        //cfg.append( "verb " + mVerb + "\n";
        cfg.append( "verb " + MAXLOGLEVEL + "\n");

        if (mConnectRetryMax == null) {
            mConnectRetryMax = "-1";
        }

        if (!mConnectRetryMax.equals("-1"))
            cfg.append("connect-retry-max ").append(mConnectRetryMax).append("\n");

        if (TextUtils.isEmpty(mConnectRetry))
            mConnectRetry = "2";

        if (TextUtils.isEmpty(mConnectRetryMaxTime))
            mConnectRetryMaxTime = "300";


        if (!mIsOpenVPN22)
            cfg.append("connect-retry ").append(mConnectRetry).append(" ")
                    .append(mConnectRetryMaxTime).append("\n");
        else if (mIsOpenVPN22 && mUseUdp)
            cfg.append("connect-retry ").append(mConnectRetry).append("\n");


        cfg.append( "resolv-retry 60\n");
//
//        cfg.append( "--ping-restart 0\n";


        // We cannot use anything else than tun
        cfg.append( "dev tun\n");


        boolean canUsePlainRemotes = true;

        if (mConnections.length == 1) {
            cfg.append(mConnections[0].getConnectionBlock());
        } else {
            for (Connection conn : mConnections) {
                canUsePlainRemotes = canUsePlainRemotes && conn.isOnlyRemote();
            }

            if (mRemoteRandom)
                cfg.append( "remote-random\n");

            if (canUsePlainRemotes) {
                for (Connection conn : mConnections) {
                    if (conn.mEnabled) {
                        cfg.append(conn.getConnectionBlock());
                    }
                }
            }
        }

        switch (mAuthenticationType) {
            case VpnProfile.TYPE_USERPASS_CERTIFICATES:
                cfg.append( "auth-user-pass\n");
            case VpnProfile.TYPE_CERTIFICATES:
                // Ca
                cfg.append(insertFileData("ca", mCaFilename));

                // Client Cert + Key
                cfg.append(insertFileData("key", mClientKeyFilename));
                cfg.append(insertFileData("cert", mClientCertFilename));

                break;
            case VpnProfile.TYPE_USERPASS_PKCS12:
                cfg.append("auth-user-pass\n");
            case VpnProfile.TYPE_PKCS12:
                cfg.append(insertFileData("pkcs12", mPKCS12Filename));
                break;

            case VpnProfile.TYPE_USERPASS_KEYSTORE:
                cfg.append("auth-user-pass\n");
            case VpnProfile.TYPE_KEYSTORE:
                if (!configForOvpn3) {
                    String[] ks = getKeyStoreCertificates(context);
                    cfg.append("### From Keystore ####\n");
                    if (ks != null) {
                        cfg.append("<ca>\n").append(ks[0]).append("\n</ca>\n");
                        if (ks[1] != null)
                            cfg.append("<extra-certs>\n").append(ks[1]).append("\n</extra-certs>\n");
                        cfg.append("<cert>\n").append(ks[2]).append("\n</cert>\n");
                        cfg.append( "management-external-key\n");
                    } else {
                        cfg.append(context.getString(R.string.keychain_access)).append("\n");
                    }
                }
                break;
            case VpnProfile.TYPE_USERPASS:
                cfg.append("auth-user-pass\n");
                cfg.append(insertFileData("ca", mCaFilename));
        }

        if (isUserPWAuth()) {
            if (mAuthenticationType == AUTH_RETRY_NOINTERACT)
                cfg.append("auth-retry nointeract");
        }

        if (!TextUtils.isEmpty(mCrlFilename))
            cfg.append(insertFileData("crl-verify", mCrlFilename));

        if (mUseLzo) {
            cfg.append("comp-lzo\n");
        }

        if (mUseTLSAuth) {
            boolean useTlsCrypt = mTLSAuthDirection.equals("tls-crypt");

            if (mAuthenticationType == TYPE_STATICKEYS)
                cfg.append(insertFileData("secret", mTLSAuthFilename));
            else if (useTlsCrypt)
                cfg.append(insertFileData("tls-crypt", mTLSAuthFilename));
            else
                cfg.append(insertFileData("tls-auth", mTLSAuthFilename));

            if (!TextUtils.isEmpty(mTLSAuthDirection) && !useTlsCrypt) {
                cfg.append("key-direction ");
                cfg.append(mTLSAuthDirection);
                cfg.append("\n");
            }

        }

        if (!mUsePull) {
            if (!TextUtils.isEmpty(mIPv4Address))
                cfg.append("ifconfig ").append(cidrToIPAndNetmask(mIPv4Address)).append("\n");
        }

        if (mUsePull && mRoutenopull)
            cfg.append( "route-nopull\n");

        StringBuilder routes = new StringBuilder();

        if (mUseDefaultRoute)
            routes.append("route 0.0.0.0 0.0.0.0 vpn_gateway\n");
        else {
            for (String route : getCustomRoutes(mCustomRoutes)) {
                routes.append("route ").append(route).append(" vpn_gateway\n");
            }

            for (String route : getCustomRoutes(mExcludedRoutes)) {
                routes.append("route ").append(route).append(" net_gateway\n");
            }
        }


        if (mUseDefaultRoutev6)
            cfg.append( "route-ipv6 ::/0\n");
        else
            for (String route : getCustomRoutesv6(mCustomRoutesv6)) {
                routes.append("route-ipv6 ").append(route).append("\n");
            }

        cfg.append(routes.toString());

        if (mOverrideDNS || !mUsePull) {
            if (!TextUtils.isEmpty(mDNS1)) {
                if (mDNS1.contains(":"))
                    cfg.append("dhcp-option DNS6 ").append(mDNS1).append("\n");
                else
                    cfg.append("dhcp-option DNS ").append(mDNS1).append("\n");
            }
            if (!TextUtils.isEmpty(mDNS2)) {
                if (mDNS2.contains(":"))
                    cfg.append("dhcp-option DNS6 ").append(mDNS2).append("\n");
                else
                    cfg.append("dhcp-option DNS ").append(mDNS2).append("\n");
            }
            if (!TextUtils.isEmpty(mSearchDomain))
                cfg.append("dhcp-option DOMAIN ").append(mSearchDomain).append("\n");

        }

        if (mMssFix != 0) {
            if (mMssFix != 1450) {
                cfg.append(String.format(Locale.US, "mssfix %d\n", mMssFix));
            } else
                cfg.append("mssfix\n");
        }

        if (mTunMtu >= 48 && mTunMtu != 1500) {
            cfg.append(String.format(Locale.US, "tun-mtu %d\n", mTunMtu));
        }

        if (mNobind)
            cfg.append("nobind\n");

//        mRemoteCN = "zz1";
        // Authentication
        if (mAuthenticationType != TYPE_STATICKEYS) {
            if (mCheckRemoteCN) {
                if (mRemoteCN == null || mRemoteCN.equals(""))
                    cfg.append("verify-x509-name ").append(openVpnEscape(mConnections[0].mServerName)).append(" name\n");
                else
                    switch (mX509AuthType) {

                        // 2.2 style x509 checks
                        case X509_VERIFY_TLSREMOTE_COMPAT_NOREMAPPING:
                            cfg.append( "compat-names no-remapping\n");
                        case X509_VERIFY_TLSREMOTE:
                            cfg.append("tls-remote ").append(openVpnEscape(mRemoteCN)).append("\n");
                            break;

                        case X509_VERIFY_TLSREMOTE_RDN:
                            cfg.append("verify-x509-name ").append(openVpnEscape(mRemoteCN)).append(" name\n");
                            break;

                        case X509_VERIFY_TLSREMOTE_RDN_PREFIX:
                            cfg.append("verify-x509-name ").append(openVpnEscape(mRemoteCN)).append(" name-prefix\n");
                            break;

                        case X509_VERIFY_TLSREMOTE_DN:
                            cfg.append("verify-x509-name ").append(openVpnEscape(mRemoteCN)).append("\n");
                            break;
                    }
                if (!TextUtils.isEmpty(mx509UsernameField))
                    cfg.append("x509-username-field ").append(openVpnEscape(mx509UsernameField)).append("\n");
            }
            if (mExpectTLSCert)
                cfg.append( "remote-cert-tls server\n");
        }

        if (!TextUtils.isEmpty(mCipher)) {
            cfg.append("cipher ").append(mCipher).append("\n");
        }

        if (!TextUtils.isEmpty(mAuth)) {
            cfg.append("auth ").append(mAuth).append("\n");
        }

        // Obscure Settings dialog
        if (mUseRandomHostname)
            cfg.append("#my favorite options :)\nremote-random-hostname\n");

        if (mUseFloat)
            cfg.append( "float\n");

        if (mPersistTun) {
            cfg.append("persist-tun\n");
            cfg.append( "# persist-tun also enables pre resolving to avoid DNS resolve problem\n");
            cfg.append( "preresolve\n");
        }

        if (mPushPeerInfo)
            cfg.append("push-peer-info\n");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean usesystemproxy = prefs.getBoolean("usesystemproxy", true);
        if (usesystemproxy && !mIsOpenVPN22) {
            cfg.append("# Use system proxy setting\n");
            cfg.append("management-query-proxy\n");
        }


        if (mUseCustomConfig) {
            cfg.append("# Custom configuration options\n");
            cfg.append("# You are on your on own here :)\n");
            cfg.append(mCustomConfigOptions);
            cfg.append("\n");

        }

        if (!canUsePlainRemotes) {
            cfg.append( "# Connection Options are at the end to allow global options (and global custom options) to influence connection blocks\n");
            for (Connection conn : mConnections) {
                if (conn.mEnabled) {
                    cfg.append("<connection>\n");
                    cfg.append(conn.getConnectionBlock());
                    cfg.append("</connection>\n");
                }
            }
        }

        return cfg.toString();
    }

    private String getVersionEnvString(Context c) {
        String version = "unknown";
        try {
            PackageInfo packageinfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = packageinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LOGGER.error(e.getLocalizedMessage());
//            VpnStatus.logException(e);
        }
        return String.format(Locale.US, "%s %s", c.getPackageName(), version);
    }

    //! Put inline data inline and other data as normal escaped filename
    public static String insertFileData(String cfgentry, String filedata) {
        if (filedata == null) {
            return String.format("%s %s\n", cfgentry, "file missing in config profile");
        } else if (isEmbedded(filedata)) {
            String dataWithOutHeader = getEmbeddedContent(filedata);
            return String.format(Locale.ENGLISH, "<%s>\n%s\n</%s>\n", cfgentry, dataWithOutHeader, cfgentry);
        } else {
            return String.format(Locale.ENGLISH, "%s %s\n", cfgentry, openVpnEscape(filedata));
        }
    }

    @NonNull
    private Collection<String> getCustomRoutes(String routes) {
        Vector<String> cidrRoutes = new Vector<>();
        if (routes == null) {
            // No routes set, return empty vector
            return cidrRoutes;
        }
        for (String route : routes.split("[\n \t]")) {
            if (!route.equals("")) {
                String cidrroute = cidrToIPAndNetmask(route);
                if (cidrroute == null)
                    return cidrRoutes;

                cidrRoutes.add(cidrroute);
            }
        }

        return cidrRoutes;
    }

    private Collection<String> getCustomRoutesv6(String routes) {
        Vector<String> cidrRoutes = new Vector<>();
        if (routes == null) {
            // No routes set, return empty vector
            return cidrRoutes;
        }
        for (String route : routes.split("[\n \t]")) {
            if (!route.equals("")) {
                cidrRoutes.add(route);
            }
        }

        return cidrRoutes;
    }

    private String cidrToIPAndNetmask(String route) {
        String[] parts = route.split("/");

        // No /xx, assume /32 as netmask
        if (parts.length == 1)
            parts = (route + "/32").split("/");

        if (parts.length != 2)
            return null;
        int len;
        try {
            len = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ne) {
            return null;
        }
        if (len < 0 || len > 32)
            return null;


        long nm = 0xffffffffL;
        nm = (nm << (32 - len)) & 0xffffffffL;

        String netmask = String.format(Locale.ENGLISH, "%d.%d.%d.%d",
                (nm & 0xff000000) >> 24, (nm & 0xff0000) >> 16, (nm & 0xff00) >> 8, nm & 0xff);
        return parts[0] + "  " + netmask;
    }

    public void writeConfigFile(Context context) throws IOException {
        FileWriter cfg = new FileWriter(VPNLaunchHelper.getConfigFilePath(context));
        cfg.write(getConfigFile(context, false));
        cfg.flush();
        cfg.close();
    }

    private String[] getKeyStoreCertificates(Context context) {
        return getKeyStoreCertificates(context, 5);
    }

    public static String getDisplayName(String embeddedFile) {
        int start = DISPLAYNAME_TAG.length();
        int end = embeddedFile.indexOf(INLINE_TAG);
        return embeddedFile.substring(start, end);
    }

    public static String getEmbeddedContent(String data) {
        if (!data.contains(INLINE_TAG))
            return data;

        int start = data.indexOf(INLINE_TAG) + INLINE_TAG.length();
        return data.substring(start);
    }

    public static boolean isEmbedded(String data) {
        if (data == null)
            return false;
        return data.startsWith(INLINE_TAG) || data.startsWith(DISPLAYNAME_TAG);
    }

    public void checkForRestart(final Context context) {
        /* This method is called when OpenVPNService is restarted */

        if ((mAuthenticationType == VpnProfile.TYPE_KEYSTORE || mAuthenticationType == VpnProfile.TYPE_USERPASS_KEYSTORE)
                && mPrivateKey == null) {
            new Thread(() -> getKeyStoreCertificates(context)).start();
        }
    }

    @Override
    protected VpnProfile clone() throws CloneNotSupportedException {
        VpnProfile copy = (VpnProfile) super.clone();
        copy.mUuid = UUID.randomUUID();
        copy.mConnections = new Connection[mConnections.length];
        int i = 0;
        for (Connection conn : mConnections) {
            copy.mConnections[i++] = conn.clone();
        }
        copy.mAllowedAppsVpn = (HashSet<String>) mAllowedAppsVpn.clone();
        return copy;
    }

    public VpnProfile copy(String name) {
        try {
            VpnProfile copy = clone();
            copy.mName = name;
            return copy;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    class NoCertReturnedException extends Exception {
        NoCertReturnedException(String msg) {
            super(msg);
        }
    }

    private synchronized String[] getKeyStoreCertificates(Context context, int tries) {
        // Force application context- KeyChain methods will block long enough that by the time they
        // are finished and try to unbind, the original activity context might have been destroyed.
        context = context.getApplicationContext();

        try {
            mPrivateKey = KeyChain.getPrivateKey(context, mAlias);

            String keystoreChain = null;


            X509Certificate[] caChain = KeyChain.getCertificateChain(context, mAlias);
            if (caChain == null)
                throw new NoCertReturnedException("No certificate returned from Keystore");

            if (caChain.length <= 1 && TextUtils.isEmpty(mCaFilename)) {
                LOGGER.error(context.getString(R.string.keychain_nocacert));
            } else {
                StringWriter ksStringWriter = new StringWriter();

                PemWriter pw = new PemWriter(ksStringWriter);
                for (int i = 1; i < caChain.length; i++) {
                    X509Certificate cert = caChain[i];
                    pw.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
                }
                pw.close();
                keystoreChain = ksStringWriter.toString();
            }

            String caout = null;
            if (!TextUtils.isEmpty(mCaFilename)) {
                try {
                    Certificate[] cacerts = X509Utils.getCertificatesFromFile(mCaFilename);
                    StringWriter caoutWriter = new StringWriter();
                    PemWriter pw = new PemWriter(caoutWriter);

                    for (Certificate cert : cacerts)
                        pw.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
                    pw.close();
                    caout = caoutWriter.toString();

                } catch (Exception e) {
                    LOGGER.error("Could not read CA certificate" + e.getLocalizedMessage());
                }
            }

            StringWriter certout = new StringWriter();

            if (caChain.length >= 1) {
                X509Certificate usercert = caChain[0];

                PemWriter upw = new PemWriter(certout);
                upw.writeObject(new PemObject("CERTIFICATE", usercert.getEncoded()));
                upw.close();

            }
            String user = certout.toString();

            String ca, extra;
            if (caout == null) {
                ca = keystoreChain;
                extra = null;
            } else {
                ca = caout;
                extra = keystoreChain;
            }

            return new String[]{ca, extra, user};
        } catch (InterruptedException | IOException | KeyChainException | NoCertReturnedException | IllegalArgumentException
                | CertificateException e) {
            e.printStackTrace();
            LOGGER.error(context.getString(R.string.keyChainAccessError), e.getLocalizedMessage());

            LOGGER.error(context.getString(R.string.keychain_access));
            return null;
        } catch (AssertionError e) {
            if (tries == 0)
                return null;
            LOGGER.error(String.format("Failure getting Keystore Keys (%s), retrying", e.getLocalizedMessage()));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException interruptedException) {
                LOGGER.error(interruptedException.getLocalizedMessage());
            }
            return getKeyStoreCertificates(context, tries - 1);
        }
    }

    private boolean isUserPWAuth() {
        switch (mAuthenticationType) {
            case TYPE_USERPASS:
            case TYPE_USERPASS_CERTIFICATES:
            case TYPE_USERPASS_KEYSTORE:
            case TYPE_USERPASS_PKCS12:
                return true;
            default:
                return false;
        }
    }

    // Used by the Array Adapter
    @Override
    public String toString() {
        return mName;
    }

    public String getUUIDString() {
        return mUuid.toString();
    }

    private PrivateKey getKeystoreKey() {
        return mPrivateKey;
    }

    public String getSignedData(String b64data) {
        PrivateKey privateKey = getKeystoreKey();

        byte[] data = Base64.decode(b64data, Base64.DEFAULT);

        try {
            /* ECB is perfectly fine in this special case, since we are using it for
               the public/private part in the TLS exchange
             */
            @SuppressLint("GetInstance")
            Cipher rsaSigner = Cipher.getInstance("RSA/ECB/OAEPPadding");

            rsaSigner.init(Cipher.ENCRYPT_MODE, privateKey);

            byte[] signed_bytes = rsaSigner.doFinal(data);
            return Base64.encodeToString(signed_bytes, Base64.NO_WRAP);

        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | NoSuchPaddingException e) {
            LOGGER.error(IVPNApplication.application.getString(R.string.error_rsa_sign), e.getClass().toString(),
                    e.getLocalizedMessage());
            return null;
        }
    }
}