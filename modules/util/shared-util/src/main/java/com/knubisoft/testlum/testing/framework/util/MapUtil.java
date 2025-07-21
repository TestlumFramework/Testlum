package com.knubisoft.testlum.testing.framework.util;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class MapUtil {

    public <K, V> Map.Entry<K, V> getLastEntryFromLinkedHashMap(final Map<K, V> map) {
        Map.Entry<K, V> last = null;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            last = entry;
        }
        return last;
    }

}