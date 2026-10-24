package com.testlum.testing.framework.interpreter;

import com.testlum.testing.model.scenario.AssertJsonPath;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonPathAssertionTest {

    private static final String BODY =
            "{\"id\":7,\"title\":\"Login page\",\"points\":3.5,\"done\":false,\"owner\":null,"
                    + "\"tags\":[\"ui\",\"auth\"],\"team\":{\"lead\":\"Ana\"}}";

    private static AssertJsonPath assertion(final String path, final Boolean exists, final String equals,
                                            final String matches) {
        AssertJsonPath a = new AssertJsonPath();
        a.setPath(path);
        a.setExists(exists);
        a.setEquals(equals);
        a.setMatches(matches);
        return a;
    }

    @Nested
    class Existence {

        @Test
        void fieldMustExistWhenNothingElseIsGiven() {
            assertNull(JsonPathAssertion.check(BODY, assertion("$.title", null, null, null)));
            assertEquals("$.missing: not found",
                    JsonPathAssertion.check(BODY, assertion("$.missing", null, null, null)));
        }

        @Test
        void existsFalseNeedsTheFieldToBeAbsent() {
            assertNull(JsonPathAssertion.check(BODY, assertion("$.missing", false, null, null)));
            assertTrue(JsonPathAssertion.check(BODY, assertion("$.id", false, null, null))
                    .contains("exists with value [7] but should not"));
        }

        @Test
        void existsFalseCannotBeCombinedWithAValueCheck() {
            String expected = "$.missing: exists=false cannot be combined with equals or matches";
            assertEquals(expected, JsonPathAssertion.check(BODY, assertion("$.missing", false, "x", null)));
            assertEquals(expected, JsonPathAssertion.check(BODY, assertion("$.missing", false, null, "x.*")));
        }

        @Test
        void missingFieldWithValueCheckReportsNotFound() {
            assertEquals("$.nope: not found", JsonPathAssertion.check(BODY, assertion("$.nope", null, "x", null)));
        }

        @Test
        void filterThatMatchesNothingCountsAsNotFound() {
            String list = "[{\"a\":\"Ana\"},{\"a\":\"Ben\"}]";
            assertNull(JsonPathAssertion.check(list, assertion("$[?(@.a=='Zed')]", false, null, null)));
            assertEquals("$[?(@.a=='Zed')]: not found",
                    JsonPathAssertion.check(list, assertion("$[?(@.a=='Zed')]", null, null, null)));
            assertNull(JsonPathAssertion.check(list, assertion("$[?(@.a=='Ana')]", true, null, null)));
            assertNull(JsonPathAssertion.check("{\"items\":[]}", assertion("$.items", true, null, null)));
        }
    }

    @Nested
    class Values {

        @Test
        void equalsComparesTheValueAsText() {
            assertNull(JsonPathAssertion.check(BODY, assertion("$.id", null, "7", null)));
            assertNull(JsonPathAssertion.check(BODY, assertion("$.points", null, "3.5", null)));
            assertNull(JsonPathAssertion.check(BODY, assertion("$.done", null, "false", null)));
            assertNull(JsonPathAssertion.check(BODY, assertion("$.owner", null, "null", null)));
            assertNull(JsonPathAssertion.check(BODY, assertion("$.team.lead", null, "Ana", null)));
            assertNull(JsonPathAssertion.check(BODY, assertion("$.tags", null, "[\"ui\",\"auth\"]", null)));
            assertEquals("$.title: expected [Other] but was [Login page]",
                    JsonPathAssertion.check(BODY, assertion("$.title", null, "Other", null)));
        }

        @Test
        void matchesNeedsTheWholeValueToMatch() {
            assertNull(JsonPathAssertion.check(BODY, assertion("$.title", null, null, "Login.*")));
            assertEquals("$.title: [Login page] does not match [Login]",
                    JsonPathAssertion.check(BODY, assertion("$.title", null, null, "Login")));
        }

        @Test
        void everyGivenCheckMustHold() {
            assertNull(JsonPathAssertion.check(BODY, assertion("$.id", true, "7", "\\d+")));
            String failure = JsonPathAssertion.check(BODY, assertion("$.id", true, "8", "x"));
            assertTrue(failure.contains("expected [8] but was [7]") && failure.contains("does not match [x]"), failure);
        }

        @Test
        void invalidRegexIsAFailureNotAnException() {
            assertEquals("$.title: matches is not a valid regex: (",
                    JsonPathAssertion.check(BODY, assertion("$.title", null, null, "(")));
        }
    }

    @Nested
    class ScalarRoots {

        @Test
        void aBodyThatIsOnlyANumberStringBooleanOrNullCanBeReadAtTheRoot() {
            assertNull(JsonPathAssertion.check("42", assertion("$", null, "42", null)));
            assertNull(JsonPathAssertion.check(" true ", assertion("$", null, "true", null)));
            assertNull(JsonPathAssertion.check("\"text\"", assertion("$", null, "text", null)));
            assertNull(JsonPathAssertion.check("null", assertion("$", true, "null", null)));
        }

        @Test
        void aScalarBodyHasNothingBelowTheRoot() {
            assertEquals("$.a: not found", JsonPathAssertion.check("42", assertion("$.a", null, null, null)));
            assertNull(JsonPathAssertion.check("42", assertion("$.a", false, null, null)));
        }

        @Test
        void aScalarThatIsNotValidJsonIsStillReported() {
            assertTrue(JsonPathAssertion.check("42abc", assertion("$", null, null, null)).contains("valid JSON"));
            assertTrue(JsonPathAssertion.check("\"cut", assertion("$", null, null, null)).contains("valid JSON"));
        }
    }

    @Nested
    class BadInput {

        @Test
        void invalidJsonIsReported() {
            assertTrue(JsonPathAssertion.check("not json", assertion("$.a", null, null, null)).contains("valid JSON"));
        }

        @Test
        void noBodyIsReported() {
            assertEquals("$.a: there is no response body to read",
                    JsonPathAssertion.check(null, assertion("$.a", null, null, null)));
        }

        @Test
        void brokenJsonThatLooksLikeJsonIsReported() {
            String m = JsonPathAssertion.check("{\"a\":", assertion("$.a", null, null, null));
            assertTrue(m.startsWith("$.a: the body is not valid JSON") || m.startsWith("$.a: cannot read path"), m);
        }

        @Test
        void malformedPathIsReportedNotThrown() {
            String m = JsonPathAssertion.check(BODY, assertion("$[", null, null, null));
            assertTrue(m.startsWith("$[: cannot read path"), m);
        }

        @Test
        void malformedPathIsAnErrorEvenWhenTheFieldShouldBeAbsent() {
            String m = JsonPathAssertion.check(BODY, assertion("$[", false, null, null));
            assertTrue(m.startsWith("$[: cannot read path"), m);
        }

        @Test
        void textAfterTheFirstValueIsInvalidJson() {
            assertTrue(JsonPathAssertion.check("1,2", assertion("$", null, "1", null)).contains("valid JSON"));
            assertTrue(JsonPathAssertion.check("{\"a\":1} x", assertion("$.a", null, "1", null))
                    .contains("valid JSON"));
        }
    }
}
