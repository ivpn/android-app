package net.ivpn.client.common.billing

object ConsumableProducts {

    const val ONE_WEEK_STANDARD = "net.ivpn.subscriptions.standard.1week";
    const val ONE_WEEK_PRO = "net.ivpn.subscriptions.pro.1week";

    const val ONE_MONTH_STANDARD = "net.ivpn.subscriptions.standard.1month";
    const val ONE_MONTH_PRO = "net.ivpn.subscriptions.pro.1month";

    const val ONE_YEAR_STANDARD = "net.ivpn.subscriptions.standard.1year"
    const val ONE_YEAR_PRO = "net.ivpn.subscriptions.pro.1year"

    const val TWO_YEARS_STANDARD = "net.ivpn.subscriptions.standard.2year"
    const val TWO_YEARS_PRO = "net.ivpn.subscriptions.pro.2year"

    const val THREE_YEARS_STANDARD = "net.ivpn.subscriptions.standard.3year"
    const val THREE_YEARS_PRO = "net.ivpn.subscriptions.pro.3year"

    fun getConsumableSKUs(): List<String> {
        val skuList = mutableListOf<String>()
        skuList.add(ONE_WEEK_STANDARD)
        skuList.add(ONE_WEEK_PRO)

        skuList.add(ONE_MONTH_STANDARD)
        skuList.add(ONE_MONTH_PRO)

        skuList.add(ONE_YEAR_STANDARD)
        skuList.add(ONE_YEAR_PRO)

        skuList.add(TWO_YEARS_STANDARD)
        skuList.add(TWO_YEARS_PRO)

        skuList.add(THREE_YEARS_STANDARD)
        skuList.add(THREE_YEARS_PRO)

        return skuList
    }
}