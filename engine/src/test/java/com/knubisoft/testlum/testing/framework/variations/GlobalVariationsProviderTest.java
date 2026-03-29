package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.parser.CSVParser;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import com.knubisoft.testlum.testing.model.scenario.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GlobalVariationsProvider} verifying CSV parsing,
 * placeholder substitution, and caching behavior.
 */
@ExtendWith(MockitoExtension.class)
class GlobalVariationsProviderTest {

    @Mock
    private VariationsValidator variationsValidator;

    @Mock
    private CSVParser csvParser;

    @InjectMocks
    private GlobalVariationsProvider provider;

    @BeforeEach
    void clearStaticCache() throws Exception {
        final Field variationsField = GlobalVariationsProvider.class.getDeclaredField("VARIATIONS");
        variationsField.setAccessible(true);
        ((Map<?, ?>) variationsField.get(null)).clear();
    }

    @Nested
    class GetValue {
        @Test
        void returnsOriginalWhenBlank() {
            assertEquals("", provider.getValue("", Map.of()));
        }

        @Test
        void returnsNullWhenNull() {
            assertNull(provider.getValue(null, Map.of()));
        }

        @Test
        void replacesPlaceholderWithValue() {
            final Map<String, String> vars = Map.of("name", "Alice");
            final String result = provider.getValue("Hello {{name}}", vars);
            assertEquals("Hello Alice", result);
        }

        @Test
        void replacesMultiplePlaceholders() {
            final Map<String, String> vars = Map.of("first", "A", "second", "B");
            final String result = provider.getValue("{{first}}-{{second}}", vars);
            assertEquals("A-B", result);
        }

        @Test
        void throwsWhenKeyNotFound() {
            final Map<String, String> vars = Map.of("other", "val");
            assertThrows(IllegalArgumentException.class,
                    () -> provider.getValue("{{missing}}", vars));
        }

        @Test
        void returnsStringWithoutPlaceholdersUnchanged() {
            final Map<String, String> vars = Map.of("key", "val");
            assertEquals("no placeholders", provider.getValue("no placeholders", vars));
        }
    }

    @Nested
    class GetValueWithContext {
        @Test
        void returnsBlankWhenBlank() {
            final ScenarioContext ctx = new ScenarioContext(new HashMap<>());
            assertNull(provider.getValue(null, Map.of(), ctx));
        }

        @Test
        void replacesPlaceholderFromMap() {
            final ScenarioContext ctx = new ScenarioContext(new HashMap<>());
            final Map<String, String> vars = Map.of("x", "42");
            assertEquals("val=42", provider.getValue("val={{x}}", vars, ctx));
        }

        @Test
        void skipsContextVariableWhenPresent() {
            final ScenarioContext ctx = new ScenarioContext(new HashMap<>());
            ctx.set("ctxKey", "ctxVal");
            final Map<String, String> vars = new HashMap<>();
            final String result = provider.getValue("{{ctxKey}}", vars, ctx);
            assertEquals("{{ctxKey}}", result);
        }

        @Test
        void throwsWhenKeyNotInMapOrContext() {
            final ScenarioContext ctx = new ScenarioContext(new HashMap<>());
            final Map<String, String> vars = new HashMap<>();
            assertThrows(IllegalArgumentException.class,
                    () -> provider.getValue("{{unknown}}", vars, ctx));
        }
    }

    @Nested
    class ProcessScenario {
        @Test
        void parsesAndCachesVariations() {
            final Scenario scenario = new Scenario();
            final Settings settings = new Settings();
            settings.setVariations("test.csv");
            scenario.setSettings(settings);

            final List<Map<String, String>> parsed = List.of(Map.of("k", "v"));
            when(csvParser.parseVariations("test.csv")).thenReturn(parsed);
            doNothing().when(variationsValidator)
                    .validateByScenario(any(), eq(scenario), any());

            provider.process(scenario, new File("scenario.xml"));

            verify(csvParser).parseVariations("test.csv");
            verify(variationsValidator).validateByScenario(parsed, scenario, new File("scenario.xml"));
        }
    }

    @Nested
    class ProcessFileName {
        @Test
        void parsesNonBlankFileName() {
            final List<Map<String, String>> parsed = List.of(Map.of("a", "1"));
            when(csvParser.parseVariations("data.csv")).thenReturn(parsed);

            provider.process("data.csv");

            verify(csvParser).parseVariations("data.csv");
        }

        @Test
        void skipsBlankFileName() {
            provider.process("");
            provider.process(null);
            provider.process("   ");
        }
    }

    @Nested
    class GetVariations {
        @Test
        void returnsVariationsAfterProcess() {
            final List<Map<String, String>> parsed = List.of(Map.of("col", "val"));
            when(csvParser.parseVariations("file.csv")).thenReturn(parsed);

            provider.process("file.csv");
            final List<Map<String, String>> result = provider.getVariations("file.csv");

            assertEquals(1, result.size());
            assertEquals("val", result.get(0).get("col"));
        }

        @Test
        void throwsWhenVariationsNotCached() {
            assertThrows(DefaultFrameworkException.class,
                    () -> provider.getVariations("nonexistent.csv"));
        }
    }
}
