package net.ivpn.client.billing;

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


import java.util.ArrayList;
import java.util.List;

public class Sku {

    public static final String MONTH_STANDARD_SKU = "1_month_standard";
    public static final String MONTH_PRO_SKU = "1_month_pro";
    public static final String YEAR_STANDARD_SKU = "1_year_standard";
    public static final String YEAR_PRO_SKU = "1_year_pro";

    public static List<String> getAllSku() {
        List<String> skuList = new ArrayList<>();
        skuList.add(MONTH_STANDARD_SKU);
        skuList.add(MONTH_PRO_SKU);
        skuList.add(YEAR_STANDARD_SKU);
        skuList.add(YEAR_PRO_SKU);

        return skuList;
    }
}