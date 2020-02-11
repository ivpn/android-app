package net.ivpn.client.ui.untrustedwifi;

import net.ivpn.client.common.prefs.Preference;
import net.ivpn.client.vpn.model.WifiItem;

import org.junit.After;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class ItemWifiViewModelTest {

    private final String wifiSsid = "Wifi1";

    @After
    public void release() {
        Preference.INSTANCE.removeAll();
    }

//    @Test
//    public void shouldSaveAsTrustedInPrefs() {
//        WifiItem item = new WifiItem(wifiSsid, false);
//        ItemWifiViewModel viewModel = new ItemWifiViewModel(item);
//
//        viewModel.onItemClick();
//        assertTrue(viewModel.isChecked());
//
//        Set<String> trustedWifiList = Preference.INSTANCE.getTrustedWifiList();
//        assertThat(trustedWifiList, hasItem(item.getSsid()));
//    }
//
//    @Test
//    public void shouldRemoveFromTrustedInPrefs() {
//        Preference.INSTANCE.markWifiAsTrusted(wifiSsid);
//        WifiItem item = new WifiItem(wifiSsid, true);
//        ItemWifiViewModel viewModel = new ItemWifiViewModel(item);
//
//        viewModel.onItemClick();
//        assertFalse(viewModel.isChecked());
//
//        Set<String> trustedWifiList = Preference.INSTANCE.getTrustedWifiList();
//        assertFalse(trustedWifiList.contains(wifiSsid));
//    }
}