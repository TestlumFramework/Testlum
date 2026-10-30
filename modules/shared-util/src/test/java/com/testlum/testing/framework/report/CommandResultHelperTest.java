package com.testlum.testing.framework.report;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CommandResultHelper} verifying exception result setting,
 * sub-command failure propagation, and header metadata.
 */
class CommandResultHelperTest {

    @Nested
    class SetExceptionResult {
        @Test
        void setsSuccessToFalseAndException() {
            final CommandResult result = new CommandResult();
            result.setSuccess(true);
            final Exception ex = new RuntimeException("test error");

            CommandResultHelper.setExceptionResult(result, ex);

            assertFalse(result.isSuccess());
            assertEquals(ex, result.getException());
        }
    }

    @Nested
    class SetExecutionResultIfSubCommandsFailed {
        @Test
        void noFailedSubCommands() {
            final CommandResult result = new CommandResult();
            result.setSuccess(true);

            final CommandResult sub1 = new CommandResult();
            sub1.setSuccess(true);
            final CommandResult sub2 = new CommandResult();
            sub2.setSuccess(true);
            result.setSubCommandsResult(List.of(sub1, sub2));

            CommandResultHelper.setExecutionResultIfSubCommandsFailed(result);

            assertTrue(result.isSuccess());
            assertNull(result.getException());
        }

        @Test
        void failedSubCommandPropagatesException() {
            final CommandResult result = new CommandResult();
            result.setSuccess(true);

            final Exception subEx = new RuntimeException("sub failed");
            final CommandResult sub1 = new CommandResult();
            sub1.setSuccess(true);
            final CommandResult sub2 = new CommandResult();
            sub2.setSuccess(false);
            sub2.setException(subEx);
            result.setSubCommandsResult(List.of(sub1, sub2));

            CommandResultHelper.setExecutionResultIfSubCommandsFailed(result);

            assertFalse(result.isSuccess());
            assertEquals(subEx, result.getException());
        }

        @Test
        void skippedSubCommandNotTreatedAsFailure() {
            final CommandResult result = new CommandResult();
            result.setSuccess(true);

            final CommandResult sub1 = new CommandResult();
            sub1.setSuccess(false);
            sub1.setSkipped(true);
            result.setSubCommandsResult(List.of(sub1));

            CommandResultHelper.setExecutionResultIfSubCommandsFailed(result);

            assertTrue(result.isSuccess());
        }

        @Test
        void failedSubWithNoExceptionGetsDefault() {
            final CommandResult result = new CommandResult();
            result.setSuccess(true);

            final CommandResult sub1 = new CommandResult();
            sub1.setSuccess(false);
            // no exception set on sub1
            result.setSubCommandsResult(List.of(sub1));

            CommandResultHelper.setExecutionResultIfSubCommandsFailed(result);

            assertFalse(result.isSuccess());
            assertNotNull(result.getException());
        }
    }

    @Nested
    class AddHeadersMetaData {
        @Test
        void masksAuthorizationRegardlessOfCaseWithoutChangingRequestHeaders() {
            for (String name : List.of("Authorization", "authorization", "AUTHORIZATION", "aUtHoRiZaTiOn")) {
                final Map<String, String> headers = Map.of(
                        name, "Bearer sensitive-access-value", "Accept", "text/html");
                final CommandResult result = new CommandResult();

                CommandResultHelper.addHeadersMetaData(headers, result);

                final List<?> metadata = (List<?>) result.getMetadata().get(CommandResultHelper.ADDITIONAL_HEADERS);
                assertTrue(metadata.contains(name + ": [REDACTED]"));
                assertTrue(metadata.contains("Accept: text/html"));
                assertEquals("Bearer sensitive-access-value", headers.get(name));
                assertFalse(result.getMetadata().toString().contains("sensitive-access-value"));
            }
        }

        @Test
        void masksBasicAndProxyAuthorizationWithoutChangingUnrelatedHeaders() {
            final Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Authorization", "Basic sensitive-basic-value");
            headers.put("proxy-authorization", "Basic sensitive-proxy-value");
            headers.put("Content-Type", "application/json");
            final CommandResult result = new CommandResult();

            CommandResultHelper.addHeadersMetaData(headers, result);

            assertEquals(List.of("Authorization: [REDACTED]", "proxy-authorization: [REDACTED]",
                    "Content-Type: application/json"),
                    result.getMetadata().get(CommandResultHelper.ADDITIONAL_HEADERS));
            assertEquals("Basic sensitive-basic-value", headers.get("Authorization"));
            assertEquals("Basic sensitive-proxy-value", headers.get("proxy-authorization"));
        }

        @Test
        void addsHeadersToMetadata() {
            final CommandResult result = new CommandResult();
            final Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "text/html");

            CommandResultHelper.addHeadersMetaData(headers, result);

            final Object metadata = result.getMetadata()
                    .get(CommandResultHelper.ADDITIONAL_HEADERS);
            assertNotNull(metadata);
            @SuppressWarnings("unchecked")
            final List<String> headerList = (List<String>) metadata;
            assertEquals(2, headerList.size());
            assertTrue(headerList.contains("Content-Type: application/json"));
            assertTrue(headerList.contains("Accept: text/html"));
        }

        @Test
        void emptyHeadersAddsEmptyList() {
            final CommandResult result = new CommandResult();
            final Map<String, String> headers = new LinkedHashMap<>();

            CommandResultHelper.addHeadersMetaData(headers, result);

            final Object metadata = result.getMetadata()
                    .get(CommandResultHelper.ADDITIONAL_HEADERS);
            assertNotNull(metadata);
            @SuppressWarnings("unchecked")
            final List<String> headerList = (List<String>) metadata;
            assertTrue(headerList.isEmpty());
        }
    }

    @Test
    void constantsHaveExpectedValues() {
        assertEquals("%s: %s", CommandResultHelper.HEADER_TEMPLATE);
        assertEquals("Additional headers", CommandResultHelper.ADDITIONAL_HEADERS);
    }
}
