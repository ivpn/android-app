package net.ivpn.core.common;

import org.junit.Test;
import static org.junit.Assert.*;
import de.blinkt.openvpn.VpnProfile;

public class ProfileStorageTest {

    final String profileName = "TestVpnProfile";

    @Test
    public void writeAndReadValidProfile() {
        VpnProfile profile = new VpnProfile(profileName);

        ProfileStorage.writeProfile(profile);
        VpnProfile profileFromStorage = ProfileStorage.readProfile();
        assertEquals(profile, profileFromStorage);
        assertEquals(profileName, profileFromStorage.mName);
    }

    @Test
    public void writeAndReadNullProfile() {
        ProfileStorage.writeProfile(null);
        VpnProfile profileFromStorage = ProfileStorage.readProfile();
        assertNull(profileFromStorage);
    }
}