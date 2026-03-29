package com.knubisoft.testlum.testing.framework.report;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandResultTest {

    @Nested
    class Defaults {
        @Test
        void defaultValuesAreCorrect() {
            final CommandResult result = new CommandResult();
            assertEquals(0, result.getId());
            assertNull(result.getCommandKey());
            assertNull(result.getComment());
            assertNull(result.getExpected());
            assertNull(result.getActual());
            assertFalse(result.isSuccess());
            assertFalse(result.isSkipped());
            assertNull(result.getException());
            assertEquals(0, result.getExecutionTime());
            assertNull(result.getBase64Screenshot());
            assertNull(result.getSubCommandsResult());
            assertNotNull(result.getMetadata());
        }
    }

    @Nested
    class Put {
        @Test
        void putsValueIntoMetadata() {
            final CommandResult result = new CommandResult();
            result.put("key", "value");
            assertEquals("value", result.getMetadata().get("key"));
        }

        @Test
        void keyIsConvertedToString() {
            final CommandResult result = new CommandResult();
            result.put(42, "number-key");
            assertEquals("number-key", result.getMetadata().get("42"));
        }

        @Test
        void overwritesExistingKey() {
            final CommandResult result = new CommandResult();
            result.put("k", "old");
            result.put("k", "new");
            assertEquals("new", result.getMetadata().get("k"));
        }
    }

    @Nested
    class SettersAndGetters {
        @Test
        void setAndGetAllFields() {
            final CommandResult result = new CommandResult();
            final Exception ex = new RuntimeException("err");

            result.setId(5);
            result.setCommandKey("http");
            result.setComment("my comment");
            result.setExpected("exp");
            result.setActual("act");
            result.setSuccess(true);
            result.setSkipped(true);
            result.setException(ex);
            result.setExecutionTime(1234);
            result.setBase64Screenshot("base64data");
            result.setSubCommandsResult(List.of(new CommandResult()));

            assertEquals(5, result.getId());
            assertEquals("http", result.getCommandKey());
            assertEquals("my comment", result.getComment());
            assertEquals("exp", result.getExpected());
            assertEquals("act", result.getActual());
            assertEquals(true, result.isSuccess());
            assertEquals(true, result.isSkipped());
            assertEquals(ex, result.getException());
            assertEquals(1234, result.getExecutionTime());
            assertEquals("base64data", result.getBase64Screenshot());
            assertEquals(1, result.getSubCommandsResult().size());
        }
    }
}
