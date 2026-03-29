package com.knubisoft.comparator.alias;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Alias} enum verifying regex pattern matching
 * for all predefined aliases and raw regex fallback.
 */
class AliasTest {

    @Test
    void anyMatchesAnything() {
        final Pattern p = Alias.getPattern("any");
        assertTrue(p.matcher("").matches());
        assertTrue(p.matcher("anything at all\nnewline").matches());
    }

    @ParameterizedTest
    @CsvSource({
            "42, true",
            "-7, true",
            "3.14, true",
            "-0.5, true",
            "abc, false",
            "'', false"
    })
    void digitPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("digit");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches(), "Expected '" + input + "' to match digit");
        } else {
            assertFalse(p.matcher(input).matches(), "Expected '" + input + "' not to match digit");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "0, true",
            "100, true",
            "-50, true",
            "1000.50, true",
            "abc, false"
    })
    void moneyPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("money");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches(), "Expected '" + input + "' to match money");
        } else {
            assertFalse(p.matcher(input).matches(), "Expected '" + input + "' not to match money");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "test@example.com, true",
            "user+tag@domain.org, true",
            "invalid, false",
            "@missing.com, false"
    })
    void emailPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("email");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches(), "Expected '" + input + "' to match email");
        } else {
            assertFalse(p.matcher(input).matches(), "Expected '" + input + "' not to match email");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "192.168.1.1, true",
            "10.0.0.1, true",
            "255.255.255.255, true",
            "999.999.999.999, false",
            "abc, false"
    })
    void ipPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("ip");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches(), "Expected '" + input + "' to match ip");
        } else {
            assertFalse(p.matcher(input).matches(), "Expected '" + input + "' not to match ip");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "550e8400-e29b-41d4-a716-446655440000, true",
            "not-a-uuid, false"
    })
    void uuidPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("uuid");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches(), "Expected '" + input + "' to match uuid");
        } else {
            assertFalse(p.matcher(input).matches(), "Expected '" + input + "' not to match uuid");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "#FF00FF, true",
            "#abc, true",
            "#ZZZZZZ, false",
            "FF00FF, false"
    })
    void colorPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("color");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches(), "Expected '" + input + "' to match color");
        } else {
            assertFalse(p.matcher(input).matches(), "Expected '" + input + "' not to match color");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "2024-01-15, true",
            "2024/01/15, false",
            "15-01-2024, false"
    })
    void ymdDashPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("y-m-d");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches());
        } else {
            assertFalse(p.matcher(input).matches());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "2024/01/15, true",
            "2024-01-15, false"
    })
    void ymdSlashPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("y/m/d");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches());
        } else {
            assertFalse(p.matcher(input).matches());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "15-01-2024, true",
            "2024-01-15, false"
    })
    void dmyDashPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("d-m-y");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches());
        } else {
            assertFalse(p.matcher(input).matches());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "15/01/2024, true",
            "2024/01/15, false"
    })
    void dmySlashPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("d/m/y");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches());
        } else {
            assertFalse(p.matcher(input).matches());
        }
    }

    @Test
    void notEmptyMatchesNonEmptyString() {
        final Pattern p = Alias.getPattern("notEmpty");
        assertTrue(p.matcher("hello").matches());
        assertTrue(p.matcher(" x ").matches());
        assertFalse(p.matcher("").matches());
        assertFalse(p.matcher("   ").matches());
    }

    @ParameterizedTest
    @CsvSource({
            "https://example.com, true",
            "http://example.com/path?q=1, true",
            "not a url, false"
    })
    void urlPattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("url");
        if (shouldMatch) {
            assertTrue(p.matcher(input).find(), "Expected '" + input + "' to match url");
        } else {
            assertFalse(p.matcher(input).find(), "Expected '" + input + "' not to match url");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "2024-01-15 10:30:00, true",
            "not-a-datetime, false"
    })
    void dateTimePattern(final String input, final boolean shouldMatch) {
        final Pattern p = Alias.getPattern("dateTime");
        if (shouldMatch) {
            assertTrue(p.matcher(input).matches(), "Expected '" + input + "' to match dateTime");
        } else {
            assertFalse(p.matcher(input).matches(), "Expected '" + input + "' not to match dateTime");
        }
    }

    @Test
    void unknownAliasFallsBackToRawRegex() {
        final Pattern p = Alias.getPattern("\\d{3}");
        assertNotNull(p);
        assertTrue(p.matcher("123").matches());
        assertFalse(p.matcher("12").matches());
    }
}
