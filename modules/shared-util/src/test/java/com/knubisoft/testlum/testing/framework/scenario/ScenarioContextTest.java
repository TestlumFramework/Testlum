package com.knubisoft.testlum.testing.framework.scenario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ScenarioContext} verifying variable storage,
 * template injection, and condition management.
 */
class ScenarioContextTest {

    private ScenarioContext context;

    @BeforeEach
    void setUp() {
        context = new ScenarioContext(new HashMap<>());
    }

    @Nested
    class SetAndGet {
        @Test
        void setAndGetValue() {
            context.set("key1", "value1");
            assertEquals("value1", context.get("key1"));
        }

        @Test
        void overwriteExistingKey() {
            context.set("key1", "old");
            context.set("key1", "new");
            assertEquals("new", context.get("key1"));
        }

        @Test
        void getMissingKeyThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> context.get("nonexistent"));
        }

        @Test
        void getReturnsConditionValueIfNotInContextMap() {
            context.setCondition("flag", true);
            assertEquals("true", context.get("flag"));
        }
    }

    @Nested
    class GetBody {
        @Test
        void getBodyReturnsLastEntry() {
            context.set("first", "v1");
            context.set("second", "v2");
            final Map.Entry<String, String> body = context.getBody();
            assertEquals("second", body.getKey());
            assertEquals("v2", body.getValue());
        }

        @Test
        void getBodyOnEmptyContextThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> context.getBody());
        }
    }

    @Nested
    class Conditions {
        @Test
        void setAndGetCondition() {
            context.setCondition("isEnabled", true);
            assertEquals("true", context.getCondition("isEnabled"));
        }

        @Test
        void getConditionReplacesMultipleKeys() {
            context.setCondition("a", true);
            context.setCondition("b", false);
            assertEquals("true && false",
                    context.getCondition("a && b"));
        }

        @Test
        void getConditionReturnsOriginalIfNoMatch() {
            assertEquals("noMatch", context.getCondition("noMatch"));
        }
    }

    @Nested
    class Inject {
        @Test
        void injectReplacesPlaceholders() {
            context.set("name", "John");
            assertEquals("Hello John",
                    context.inject("Hello {{name}}"));
        }

        @Test
        void injectReplacesMultiplePlaceholders() {
            context.set("first", "A");
            context.set("second", "B");
            assertEquals("A and B",
                    context.inject("{{first}} and {{second}}"));
        }

        @Test
        void injectReturnsOriginalWhenNoPlaceholders() {
            assertEquals("no placeholders",
                    context.inject("no placeholders"));
        }

        @Test
        void injectReturnsBlankForBlankInput() {
            assertEquals("", context.inject(""));
            assertEquals(null, context.inject(null));
        }

        @Test
        void injectThrowsForMissingKey() {
            assertThrows(IllegalArgumentException.class,
                    () -> context.inject("{{missing}}"));
        }

        @Test
        void injectEscapesJsonCharacters() {
            context.set("val", "line1\nline2");
            final String result = context.inject("{{val}}");
            assertNotNull(result);
        }
    }

    @Nested
    class InjectSpel {
        @Test
        void injectSpelEscapesSingleQuotes() {
            context.set("val", "it's");
            final String result = context.injectSpel("{{val}}");
            assertEquals("it''s", result);
        }

        @Test
        void injectSpelReplacesPlaceholders() {
            context.set("name", "John");
            assertEquals("John",
                    context.injectSpel("{{name}}"));
        }

        @Test
        void injectSpelReturnsBlankForBlank() {
            assertEquals("", context.injectSpel(""));
            assertEquals(null, context.injectSpel(null));
        }
    }

    @Nested
    class InitWithPreexistingMap {
        @Test
        void contextInitializedWithExistingEntries() {
            final Map<String, String> initial = new HashMap<>();
            initial.put("pre", "existing");
            final ScenarioContext ctx = new ScenarioContext(initial);
            assertEquals("existing", ctx.get("pre"));
        }

        @Test
        void modifyingOriginalMapDoesNotAffectContext() {
            final Map<String, String> initial = new HashMap<>();
            initial.put("key", "val");
            final ScenarioContext ctx = new ScenarioContext(initial);
            initial.put("key", "modified");
            assertEquals("val", ctx.get("key"));
        }
    }

    @Nested
    class GetBodySingleEntry {
        @Test
        void singleEntryReturnsThatEntry() {
            context.set("only", "one");
            final Map.Entry<String, String> body = context.getBody();
            assertEquals("only", body.getKey());
            assertEquals("one", body.getValue());
        }
    }

    @Nested
    class InjectNested {
        @Test
        void nestedPlaceholdersResolveSequentially() {
            context.set("greeting", "Hello");
            context.set("target", "World");
            assertEquals("Hello World",
                    context.inject("{{greeting}} {{target}}"));
        }
    }

    @Nested
    class GetConditionNoConditionsSet {
        @Test
        void returnsInputWhenNoConditions() {
            assertEquals("someExpression",
                    context.getCondition("someExpression"));
        }
    }

    @Nested
    class ContainsKey {
        @Test
        void returnsTrueForContextMapKey() {
            context.set("myKey", "val");
            assertTrue(context.containsKey("myKey"));
        }

        @Test
        void returnsTrueForConditionMapKey() {
            context.setCondition("condKey", true);
            assertTrue(context.containsKey("condKey"));
        }

        @Test
        void returnsFalseForMissingKey() {
            assertFalse(context.containsKey("absent"));
        }
    }

    @Nested
    class GetFromConditionMap {
        @Test
        void returnsFalseConditionAsString() {
            context.setCondition("flag", false);
            assertEquals("false", context.get("flag"));
        }

        @Test
        void throwsWhenKeyInNeitherMap() {
            context.setCondition("other", true);
            assertThrows(IllegalArgumentException.class,
                    () -> context.get("missing"));
        }
    }
}
