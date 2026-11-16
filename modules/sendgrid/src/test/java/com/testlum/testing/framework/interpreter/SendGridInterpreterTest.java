package com.testlum.testing.framework.interpreter;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.http.util.HttpUtil;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Body;
import com.testlum.testing.model.scenario.Header;
import com.testlum.testing.model.scenario.Mode;
import com.testlum.testing.model.scenario.Multipart;
import com.testlum.testing.model.scenario.QueryParam;
import com.testlum.testing.model.scenario.SendgridInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SendGridInterpreterTest {

    @TempDir
    File tempDir;

    private SendGridInterpreter interpreter;
    private FileSearcher fileSearcher;

    @BeforeEach
    void setUp() {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        fileSearcher = mock(FileSearcher.class);
        when(applicationContext.getBean(FileSearcher.class)).thenReturn(fileSearcher);
        when(applicationContext.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(applicationContext.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        when(applicationContext.getBean(JacksonService.class)).thenReturn(mock(JacksonService.class));

        final StringPrettifier prettifier = mock(StringPrettifier.class);
        when(prettifier.asJsonResult(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(prettifier.cut(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(prettifier.prettify(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(prettifier.prettifyToSave(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(applicationContext.getBean(StringPrettifier.class)).thenReturn(prettifier);

        when(applicationContext.getBean(HttpUtil.class)).thenReturn(mock(HttpUtil.class));
        when(applicationContext.getBean(SendGridUtil.class)).thenReturn(mock(SendGridUtil.class));

        final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
        when(applicationContext.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);

        final InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(applicationContext)
                .file(new File(tempDir, "scenario.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        interpreter = new SendGridInterpreter(dependencies);
    }

    @Nested
    class Construction {

        @Test
        void interpreterIsNotNull() {
            assertNotNull(interpreter);
        }
    }

    @Nested
    class BodyExtraction {

        @Test
        void throwsExceptionWhenMultipartIsConfigured() {
            final SendGridUtil util = new SendGridUtil(new JacksonService());
            final Body body = new Body();
            body.setMultipart(new Multipart());

            final DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> util.extractBody(body, interpreter));
            assertTrue(ex.getMessage().contains("Multipart request body is not supported for <sendgrid>"));
        }
    }

    @Nested
    class AddSendGridMetaData {

        @Test
        void addsMetadataCorrectly() throws Exception {
            final CommandResult result = new CommandResult();
            final Method method = SendGridInterpreter.class.getDeclaredMethod(
                    "addSendGridMetaData", String.class, String.class, Map.class, String.class, List.class,
                    CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, "alias", "POST", Map.of("key", "val"), "/v3/mail/send",
                    List.of("param=val"), result);

            assertEquals("alias", result.getMetadata().get("Alias"));
            assertEquals("POST", result.getMetadata().get("HTTP method"));
            assertEquals("/v3/mail/send", result.getMetadata().get("Endpoint"));
            assertEquals(List.of("key: val"), result.getMetadata().get("Additional headers"));
            assertEquals("param=val", result.getMetadata().get("Query parameters"));
        }
    }

    @Nested
    class GetRequest {

        @Test
        void populatesRequestFieldsAndHeaders() throws Exception {
            final Method method = SendGridInterpreter.class.getDeclaredMethod(
                    "getRequest", String.class, com.sendgrid.Method.class, SendgridInfo.class,
                    String.class, Map.class);
            method.setAccessible(true);

            final SendgridInfo sendgridInfo = mock(SendgridInfo.class);
            final QueryParam qp = new QueryParam();
            qp.setKey("start_date");
            qp.setValue("2026-01-01");
            when(sendgridInfo.getQueryParam()).thenReturn(List.of(qp));

            final Map<String, String> requestHeaders = Map.of(
                    "Authorization", "Bearer SG.testKey",
                    "X-Test-Request", "test-val"
            );

            final Request request = (Request) method.invoke(
                    interpreter, "{\"body\":\"test\"}", com.sendgrid.Method.POST,
                    sendgridInfo, "/v3/mail/send", requestHeaders);

            assertNotNull(request);
            assertEquals(com.sendgrid.Method.POST, request.getMethod());
            assertEquals("/v3/mail/send", request.getEndpoint());
            assertEquals("{\"body\":\"test\"}", request.getBody());
            assertEquals("2026-01-01", request.getQueryParams().get("start_date"));
            assertEquals("Bearer SG.testKey", request.getHeaders().get("Authorization"));
            assertEquals("test-val", request.getHeaders().get("X-Test-Request"));
        }
    }

    @Nested
    class Compare {

        @Test
        void strictModeRejectsExtraFieldsInActual() throws Exception {
            when(fileSearcher.searchFileToString(anyString(), any())).thenReturn("{\"id\":1}");

            final com.testlum.testing.model.scenario.Response expected =
                    new com.testlum.testing.model.scenario.Response();
            expected.setCode(200);
            expected.setFile("expected.json");
            expected.setMode(Mode.STRICT);

            final Response actual = new Response(200, "{\"id\":1,\"extra\":\"field\"}", Collections.emptyMap());
            final CommandResult result = new CommandResult();

            final Method method = SendGridInterpreter.class.getDeclaredMethod(
                    "compare", com.testlum.testing.model.scenario.Response.class,
                    Response.class, CommandResult.class);
            method.setAccessible(true);

            assertThrows(Exception.class, () -> {
                try {
                    method.invoke(interpreter, expected, actual, result);
                } catch (Exception e) {
                    throw e.getCause() != null ? (Exception) e.getCause() : e;
                }
            });
        }

        @Test
        void defaultModeRejectsExtraFieldsInActual() throws Exception {
            when(fileSearcher.searchFileToString(anyString(), any())).thenReturn("{\"id\":1}");

            final com.testlum.testing.model.scenario.Response expected =
                    new com.testlum.testing.model.scenario.Response();
            expected.setCode(200);
            expected.setFile("expected.json");
            // mode is null, getMode() returns STRICT by default

            final Response actual = new Response(200, "{\"id\":1,\"extra\":\"field\"}", Collections.emptyMap());
            final CommandResult result = new CommandResult();

            final Method method = SendGridInterpreter.class.getDeclaredMethod(
                    "compare", com.testlum.testing.model.scenario.Response.class,
                    Response.class, CommandResult.class);
            method.setAccessible(true);

            assertThrows(Exception.class, () -> {
                try {
                    method.invoke(interpreter, expected, actual, result);
                } catch (Exception e) {
                    throw e.getCause() != null ? (Exception) e.getCause() : e;
                }
            });
        }

        @Test
        void lenientModeAllowsExtraFieldsInActual() throws Exception {
            when(fileSearcher.searchFileToString(anyString(), any())).thenReturn("{\"id\":1}");

            final com.testlum.testing.model.scenario.Response expected =
                    new com.testlum.testing.model.scenario.Response();
            expected.setCode(200);
            expected.setFile("expected.json");
            expected.setMode(Mode.LENIENT);

            final Response actual = new Response(200, "{\"id\":1,\"extra\":\"field\"}", Collections.emptyMap());
            final CommandResult result = new CommandResult();

            final Method method = SendGridInterpreter.class.getDeclaredMethod(
                    "compare", com.testlum.testing.model.scenario.Response.class,
                    Response.class, CommandResult.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> method.invoke(interpreter, expected, actual, result));
        }

        @Test
        void validatesMatchingResponseHeadersSuccessfully() throws Exception {
            when(fileSearcher.searchFileToString(anyString(), any())).thenReturn("{\"id\":1}");

            final com.testlum.testing.model.scenario.Response expected =
                    new com.testlum.testing.model.scenario.Response();
            expected.setCode(200);
            expected.setFile("expected.json");

            final Header header = new Header();
            header.setName("Content-Type");
            header.setData("application/json");
            expected.getHeader().add(header);

            final Response actual = new Response(
                    200, "{\"id\":1}", Map.of("Content-Type", "application/json"));
            final CommandResult result = new CommandResult();

            final Method method = SendGridInterpreter.class.getDeclaredMethod(
                    "compare", com.testlum.testing.model.scenario.Response.class,
                    Response.class, CommandResult.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> method.invoke(interpreter, expected, actual, result));
        }

        @Test
        void throwsExceptionWhenResponseHeaderMismatches() throws Exception {
            when(fileSearcher.searchFileToString(anyString(), any())).thenReturn("{\"id\":1}");

            final com.testlum.testing.model.scenario.Response expected =
                    new com.testlum.testing.model.scenario.Response();
            expected.setCode(200);
            expected.setFile("expected.json");

            final Header header = new Header();
            header.setName("Content-Type");
            header.setData("text/plain");
            expected.getHeader().add(header);

            final Response actual = new Response(
                    200, "{\"id\":1}", Map.of("Content-Type", "application/json"));
            final CommandResult result = new CommandResult();

            final Method method = SendGridInterpreter.class.getDeclaredMethod(
                    "compare", com.testlum.testing.model.scenario.Response.class,
                    Response.class, CommandResult.class);
            method.setAccessible(true);

            assertThrows(Exception.class, () -> {
                try {
                    method.invoke(interpreter, expected, actual, result);
                } catch (Exception e) {
                    throw e.getCause() != null ? (Exception) e.getCause() : e;
                }
            });
        }

        @Test
        void throwsExceptionWhenExpectedHeaderMissingInActualResponse() throws Exception {
            when(fileSearcher.searchFileToString(anyString(), any())).thenReturn("{\"id\":1}");

            final com.testlum.testing.model.scenario.Response expected =
                    new com.testlum.testing.model.scenario.Response();
            expected.setCode(200);
            expected.setFile("expected.json");

            final Header header = new Header();
            header.setName("X-Custom-Header");
            header.setData("custom-value");
            expected.getHeader().add(header);

            final Response actual = new Response(200, "{\"id\":1}", Collections.emptyMap());
            final CommandResult result = new CommandResult();

            final Method method = SendGridInterpreter.class.getDeclaredMethod(
                    "compare", com.testlum.testing.model.scenario.Response.class,
                    Response.class, CommandResult.class);
            method.setAccessible(true);

            assertThrows(Exception.class, () -> {
                try {
                    method.invoke(interpreter, expected, actual, result);
                } catch (Exception e) {
                    throw e.getCause() != null ? (Exception) e.getCause() : e;
                }
            });
        }
    }
}
