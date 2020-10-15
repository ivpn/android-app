package net.ivpn.client.common.billing

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