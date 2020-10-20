package net.ivpn.client.rest;

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

public interface Responses {
    int SUCCESS = 200;
    int BAD_REQUEST = 400;
    int INVALID_CREDENTIALS = 401;
    int NOT_ACTIVE = 402;
    int WIREGUARD_KEY_INVALID = 422;
    int WIREGUARD_PUBLIC_KEY_EXIST = 423;
    int WIREGUARD_KEY_NOT_FOUND = 424;
    int WIREGUARD_KEY_LIMIT_REACHED = 425;
    int WIREGUARD_KEY_NOT_PROVIDED = 426;

    int ACCOUNT_NOT_ACTIVE = 11005;

    int GEO_LOOKUP_DB_ERROR = 501;
    int GEO_LOOKUP_IP_INVALID = 502;
    int GEO_LOOKUP_IP_NOT_FOUND = 503;

    int SESSION_SERVICE_ERROR = 600;
    int SESSION_NOT_FOUND = 601;
    int SESSION_TOO_MANY = 602;

    int SERVICE_IS_NOT_ACTIVE = 702;

    int SUBSCRIPTION_GOOGLE_ERROR = 403;
    int SUBSCRIPTION_ALREADY_REGISTERED = 409;
    int SUBSCRIPTION_ERROR_WHILE_CREATING_ACCOUNT = 500;

    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int CONFLICT = 409;
//    int WG_PK_NOT_FOUND = 424;
//    int WG_TOO_MANY_KEYS = 425;
    int TOO_MANY_ATTEMPTS = 429;
    int SERVER_ERROR = 500;

    String PRIVATE_EMAILS = "private-emails";
    String MULTI_HOP = "multihop";
    String WIREGUARD = "wireguard";
    String SUCCESS_STR = "success";
    String ERROR = "error";
}

