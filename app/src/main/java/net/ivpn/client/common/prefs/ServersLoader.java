package net.ivpn.client.common.prefs;

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

import net.ivpn.client.IVPNApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ServersLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServersLoader.class);
    private static final String SERVERS_PATH = "servers.json";

    public static String load() {
        LOGGER.info("load servers");
        StringBuilder stringBuilder = new StringBuilder();

        try {
            InputStream inputStream = IVPNApplication.getApplication().getAssets().open(SERVERS_PATH);
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                stringBuilder.append(str);
            }

            in.close();
        } catch (IOException e) {
            LOGGER.error("Error while loading servers", e);
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}