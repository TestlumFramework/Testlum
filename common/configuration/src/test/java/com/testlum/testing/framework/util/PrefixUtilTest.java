package com.testlum.testing.framework.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrefixUtilTest {

    @Test
    void appliesPrefixWhenPresent() {
        assertEquals("included_login_action_1_actual.json",
                PrefixUtil.applyPrefix("included_login", "action_1_actual.json"));
    }

    @Test
    void returnsOriginalFileNameWhenPrefixIsNull() {
        assertEquals("action_1_actual.json",
                PrefixUtil.applyPrefix(null, "action_1_actual.json"));
    }

    @Test
    void returnsOriginalFileNameWhenPrefixIsEmpty() {
        assertEquals("action_1_actual.json",
                PrefixUtil.applyPrefix("", "action_1_actual.json"));
    }

    @Test
    void returnsOriginalFileNameWhenPrefixIsBlank() {
        assertEquals("action_1_actual.json",
                PrefixUtil.applyPrefix("   ", "action_1_actual.json"));
    }
}
