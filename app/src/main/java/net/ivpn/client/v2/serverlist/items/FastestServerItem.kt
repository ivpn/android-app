package net.ivpn.client.v2.serverlist.items


class FastestServerItem: ConnectionOption {
    override fun equals(other: Any?): Boolean {
        other?.let {
            return it is FastestServerItem
        } ?: return false
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}