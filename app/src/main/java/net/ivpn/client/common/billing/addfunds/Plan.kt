package net.ivpn.client.common.billing.addfunds

enum class Plan(val skuPath: String, val productName: String) {
    PRO("net.ivpn.subscriptions.pro.", "IVPN Pro"),
    STANDARD("net.ivpn.subscriptions.standard.", "IVPN Standard");
}