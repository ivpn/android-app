package net.ivpn.client.v2.serverlist.items

class SearchServerItem : ConnectionOption {

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        return other is SearchServerItem

    }
}