package com.knubisoft.testlum.testing.framework.util;

import java.util.Map;

public class MapUtil {

    private MapUtil() {
        // empty
    }

    public static <K, V> Map.Entry<K, V> getLastEntryFromLinkedHashMap(final Map<K, V> map) {
        Map.Entry<K, V> last = null;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            last = entry;
        }
        return last;
    }

}