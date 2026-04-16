package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ResultUtil} verifying command result creation,
 * metadata population, and failure propagation.
 */
class ResultUtilTest {

    private final ResultUtil resultUtil = new ResultUtil();

    @Nested
    class NewCommandResultInstance {
        @Test
        void createsWithIdAndSuccess() {
            final CommandResult result = resultUtil.newCommandResultInstance(1);
            assertEquals(1, result.getId());
            assertTrue(result.isSuccess());
            assertNull(result.getCommandKey());
        }

        @Test
        void createsWithCommandKey() {
            final CommandResult result = resultUtil.newCommandResultInstance(5);
            assertEquals(5, result.getId());
            assertTrue(result.isSuccess());
        }
    }

    @Nested
    class SetExpectedActual {
        @Test
        void setsExpectedAndActual() {
            final CommandResult result = new CommandResult();
            resultUtil.setExpectedActual("exp", "act", result);
            assertEquals("exp", result.getExpected());
            assertEquals("act", result.getActual());
        }
    }

    @Nested
    class SetExceptionResult {
        @Test
        void setsFailureAndException() {
            final CommandResult result = new CommandResult();
            result.setSuccess(true);
            final Exception ex = new RuntimeException("test");
            resultUtil.setExceptionResult(result, ex);
            assertFalse(result.isSuccess());
            assertEquals(ex, result.getException());
        }
    }

    @Nested
    class SetExecutionResultIfSubCommandsFailed {
        @Test
        void propagatesFailure() {
            final CommandResult parent = new CommandResult();
            parent.setSuccess(true);
            final CommandResult child = new CommandResult();
            child.setSuccess(false);
            child.setException(new RuntimeException("child failed"));
            parent.setSubCommandsResult(List.of(child));

            resultUtil.setExecutionResultIfSubCommandsFailed(parent);

            assertFalse(parent.isSuccess());
        }

        @Test
        void noFailureKeepsSuccess() {
            final CommandResult parent = new CommandResult();
            parent.setSuccess(true);
            final CommandResult child = new CommandResult();
            child.setSuccess(true);
            parent.setSubCommandsResult(List.of(child));

            resultUtil.setExecutionResultIfSubCommandsFailed(parent);

            assertTrue(parent.isSuccess());
        }
    }

    @Nested
    class AddHttpMetaData {
        @Test
        void addsMetadata() {
            final CommandResult result = new CommandResult();
            final Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Accept", "application/json");

            resultUtil.addHttpMetaData("myAlias", "GET", headers, "/api/test", result);

            assertEquals("myAlias", result.getMetadata().get("API alias"));
            assertEquals("/api/test", result.getMetadata().get("Endpoint"));
            assertEquals("GET", result.getMetadata().get("HTTP method"));
        }

        @Test
        void emptyHeadersSkipsHeaderMetadata() {
            final CommandResult result = new CommandResult();
            resultUtil.addHttpMetaData("alias", "POST", Map.of(), "/ep", result);
            assertNotNull(result.getMetadata().get("API alias"));
        }
    }

    @Nested
    class AddVariableMetaData {
        @Test
        void addsVariableInfo() {
            final CommandResult result = new CommandResult();
            resultUtil.addVariableMetaData("Constant", "myKey", "expression", "value", result);
            assertEquals("Constant", result.getMetadata().get("Type"));
            assertEquals("myKey", result.getMetadata().get("Name"));
            assertEquals("value", result.getMetadata().get("Value"));
        }
    }

    @Nested
    class AddConditionMetaData {
        @Test
        void addsConditionInfo() {
            final CommandResult result = new CommandResult();
            resultUtil.addConditionMetaData("cond1", "true && false", false, result);
            assertEquals("cond1", result.getMetadata().get("Name"));
            assertEquals("true && false", result.getMetadata().get("Expression"));
            assertEquals(false, result.getMetadata().get("Value"));
        }
    }

    @Nested
    class AddCommandOnConditionMetaData {
        @Test
        void trueConditionNotSkipped() {
            final CommandResult result = new CommandResult();
            resultUtil.addCommandOnConditionMetaData("flag", true, result);
            assertFalse(result.isSkipped());
        }

        @Test
        void falseConditionSkipped() {
            final CommandResult result = new CommandResult();
            resultUtil.addCommandOnConditionMetaData("flag", false, result);
            assertTrue(result.isSkipped());
        }

        @Test
        void conditionMetadataContainsNameAndValue() {
            final CommandResult result = new CommandResult();
            resultUtil.addCommandOnConditionMetaData("myFlag", true, result);
            assertEquals("myFlag = true", result.getMetadata().get("Condition"));
        }
    }

    @Nested
    class AddWaitMetaData {
        @Test
        void addsTimeAndUnit() {
            final CommandResult result = new CommandResult();
            resultUtil.addWaitMetaData("5", java.util.concurrent.TimeUnit.SECONDS, result);
            assertEquals("5", result.getMetadata().get("Time"));
            assertEquals("SECONDS", result.getMetadata().get("Time unit"));
        }
    }

    @Nested
    class AddScrollMetaData {
        @Test
        void addsScrollInfo() {
            final CommandResult result = new CommandResult();
            final com.knubisoft.testlum.testing.model.scenario.Scroll scroll =
                    new com.knubisoft.testlum.testing.model.scenario.Scroll();
            scroll.setDirection(com.knubisoft.testlum.testing.model.scenario.ScrollDirection.DOWN);
            scroll.setMeasure(com.knubisoft.testlum.testing.model.scenario.ScrollMeasure.PIXEL);
            scroll.setValue(300);
            scroll.setType(com.knubisoft.testlum.testing.model.scenario.ScrollType.PAGE);

            resultUtil.addScrollMetaData(scroll, result);

            assertEquals(com.knubisoft.testlum.testing.model.scenario.ScrollDirection.DOWN,
                    result.getMetadata().get("Scroll direction"));
            assertEquals(com.knubisoft.testlum.testing.model.scenario.ScrollMeasure.PIXEL,
                    result.getMetadata().get("Scroll measure"));
            assertEquals(300, result.getMetadata().get("Value"));
        }
    }

    @Nested
    class SetExecutionResultWithEmptySubCommands {
        @Test
        void emptySubCommandsKeepsSuccess() {
            final CommandResult parent = new CommandResult();
            parent.setSuccess(true);
            parent.setSubCommandsResult(List.of());

            resultUtil.setExecutionResultIfSubCommandsFailed(parent);

            assertTrue(parent.isSuccess());
        }
    }
}
