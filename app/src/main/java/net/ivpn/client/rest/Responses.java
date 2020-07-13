package net.ivpn.client.rest;

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

    int ACCOUNT_NOT_ACTIVE = 11005;

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

