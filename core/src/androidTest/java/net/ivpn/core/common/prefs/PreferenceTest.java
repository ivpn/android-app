package net.ivpn.core.common.prefs;

import org.junit.After;

import static org.hamcrest.CoreMatchers.hasItem;

public class PreferenceTest {

    @After
    public void clear() {
        Preference.INSTANCE.removeAll();
    }

//    @Test
//    public void writeReadLoggingSettings() {
//        Preference.INSTANCE.putSettingLogging(false);
//        assertFalse(Preference.INSTANCE.getSettingLogging());
//
//        Preference.INSTANCE.putSettingLogging(true);
//        assertTrue(Preference.INSTANCE.getSettingLogging());
//    }
//
//    @Test
//    public void writeReadMultiHopSettings() {
//        Preference.INSTANCE.putSettingMultiHop(false);
//        assertFalse(Preference.INSTANCE.getSettingMultiHop());
//
//        Preference.INSTANCE.putSettingMultiHop(true);
//        assertTrue(Preference.INSTANCE.getSettingMultiHop());
//    }
//
//    @Test
//    public void writeReadKillSwitchSettings() {
//        Preference.INSTANCE.putSettingKillSwitch(false);
//        assertFalse(Preference.INSTANCE.getSettingKillSwitch());
//
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        assertTrue(Preference.INSTANCE.getSettingKillSwitch());
//    }
//
//    @Test
//    public void writeReadStartOnBootSettings() {
//        Preference.INSTANCE.putSettingStartOnBoot(false);
//        assertFalse(Preference.INSTANCE.getSettingStartOnBoot());
//
//        Preference.INSTANCE.putSettingStartOnBoot(true);
//        assertTrue(Preference.INSTANCE.getSettingStartOnBoot());
//    }
//
//    @Test
//    public void writeReadAdvancedKillSwitchSettings() {
//        Preference.INSTANCE.putSettingAdvancedKillSwitch(false);
//        assertFalse(Preference.INSTANCE.getIsAdvancedKillSwitchDialogEnabled());
//
//        Preference.INSTANCE.putSettingAdvancedKillSwitch(true);
//        assertTrue(Preference.INSTANCE.getIsAdvancedKillSwitchDialogEnabled());
//    }
//
//    @Test
//    public void writeReadNewForPrivateEmailsSettings() {
//        Preference.INSTANCE.putIsNewForPrivateEmails(false);
//        assertFalse(Preference.INSTANCE.getIsNewForPrivateEmails());
//
//        Preference.INSTANCE.putIsNewForPrivateEmails(true);
//        assertTrue(Preference.INSTANCE.getIsNewForPrivateEmails());
//    }
//
//    @Test
//    public void writeReadPortSettings() {
//        Preference.INSTANCE.setPort(77);
//        assertEquals(Preference.INSTANCE.getPort(), 77);
//    }
//
//    @Test
//    public void writeReadCurrentServerValid() {
//        Server entryServer = new Server();
//        entryServer.setGateway("fr.gw.ivpn.net");
//
//        Server exitServer = new Server();
//        exitServer.setGateway("de.gw.ivpn.net");
//
//        Preference.INSTANCE.setCurrentServer(ServerType.ENTRY, entryServer);
//        Preference.INSTANCE.setCurrentServer(ServerType.EXIT, exitServer);
//
//        assertEquals(entryServer, Preference.INSTANCE.getCurrentServer(ServerType.ENTRY));
//        assertEquals(exitServer, Preference.INSTANCE.getCurrentServer(ServerType.EXIT));
//
//        //invalid params should be ignored
//        Preference.INSTANCE.setCurrentServer(null, entryServer);
//        Preference.INSTANCE.setCurrentServer(ServerType.ENTRY, null);
//        Preference.INSTANCE.setCurrentServer(ServerType.EXIT, null);
//        Preference.INSTANCE.getCurrentServer(null);
//
//        assertEquals(entryServer, Preference.INSTANCE.getCurrentServer(ServerType.ENTRY));
//        assertEquals(exitServer, Preference.INSTANCE.getCurrentServer(ServerType.EXIT));
//    }
//
//    @Test
//    public void writeReadServerList() {
//        List<Server> list = new ArrayList<>();
//        Server server1 = new Server();
//        server1.setGateway("fr.gw.ivpn.net");
//
//        Server server2 = new Server();
//        server2.setGateway("de.gw.ivpn.net");
//
//        Server server3 = new Server();
//        server3.setGateway("uk.gw.ivpn.net");
//
//        list.add(server1);
//        list.add(server2);
//        list.add(server3);
//
//        Preference.INSTANCE.putServerList(list);
//        List<Server> listFromPref = Preference.INSTANCE.getServersList();
//        assertNotNull(listFromPref);
//        assertEquals(listFromPref.size(), list.size());
//        for (Server server : listFromPref) {
//            assertThat(list, hasItem(server));
//        }
//    }
//
//    @Test
//    public void writeReadFavouriteServerList() {
//        Server server1 = new Server();
//        server1.setGateway("fr.gw.ivpn.net");
//
//        Server server2 = new Server();
//        server2.setGateway("de.gw.ivpn.net");
//
//        Server server3 = new Server();
//        server3.setGateway("uk.gw.ivpn.net");
//
//        Preference.INSTANCE.addFavouriteServer(server1);
//        List<Server> favListFromPref = Preference.INSTANCE.getFavouritesServersList();
//        assertNotNull(favListFromPref);
//        assertEquals(favListFromPref.size(), 1);
//
//        //invalid params and same server should be ignored
//        Preference.INSTANCE.addFavouriteServer(server1);
//        Preference.INSTANCE.addFavouriteServer(null);
//
//        favListFromPref = Preference.INSTANCE.getFavouritesServersList();
//        assertNotNull(favListFromPref);
//        assertEquals(favListFromPref.size(), 1);
//
//        Preference.INSTANCE.addFavouriteServer(server2);
//        Preference.INSTANCE.addFavouriteServer(server3);
//
//        favListFromPref = Preference.INSTANCE.getFavouritesServersList();
//        assertNotNull(favListFromPref);
//        assertEquals(favListFromPref.size(), 3);
//        assertThat(favListFromPref, hasItem(server1));
//        assertThat(favListFromPref, hasItem(server2));
//        assertThat(favListFromPref, hasItem(server3));
//
//        Preference.INSTANCE.removeFavouriteServer(server1);
//        Preference.INSTANCE.removeFavouriteServer(server2);
//        favListFromPref = Preference.INSTANCE.getFavouritesServersList();
//        assertNotNull(favListFromPref);
//        assertEquals(favListFromPref.size(), 1);
//        assertThat(favListFromPref, hasItem(server3));
//
//        //invalid params and same server should be ignored
//        Preference.INSTANCE.removeFavouriteServer(null);
//        Preference.INSTANCE.removeFavouriteServer(server1);
//        assertNotNull(favListFromPref);
//        assertEquals(favListFromPref.size(), 1);
//        assertThat(favListFromPref, hasItem(server3));
//    }
//
//    @Test
//    public void allowDisallowApps() {
//        Set<String> disallowedApps = new HashSet<>();
//        disallowedApps.add("com.ninegag.android.app");
//        disallowedApps.add("com.google.android.gm");
//        disallowedApps.add("com.facebook.katana");
//
//        Preference.INSTANCE.disallowAllPackages(disallowedApps);
//        Set<String> disallowedAppsFromPref = Preference.INSTANCE.getDisallowedPackages();
//        assertNotNull(disallowedAppsFromPref);
//        assertEquals(disallowedAppsFromPref.size(), disallowedApps.size());
//        for (String packageName : disallowedApps) {
//            assertThat(disallowedAppsFromPref, hasItem(packageName));
//        }
//
//        //invalid params should be ignored
//        Preference.INSTANCE.allowPackage("com.google.android.youtube");
//        Preference.INSTANCE.allowPackage("com.snapchat.android");
//        Preference.INSTANCE.allowPackage(null);
//
//        disallowedAppsFromPref = Preference.INSTANCE.getDisallowedPackages();
//        assertNotNull(disallowedAppsFromPref);
//        assertEquals(disallowedAppsFromPref.size(), disallowedApps.size());
//        for (String packageName : disallowedApps) {
//            assertThat(disallowedAppsFromPref, hasItem(packageName));
//        }
//
//        Preference.INSTANCE.allowAllPackages();
//        disallowedAppsFromPref = Preference.INSTANCE.getDisallowedPackages();
//        assertNotNull(disallowedAppsFromPref);
//        assertEquals(disallowedAppsFromPref.size(), 0);
//
//        Preference.INSTANCE.disallowPackage(null);
//        Preference.INSTANCE.disallowPackage("com.google.android.youtube");
//        Preference.INSTANCE.disallowPackage("com.snapchat.android");
//        disallowedAppsFromPref = Preference.INSTANCE.getDisallowedPackages();
//        assertNotNull(disallowedAppsFromPref);
//        assertEquals(disallowedAppsFromPref.size(), 2);
//    }
//
//    @Test
//    public void shouldReturnCorrectDefaultUntrustedWifiSetting() {
//        assertFalse(Preference.INSTANCE.getSettingNetworkRules());
//    }
//
//    @Test
//    public void shouldReturnUntrustedWifiSetting() {
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        assertTrue(Preference.INSTANCE.getSettingNetworkRules());
//    }
//
//    @Test
//    public void shouldSaveWifiSSID() {
//        String wifiSSID = PreferenceContentGenerator.getStubWifiSSID();
//        Preference.INSTANCE.markWifiAsTrusted(wifiSSID);
//        Set<String> trustedWifiSSID = Preference.INSTANCE.getTrustedWifiList();
//        assertThat(trustedWifiSSID, hasItem(wifiSSID));
//    }
//
//    @Test
//    public void shouldIgnoreInvalidWifiSSID() {
//        Preference.INSTANCE.markWifiAsTrusted(null);
//        Set<String> trustedWifiSSID = Preference.INSTANCE.getTrustedWifiList();
//        assertNotNull(trustedWifiSSID);
//        assertTrue(trustedWifiSSID.isEmpty());
//    }
//
//    @Test
//    public void shouldRemoveWifiSSID() {
//        String wifiSSID = PreferenceContentGenerator.getStubWifiSSID();
//        Preference.INSTANCE.markWifiAsTrusted(wifiSSID);
//        Preference.INSTANCE.removeMarkWifiAsTrusted(wifiSSID);
//        Set<String> trustedWifiSSID = Preference.INSTANCE.getTrustedWifiList();
//        assertTrue(trustedWifiSSID.isEmpty());
//    }
}