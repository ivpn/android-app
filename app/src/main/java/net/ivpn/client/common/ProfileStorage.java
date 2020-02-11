package net.ivpn.client.common;

import android.app.Activity;
import android.util.Log;

import net.ivpn.client.IVPNApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.blinkt.openvpn.VpnProfile;

/**
 *  Class ProfileStorage is used to read/write {@link VpnProfile} entity in/from the file.
 */
public class ProfileStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileStorage.class);
    private static final String FILE = "profile.vp";

    /**
     *  Save profile to the file
     *  @param profile the entity that will be saved in the file
     *  @return        file name
     */
    public static boolean writeProfile(VpnProfile profile) {
        LOGGER.info("Writing OpenVpn profile...");
        ObjectOutputStream vpnFile;
        try {
            vpnFile = new ObjectOutputStream(IVPNApplication.getApplication().openFileOutput(FILE, Activity.MODE_PRIVATE));
            vpnFile.writeObject(profile);
            vpnFile.flush();
            vpnFile.close();
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while saving VPN profile", e);
            return false;
        }
    }

    /**
     *  Read profile from the file
     *  @return  profile entity. Can be null
     */
    public static VpnProfile readProfile() {
        Log.d("Profile Storage", "readProfile: ");
        LOGGER.info("Reading OpenVpn profile...");
        VpnProfile profile = null;
        try {
            ObjectInputStream vpnStream = new ObjectInputStream(IVPNApplication.getApplication().openFileInput(FILE));
            profile = ((VpnProfile) vpnStream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Error while reading VPN profile", e);
            e.printStackTrace();
            return profile;
        }
        return profile;
    }
}
