package net.ivpn.core.vpn;

import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VPNControllerTest {

    private static final String TAG = VPNControllerTest.class.getSimpleName();

    private final String connected = "CONNECTED";
    private final String disconnected = "DISCONNECTED";
    private final String msg = "";


    @Before
    public void init() {
//        Intents.init();
//        // Block any intent
//        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor((IntentFilter) null, null, true);
//        InstrumentationRegistry.getInstrumentation().addMonitor(monitor);

    }

//    @After
//    public void release() {
//        Intents.release();
//        Preference.INSTANCE.removeAll();
//        GlobalBehaviorController.INSTANCE.release();
//        VpnStatus.updateStateString(disconnected, msg);
//    }
//
//    @Test
//    public void shouldBeNoneAppState() {
//        GlobalBehaviorController.INSTANCE.init();
//        assertEquals(VPNState.NONE, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldBeVpnAppState() {
//        VpnStatus.updateStateString(connected, msg);
//        GlobalBehaviorController.INSTANCE.init();
//        assertEquals(VPNState.VPN, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldBeGuardAppState() {
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        GlobalBehaviorController.INSTANCE.init();
//        assertEquals(VPNState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldBeBothAppState() {
//        VpnStatus.updateStateString(connected, msg);
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        GlobalBehaviorController.INSTANCE.init();
//        assertEquals(VPNState.BOTH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldBeNoneGuardState() {
//        GlobalBehaviorController.INSTANCE.init();
//        assertEquals(GuardState.NONE, GlobalBehaviorController.INSTANCE.getGuardState());
//    }
//
//    @Test
//    public void shouldBeKillSwitchGuardState() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        GlobalBehaviorController.INSTANCE.init();
//        assertEquals(GuardState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getGuardState());
//    }
//
//    @Test
//    public void shouldBeWifiWatcherGuardState() {
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        GlobalBehaviorController.INSTANCE.init();
//        assertEquals(GuardState.WIFI_WATCHER, GlobalBehaviorController.INSTANCE.getGuardState());
//    }
//
//    @Test
//    public void shouldBeBothGuardState() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        GlobalBehaviorController.INSTANCE.init();
//        assertEquals(GuardState.BOTH, GlobalBehaviorController.INSTANCE.getGuardState());
//    }
//
//    @Test
//    public void shouldEnableKillSwitch() {
//        GlobalBehaviorController.INSTANCE.init();
//        assertFalse(GlobalBehaviorController.INSTANCE.isKillSwitchShouldBeStarted());
//    }
//
//    @Test
//    public void shouldEnableKillSwitch2() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        GlobalBehaviorController.INSTANCE.init();
//        assertTrue(GlobalBehaviorController.INSTANCE.isKillSwitchShouldBeStarted());
//    }
//
//    @Test
//    public void shouldApplyKillSwitchSetting() {
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.enableKillSwitch();
//        assertEquals(GuardState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldApplyKillSwitchSetting2() {
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        VpnStatus.updateStateString(connected, msg);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.enableKillSwitch();
//        assertEquals(GuardState.BOTH, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.BOTH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldApplyKillSwitchSetting3() {
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.enableKillSwitch();
//        assertEquals(GuardState.BOTH, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldDisableKillSwitch() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.disableKillSwitch();
//        assertEquals(GuardState.NONE, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.NONE, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldDisableKillSwitch2() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.disableKillSwitch();
//        assertEquals(GuardState.WIFI_WATCHER, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldDisableKillSwitch3() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        VpnStatus.updateStateString(connected, msg);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.disableKillSwitch();
//        assertEquals(GuardState.WIFI_WATCHER, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.BOTH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldApplyWifiWatcherSetting() {
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.enableWifiWatcher();
//        assertEquals(GuardState.WIFI_WATCHER, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldApplyWifiWatcherSetting2() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.enableWifiWatcher();
//        assertEquals(GuardState.BOTH, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldApplyWifiWatcherSetting3() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        VpnStatus.updateStateString(connected, msg);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.enableWifiWatcher();
//        assertEquals(GuardState.BOTH, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.BOTH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldDisableWifiWatcher() {
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.disableWifiWatcher();
//        assertEquals(GuardState.NONE, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.NONE, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldDisableWifiWatcher2() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.disableWifiWatcher();
//        assertEquals(GuardState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldDisableWifiWatcher3() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        VpnStatus.updateStateString(connected, msg);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.disableWifiWatcher();
//        assertEquals(GuardState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.BOTH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldStartVpn() {
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.startVPN();
////        intended(hasComponent(new ComponentName(IVPNApplication.getApplication(), IVPNService.class)));
////        intended(toPackage("net.ivpn.client"));
//        assertEquals(GuardState.NONE, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.VPN, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldStartVpn2() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.startVPN();
//        assertEquals(GuardState.KILL_SWITCH, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.BOTH, GlobalBehaviorController.INSTANCE.getState());
//    }
//
//    @Test
//    public void shouldStartVpn3() {
//        Preference.INSTANCE.putSettingKillSwitch(true);
//        Preference.INSTANCE.putSettingsNetworkRules(true);
//        GlobalBehaviorController.INSTANCE.init();
//        GlobalBehaviorController.INSTANCE.startVPN();
//        assertEquals(GuardState.BOTH, GlobalBehaviorController.INSTANCE.getGuardState());
//        assertEquals(VPNState.BOTH, GlobalBehaviorController.INSTANCE.getState());
//    }
}