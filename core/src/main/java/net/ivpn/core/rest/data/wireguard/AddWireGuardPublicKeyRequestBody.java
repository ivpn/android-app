package net.ivpn.core.rest.data.wireguard;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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
    @SerializedName("kem_public_key1")
    @Expose
    private String kemPublicKey;
    @SerializedName("kem_library_version")
    @Expose
    private String kemLibraryVersion;

    public AddWireGuardPublicKeyRequestBody(String sessionToken, String publicKey, String connectedPublicKey, String kemPublicKey) {
        this.sessionToken = sessionToken;
        this.publicKey = publicKey;
        this.connectedPublicKey = connectedPublicKey;
        this.kemPublicKey = kemPublicKey;
        this.kemLibraryVersion = "0.10.0";
    }

    public AddWireGuardPublicKeyRequestBody(String sessionToken, String publicKey, String kemPublicKey) {
        this.sessionToken = sessionToken;
        this.publicKey = publicKey;
        this.kemPublicKey = kemPublicKey;
        this.kemLibraryVersion = "0.10.0";
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