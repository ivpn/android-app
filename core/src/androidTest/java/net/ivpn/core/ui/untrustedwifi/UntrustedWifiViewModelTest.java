package net.ivpn.core.ui.untrustedwifi;

public class UntrustedWifiViewModelTest {

//    private static final String wifiSsid1 = "Wifi C";
//    private static final String wifiSsid2 = "Wifi A";
//    private static final String wifiSsid3 = "Wifi B";
//
//    @Rule
//    public ActivityTestRule<WifiActivity> activityTestRule
//            = new ActivityTestRule<>(WifiActivity.class);
//
//    @After
//    public void release() {
//        Preference.INSTANCE.removeAll();
//    }
//
//    @Test
//    public void shouldSettingApply() {
//        activityTestRule.launchActivity(new Intent());
//        onView(withId(R.id.wifi_main_switcher)).check(matches(isDisplayed())).perform(click());
//        assertTrue(Preference.INSTANCE.getSettingNetworkRules());
//    }
//
//    @Test
//    public void shouldCommonSwitcherEnabled() {
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        activityTestRule.launchActivity(new Intent());
//        onView(withId(R.id.wifi_main_switcher)).check(matches(isChecked()));
//    }
//
//    @Test
//    public void shouldSettingsApplyDefaultsCorrect() {
//        WifiViewModel viewModel = new WifiViewModel(getWifiManager());
//        assertEquals(viewModel.isUntrustedWifiEnabled.get(), Preference.INSTANCE.getSettingNetworkRules());
//    }
//
//    @Test
//    public void shouldSettingsApplyCorrect() {
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        WifiViewModel viewModel = new WifiViewModel(getWifiManager());
//        assertEquals(viewModel.isUntrustedWifiEnabled.get(), Preference.INSTANCE.getSettingNetworkRules());
//    }
//
//    @Test
//    public void shouldBeSortedByTitles() {
//        WifiViewModel viewModel = new WifiViewModel(getWifiManager());
//        List<WifiItem> itemList = new ArrayList<>(viewModel.wifiItemList);
//        Collections.sort(itemList, new Comparator<WifiItem>() {
//                    @Override
//                    public int compare(WifiItem item1, WifiItem item2) {
//                        return item1.getTitle().compareToIgnoreCase(item2.getTitle());
//                    }
//                });
//        assertArrayEquals(itemList.toArray(), viewModel.wifiItemList.toArray());
//    }
//
//    @Test
//    public void shouldBeMarkedAsTrusted() {
//        Preference.INSTANCE.markWifiAsTrusted(wifiSsid2);
//        WifiViewModel viewModel = new WifiViewModel(getWifiManager());
//        for (WifiItem item : viewModel.wifiItemList) {
//            if (item.getSsid().equals(wifiSsid2)) {
//                assertTrue(item.isTrusted());
//            } else {
//                assertFalse(item.isTrusted());
//            }
//        }
//    }
//
//    private WifiManager getWifiManager() {
//        List<WifiConfiguration> wifiConfigurationList = new ArrayList<>();
//        WifiConfiguration wifi1 = mock(WifiConfiguration.class);
//        wifi1.SSID = wifiSsid1;
//
//        WifiConfiguration wifi2 = mock(WifiConfiguration.class);
//        wifi2.SSID = wifiSsid2;
//
//        WifiConfiguration wifi3 = mock(WifiConfiguration.class);
//        wifi3.SSID = wifiSsid3;
//
//        wifiConfigurationList.add(wifi1);
//        wifiConfigurationList.add(wifi2);
//        wifiConfigurationList.add(wifi3);
//
//        WifiManager wifiManager = mock(WifiManager.class);
//        when(wifiManager.getConfiguredNetworks()).thenReturn(wifiConfigurationList);
//
//        return wifiManager;
//    }
}