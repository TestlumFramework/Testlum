package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.exception.ComparisonException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Mode;
import com.testlum.testing.model.scenario.S3FileDownload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3InterpreterTest {

    @TempDir
    File tempDir;

    private S3Interpreter interpreter;

    @BeforeEach
    void setUp() {
        final ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(context.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        when(context.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));

        final JacksonService jacksonService = new JacksonService();
        final StringPrettifier stringPrettifier = new StringPrettifier(jacksonService);
        when(context.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(context.getBean(StringPrettifier.class)).thenReturn(stringPrettifier);

        final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
        when(context.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);

        final InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(context)
                .file(new File(tempDir, "scenario.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        interpreter = new S3Interpreter(dependencies);
    }

    @Nested
    class Construction {

        @Test
        void interpreterIsNotNull() {
            assertNotNull(interpreter);
        }
    }

    @Nested
    class CompareMode {

        private static final String EXPECTED_JSON = "{\"id\":1,\"name\":\"test\"}";
        private static final String ACTUAL_JSON_WITH_EXTRA =
                "{\"id\":1,\"name\":\"test\",\"extraField\":\"unexpected\"}";

        private void invokeCompare(final S3FileDownload download) throws Exception {
            final Method method = S3Interpreter.class.getDeclaredMethod(
                    "compare", String.class, String.class, S3FileDownload.class, String.class, CommandResult.class);
            method.setAccessible(true);
            try {
                method.invoke(interpreter, EXPECTED_JSON, ACTUAL_JSON_WITH_EXTRA, download, "test-key",
                        new CommandResult());
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception ex) {
                    throw ex;
                }
                throw e;
            }
        }

        @Test
        void strictModeRejectsExtraFieldInActual() {
            final S3FileDownload download = new S3FileDownload();
            download.setValue(EXPECTED_JSON);
            download.setMode(Mode.STRICT);

            assertThrows(ComparisonException.class, () -> invokeCompare(download));
        }

        @Test
        void lenientModeAcceptsExtraFieldInActual() {
            final S3FileDownload download = new S3FileDownload();
            download.setValue(EXPECTED_JSON);
            download.setMode(Mode.LENIENT);

            assertDoesNotThrow(() -> invokeCompare(download));
        }

        @Test
        void defaultModeBehavesAsStrictAndRejectsExtraFieldInActual() {
            final S3FileDownload download = new S3FileDownload();
            download.setValue(EXPECTED_JSON);
            download.setMode(null);

            assertThrows(ComparisonException.class, () -> invokeCompare(download));
        }
    }
}
