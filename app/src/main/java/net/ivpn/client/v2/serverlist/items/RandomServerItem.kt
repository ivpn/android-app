package net.ivpn.client.v2.serverlist.items

class RandomServerItem: ConnectionOption {

    override fun equals(other: Any?): Boolean {
        other?.let {
            return it is RandomServerItem
        } ?: return false
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}