package net.ivpn.core.common.prefs;

import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsTest {

    private static final String STANDARD_DNS = "1.1.1.1";
    private static final String STANDARD_DNS_MULTI_HOP = "2.2.2.2";
    private static final String HARDCORE_DNS = "3.3.3.3";
    private static final String HARDCORE_DNS_MULTI_HOP = "4.4.4.4";
    private static final String CUSTOM_DNS = "5.5.5.5";

    @Before
    public void setUp() {
        initSettings();
    }

    @Test
    public void getDNS1() {
        Settings.INSTANCE.enableAntiSurveillance(true);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(true);
        Settings.INSTANCE.enableCustomDNS(true);
        Settings.INSTANCE.enableMultiHop(true);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, HARDCORE_DNS_MULTI_HOP);
    }

    @Test
    public void getDNS2() {
        Settings.INSTANCE.enableAntiSurveillance(false);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(true);
        Settings.INSTANCE.enableCustomDNS(true);
        Settings.INSTANCE.enableMultiHop(true);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, CUSTOM_DNS);
    }

    @Test
    public void getDNS3() {
        Settings.INSTANCE.enableAntiSurveillance(true);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(false);
        Settings.INSTANCE.enableCustomDNS(true);
        Settings.INSTANCE.enableMultiHop(true);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, STANDARD_DNS_MULTI_HOP);
    }

    @Test
    public void getDNS4() {
        Settings.INSTANCE.enableAntiSurveillance(false);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(false);
        Settings.INSTANCE.enableCustomDNS(true);
        Settings.INSTANCE.enableMultiHop(true);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, CUSTOM_DNS);
    }

    @Test
    public void getDNS5() {
        Settings.INSTANCE.enableAntiSurveillance(false);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(false);
        Settings.INSTANCE.enableCustomDNS(false);
        Settings.INSTANCE.enableMultiHop(true);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, null);
    }

    @Test
    public void getDNS6() {
        Settings.INSTANCE.enableAntiSurveillance(false);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(false);
        Settings.INSTANCE.enableCustomDNS(false);
        Settings.INSTANCE.enableMultiHop(false);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, null);
    }

    @Test
    public void getDNS7() {
        Settings.INSTANCE.enableAntiSurveillance(false);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(false);
        Settings.INSTANCE.enableCustomDNS(true);
        Settings.INSTANCE.enableMultiHop(false);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, CUSTOM_DNS);
    }

    @Test
    public void getDNS8() {
        Settings.INSTANCE.enableAntiSurveillance(true);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(true);
        Settings.INSTANCE.enableCustomDNS(false);
        Settings.INSTANCE.enableMultiHop(true);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, HARDCORE_DNS_MULTI_HOP);
    }

    @Test
    public void getDNS9() {
        Settings.INSTANCE.enableAntiSurveillance(true);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(true);
        Settings.INSTANCE.enableCustomDNS(false);
        Settings.INSTANCE.enableMultiHop(false);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, HARDCORE_DNS);
    }

    @Test
    public void getDNS10() {
        Settings.INSTANCE.enableAntiSurveillance(true);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(false);
        Settings.INSTANCE.enableCustomDNS(false);
        Settings.INSTANCE.enableMultiHop(false);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, STANDARD_DNS);
    }

    @Test
    public void getDNS11() {
        Settings.INSTANCE.enableAntiSurveillance(true);
        Settings.INSTANCE.enableAntiSurveillanceHardcore(false);
        Settings.INSTANCE.enableCustomDNS(false);
        Settings.INSTANCE.enableMultiHop(true);

        String dns = Settings.INSTANCE.getDNS();
        assertSame(dns, STANDARD_DNS_MULTI_HOP);
    }

    private void initSettings() {
        Settings.INSTANCE.setAntiTrackerDefaultDNS(STANDARD_DNS);
        Settings.INSTANCE.setAntiTrackerDefaultDNSMulti(STANDARD_DNS_MULTI_HOP);
        Settings.INSTANCE.setAntiTrackerHardcoreDNS(HARDCORE_DNS);
        Settings.INSTANCE.setAntiTrackerHardcoreDNSMulti(HARDCORE_DNS_MULTI_HOP);
        Settings.INSTANCE.setCustomDNSValue(CUSTOM_DNS);
    }
}