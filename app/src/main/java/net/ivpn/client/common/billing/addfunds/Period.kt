package net.ivpn.client.common.billing.addfunds

enum class Period(val skuPath: String) {
    ONE_WEEK("1week"),
    ONE_MONTH("1month"),
    ONE_YEAR("1year"),
    TWO_YEARS("2year"),
    THREE_YEARS("3year")
}