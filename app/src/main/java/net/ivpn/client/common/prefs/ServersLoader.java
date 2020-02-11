package net.ivpn.client.common.prefs;

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