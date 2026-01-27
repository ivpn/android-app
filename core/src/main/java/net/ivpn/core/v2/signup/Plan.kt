package net.ivpn.core.common.billing.addfunds

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

enum class Plan(
        val skuPath: String,
        val productName: String,
        val title: String,
        val description: String
) {
    STANDARD(
            skuPath = "net.ivpn.subscriptions.standard.",
            productName = "IVPN Standard",
            title = "IVPN Standard",
            description = "IVPN on 5 devices"
    ),
    PLUS(
            skuPath = "net.ivpn.subscriptions.plus.",
            productName = "IVPN Plus",
            title = "IVPN Plus",
            description = "IVPN on 10 devices, modDNS, Mailx"
    ),
    PRO(
            skuPath = "net.ivpn.subscriptions.pro.",
            productName = "IVPN Pro",
            title = "IVPN Pro Suite",
            description = "IVPN on 10 devices, modDNS, Mailx, Portmaster Pro"
    );

    companion object {

        fun getPlanByProductName(productName: String?): Plan {
            if (productName == null) return STANDARD

            return when {
                productName.contains("Plus", ignoreCase = true) -> PLUS
                productName.contains("Pro", ignoreCase = true) -> PRO
                else -> STANDARD
            }
        }
    }

    fun getPlanTitle(): String = title

    fun getPlanDesc(): String = description

    fun isStandard(): Boolean = this == STANDARD

    fun getAltTitleOne(): String =
            when (this) {
                STANDARD -> PLUS.title
                PLUS -> STANDARD.title
                PRO -> STANDARD.title
            }

    fun getAltDescOne(): String =
            when (this) {
                STANDARD -> PLUS.description
                PLUS -> STANDARD.description
                PRO -> STANDARD.description
            }

    fun getAltTitleTwo(): String =
            when (this) {
                STANDARD -> PRO.title
                PLUS -> PRO.title
                PRO -> PLUS.title
            }

    fun getAltDescTwo(): String =
            when (this) {
                STANDARD -> PRO.description
                PLUS -> PRO.description
                PRO -> PLUS.description
            }
}
