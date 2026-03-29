package com.knubisoft.comparator.util;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link Parser} verifying P-bracket expression parsing with alias resolution and raw regex. */
class ParserTest {

    @Test
    void parseSingleAliasBracket() {
        String result = Parser.parseAllPBracketsToRegexp("p(digit)-p(digit)");
        assertNotNull(result);
        Pattern pattern = Pattern.compile(result);
        assertTrue(pattern.matcher("42-7").matches());
        assertFalse(pattern.matcher("abc-def").matches());
    }

    @Test
    void parseWithKnownAlias() {
        String result = Parser.parseAllPBracketsToRegexp("ID-p(uuid)");
        assertNotNull(result);
        Pattern pattern = Pattern.compile(result);
        assertTrue(pattern.matcher("ID-550e8400-e29b-41d4-a716-446655440000").matches());
        assertFalse(pattern.matcher("ID-not-uuid").matches());
    }

    @Test
    void parseWithRawRegex() {
        String result = Parser.parseAllPBracketsToRegexp("prefix-p([A-Z]{3})");
        assertNotNull(result);
        Pattern pattern = Pattern.compile(result);
        assertTrue(pattern.matcher("prefix-ABC").matches());
        assertFalse(pattern.matcher("prefix-abc").matches());
    }

    @Test
    void parseMultipleBrackets() {
        String result = Parser.parseAllPBracketsToRegexp("p(digit):p(digit)");
        assertNotNull(result);
        Pattern pattern = Pattern.compile(result);
        assertTrue(pattern.matcher("42:7").matches());
    }
}
