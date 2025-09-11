package com.xypay.analytics.util;

import java.util.Map;

public final class PiiMaskingUtil {

    private PiiMaskingUtil() {}

    public static void maskInPlace(Map<String, Object> map) {
        if (map == null) return;
        map.replaceAll((k, v) -> isSensitive(k) && v != null ? mask(String.valueOf(v)) : v);
    }

    private static boolean isSensitive(String key) {
        String k = key.toLowerCase();
        return k.contains("email") || k.contains("phone") || k.contains("ssn") || k.contains("bvn") || k.contains("card");
    }

    private static String mask(String value) {
        int n = value.length();
        if (n <= 4) return "****";
        return "****" + value.substring(n - 4);
    }
}


