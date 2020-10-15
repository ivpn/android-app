package net.ivpn.client.rest.data.wireguard;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddWireGuardPublicKeyRequestBody {

    @SerializedName("session_token")
    @Expose
    private String sessionToken;
    @SerializedName("public_key")
    @Expose
    private String publicKey;
    @SerializedName("connected_public_key")
    @Expose
    private String connectedPublicKey;

    public AddWireGuardPublicKeyRequestBody(String sessionToken, String publicKey, String connectedPublicKey) {
        this.sessionToken = sessionToken;
        this.publicKey = publicKey;
        this.connectedPublicKey = connectedPublicKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}