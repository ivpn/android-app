package net.ivpn.client.common.billing;

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