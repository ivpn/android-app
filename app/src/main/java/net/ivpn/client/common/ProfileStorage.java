package net.ivpn.client.common;

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
