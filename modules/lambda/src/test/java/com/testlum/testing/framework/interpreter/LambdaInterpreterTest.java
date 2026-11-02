package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Mode;
import com.testlum.testing.model.scenario.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LambdaInterpreterTest {

    @TempDir
    File tempDir;

    private LambdaInterpreter interpreter;
    private FileSearcher fileSearcher;

    @BeforeEach
    void setUp() {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(applicationContext.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        fileSearcher = mock(FileSearcher.class);
        when(applicationContext.getBean(FileSearcher.class)).thenReturn(fileSearcher);
        when(applicationContext.getBean(JacksonService.class)).thenReturn(mock(JacksonService.class));
        when(applicationContext.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
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

        interpreter = new LambdaInterpreter(dependencies);
    }

    @Nested
    class Construction {

        @Test
        void interpreterIsNotNull() {
            assertNotNull(interpreter);
        }
    }

    @Nested
    class AddLambdaGeneralMetaData {

        @Test
        void addsAllMetadataToResult() throws Exception {
            final StringPrettifier prettifier = mock(StringPrettifier.class);
            when(prettifier.asJsonResult("{\"key\":\"value\"}")).thenReturn("{\"key\":\"value\"}");

            final java.lang.reflect.Field field = LambdaInterpreter.class.getSuperclass()
                    .getDeclaredField("stringPrettifier");
            field.setAccessible(true);
            field.set(interpreter, prettifier);

            final CommandResult result = new CommandResult();
            final Method method = LambdaInterpreter.class.getDeclaredMethod(
                    "addLambdaGeneralMetaData", String.class, String.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, "myAlias", "myFunction", "{\"key\":\"value\"}", result);

            assertEquals("myAlias", result.getMetadata().get("Alias"));
            assertEquals("myFunction", result.getMetadata().get("Function name"));
        }
    }

    @Nested
    class ValidateBody {

        @Test
        void strictModePassesStrictFlag() throws Exception {
            final StringPrettifier prettifier = mock(StringPrettifier.class);
            when(prettifier.asJsonResult(anyString())).thenAnswer(inv -> inv.getArgument(0));

            final java.lang.reflect.Field field = LambdaInterpreter.class.getSuperclass()
                    .getDeclaredField("stringPrettifier");
            field.setAccessible(true);
            field.set(interpreter, prettifier);

            final Response expected = new Response();
            expected.setMode(Mode.STRICT);

            final HttpValidator validator = mock(HttpValidator.class);
            final CommandResult result = new CommandResult();

            final Method method = LambdaInterpreter.class.getDeclaredMethod(
                    "validateBody", Response.class, String.class, HttpValidator.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, expected, "{\"a\":1}", validator, result);

            verify(validator).validateBody("", "{\"a\":1}", true);
        }

        @Test
        void lenientModePassesLenientFlag() throws Exception {
            final StringPrettifier prettifier = mock(StringPrettifier.class);
            when(prettifier.asJsonResult(anyString())).thenAnswer(inv -> inv.getArgument(0));

            final java.lang.reflect.Field field = LambdaInterpreter.class.getSuperclass()
                    .getDeclaredField("stringPrettifier");
            field.setAccessible(true);
            field.set(interpreter, prettifier);

            final Response expected = new Response();
            expected.setMode(Mode.LENIENT);

            final HttpValidator validator = mock(HttpValidator.class);
            final CommandResult result = new CommandResult();

            final Method method = LambdaInterpreter.class.getDeclaredMethod(
                    "validateBody", Response.class, String.class, HttpValidator.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, expected, "{\"a\":1,\"b\":2}", validator, result);

            verify(validator).validateBody("", "{\"a\":1,\"b\":2}", false);
        }

        @Test
        void defaultModeDefaultsToStrict() throws Exception {
            final StringPrettifier prettifier = mock(StringPrettifier.class);
            when(prettifier.asJsonResult(anyString())).thenAnswer(inv -> inv.getArgument(0));

            final java.lang.reflect.Field field = LambdaInterpreter.class.getSuperclass()
                    .getDeclaredField("stringPrettifier");
            field.setAccessible(true);
            field.set(interpreter, prettifier);

            final Response expected = new Response(); // mode is null by default, getMode() returns STRICT

            final HttpValidator validator = mock(HttpValidator.class);
            final CommandResult result = new CommandResult();

            final Method method = LambdaInterpreter.class.getDeclaredMethod(
                    "validateBody", Response.class, String.class, HttpValidator.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, expected, "{\"a\":1}", validator, result);

            verify(validator).validateBody("", "{\"a\":1}", true);
        }
    }

    @Nested
    class CompareResult {

        private void setupPrettifier() throws Exception {
            final StringPrettifier prettifier = mock(StringPrettifier.class);
            when(prettifier.asJsonResult(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(prettifier.cut(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(prettifier.prettify(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(prettifier.prettifyToSave(anyString())).thenAnswer(inv -> inv.getArgument(0));

            final java.lang.reflect.Field field = LambdaInterpreter.class.getSuperclass()
                    .getDeclaredField("stringPrettifier");
            field.setAccessible(true);
            field.set(interpreter, prettifier);
        }

        private InvokeResponse createInvokeResponse(final String payloadJson) {
            final InvokeResponse invokeResponse = mock(InvokeResponse.class);
            when(invokeResponse.payload()).thenReturn(SdkBytes.fromUtf8String(payloadJson));
            final SdkHttpResponse httpResponse = mock(SdkHttpResponse.class);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.headers()).thenReturn(Collections.emptyMap());
            when(invokeResponse.sdkHttpResponse()).thenReturn(httpResponse);
            return invokeResponse;
        }

        @Test
        void strictModeRejectsExtraFieldsInActual() throws Exception {
            setupPrettifier();
            when(fileSearcher.searchFileToString(anyString(), any())).thenReturn("{\"status\":\"ok\"}");

            final Response expected = new Response();
            expected.setCode(200);
            expected.setFile("expected.json");
            expected.setMode(Mode.STRICT);

            final InvokeResponse invokeResponse =
                    createInvokeResponse("{\"status\":\"ok\",\"extraField\":\"unexpected\"}");

            final CommandResult result = new CommandResult();
            final Method method = LambdaInterpreter.class.getDeclaredMethod(
                    "compareResult", Response.class, InvokeResponse.class, CommandResult.class);
            method.setAccessible(true);

            assertThrows(Exception.class, () -> {
                try {
                    method.invoke(interpreter, expected, invokeResponse, result);
                } catch (Exception e) {
                    throw e.getCause() != null ? (Exception) e.getCause() : e;
                }
            });
        }

        @Test
        void defaultModeRejectsExtraFieldsInActual() throws Exception {
            setupPrettifier();
            when(fileSearcher.searchFileToString(anyString(), any())).thenReturn("{\"status\":\"ok\"}");

            final Response expected = new Response();
            expected.setCode(200);
            expected.setFile("expected.json");
            // mode is null, defaults to STRICT

            final InvokeResponse invokeResponse =
                    createInvokeResponse("{\"status\":\"ok\",\"extraField\":\"unexpected\"}");

            final CommandResult result = new CommandResult();
            final Method method = LambdaInterpreter.class.getDeclaredMethod(
                    "compareResult", Response.class, InvokeResponse.class, CommandResult.class);
            method.setAccessible(true);

            assertThrows(Exception.class, () -> {
                try {
                    method.invoke(interpreter, expected, invokeResponse, result);
                } catch (Exception e) {
                    throw e.getCause() != null ? (Exception) e.getCause() : e;
                }
            });
        }

        @Test
        void lenientModeAllowsExtraFieldsInActual() throws Exception {
            setupPrettifier();
            when(fileSearcher.searchFileToString(anyString(), any())).thenReturn("{\"status\":\"ok\"}");

            final Response expected = new Response();
            expected.setCode(200);
            expected.setFile("expected.json");
            expected.setMode(Mode.LENIENT);

            final InvokeResponse invokeResponse =
                    createInvokeResponse("{\"status\":\"ok\",\"extraField\":\"unexpected\"}");

            final CommandResult result = new CommandResult();
            final Method method = LambdaInterpreter.class.getDeclaredMethod(
                    "compareResult", Response.class, InvokeResponse.class, CommandResult.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> method.invoke(interpreter, expected, invokeResponse, result));
        }

        @Test
        void lenientModeRejectsMismatchedFieldValues() throws Exception {
            setupPrettifier();
            when(fileSearcher.searchFileToString(anyString(), any())).thenReturn("{\"status\":\"ok\"}");

            final Response expected = new Response();
            expected.setCode(200);
            expected.setFile("expected.json");
            expected.setMode(Mode.LENIENT);

            final InvokeResponse invokeResponse =
                    createInvokeResponse("{\"status\":\"failed\",\"extraField\":\"unexpected\"}");

            final CommandResult result = new CommandResult();
            final Method method = LambdaInterpreter.class.getDeclaredMethod(
                    "compareResult", Response.class, InvokeResponse.class, CommandResult.class);
            method.setAccessible(true);

            assertThrows(Exception.class, () -> {
                try {
                    method.invoke(interpreter, expected, invokeResponse, result);
                } catch (Exception e) {
                    throw e.getCause() != null ? (Exception) e.getCause() : e;
                }
            });
        }
    }
}
