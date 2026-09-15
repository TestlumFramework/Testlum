package com.testlum.testing.framework.report.testrail.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestRailCaseListDeserializerTest {

    private static final String MATCH_KEY = "custom_automation_id";

    private final TestRailResponseJsonDeserializer deserializer = new TestRailResponseJsonDeserializer();

    @Nested
    class ResponseShapes {

        @Test
        void readsBareArrayResponse() {
            String json = "[{\"id\":1,\"custom_automation_id\":\"A\"},{\"id\":2,\"custom_automation_id\":\"B\"}]";
            Map<String, Integer> target = new HashMap<>();

            int size = deserializer.collectCaseIdsByMatchKey(json, MATCH_KEY, target);

            assertEquals(2, size);
            assertEquals(1, target.get("A"));
            assertEquals(2, target.get("B"));
        }

        @Test
        void readsPaginatedEnvelopeResponse() {
            String json = "{\"offset\":0,\"limit\":250,\"size\":1,\"_links\":{\"next\":null},"
                    + "\"cases\":[{\"id\":9,\"custom_automation_id\":\"LOGIN_001\"}]}";
            Map<String, Integer> target = new HashMap<>();

            int size = deserializer.collectCaseIdsByMatchKey(json, MATCH_KEY, target);

            assertEquals(1, size);
            assertEquals(9, target.get("LOGIN_001"));
        }

        @Test
        void returnsZeroWhenNoCasesNode() {
            Map<String, Integer> target = new HashMap<>();

            assertEquals(0, deserializer.collectCaseIdsByMatchKey("{\"error\":\"nope\"}", MATCH_KEY, target));
            assertTrue(target.isEmpty());
        }
    }

    @Nested
    class ValueHandling {

        @Test
        void skipsCasesWithoutMatchKeyValue() {
            String json = "[{\"id\":1,\"custom_automation_id\":null},{\"id\":2},"
                    + "{\"id\":3,\"custom_automation_id\":\"\"},{\"id\":4,\"custom_automation_id\":\"D\"}]";
            Map<String, Integer> target = new HashMap<>();

            int size = deserializer.collectCaseIdsByMatchKey(json, MATCH_KEY, target);

            assertEquals(4, size);
            assertEquals(1, target.size());
            assertEquals(4, target.get("D"));
        }

        @Test
        void firstCaseWinsWhenMatchKeyValueIsDuplicated() {
            String json = "[{\"id\":1,\"custom_automation_id\":\"DUP\"},{\"id\":2,\"custom_automation_id\":\"DUP\"}]";
            Map<String, Integer> target = new HashMap<>();

            deserializer.collectCaseIdsByMatchKey(json, MATCH_KEY, target);

            assertEquals(1, target.get("DUP"));
        }

        @Test
        void readsNonTextualMatchKeyValue() {
            String json = "[{\"id\":7,\"custom_automation_id\":12345}]";
            Map<String, Integer> target = new HashMap<>();

            deserializer.collectCaseIdsByMatchKey(json, MATCH_KEY, target);

            assertEquals(7, target.get("12345"));
        }

        @Test
        void accumulatesAcrossPages() {
            Map<String, Integer> target = new HashMap<>();

            deserializer.collectCaseIdsByMatchKey("[{\"id\":1,\"custom_automation_id\":\"A\"}]", MATCH_KEY, target);
            deserializer.collectCaseIdsByMatchKey("[{\"id\":2,\"custom_automation_id\":\"B\"}]", MATCH_KEY, target);

            assertEquals(2, target.size());
            assertEquals(1, target.get("A"));
            assertEquals(2, target.get("B"));
            assertNull(target.get("C"));
        }
    }
}
