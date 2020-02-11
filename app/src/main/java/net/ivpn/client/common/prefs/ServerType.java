package net.ivpn.client.common.prefs;

public enum  ServerType {
    ENTRY,
    EXIT;

    public static ServerType getAnotherType(ServerType serverType) {
        if (serverType.equals(ENTRY)) return EXIT;
        return ENTRY;
    }

    public static boolean contains(String type) {
        for (ServerType c : ServerType.values()) {
            if (c.name().equals(type)) {
                return true;
            }
        }

        return false;
    }
}
