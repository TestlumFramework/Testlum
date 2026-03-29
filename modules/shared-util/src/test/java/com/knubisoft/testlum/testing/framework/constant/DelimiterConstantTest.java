package com.knubisoft.testlum.testing.framework.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DelimiterConstantTest {

    @Test
    void verifyAllConstants() {
        assertEquals("", DelimiterConstant.EMPTY);
        assertEquals(" ", DelimiterConstant.SPACE);
        assertEquals(".", DelimiterConstant.DOT);
        assertEquals("\\.", DelimiterConstant.DOT_REGEX);
        assertEquals("]", DelimiterConstant.CLOSE_SQUARE_BRACKET);
        assertEquals("[", DelimiterConstant.OPEN_SQUARE_BRACKET);
        assertEquals("}", DelimiterConstant.CLOSE_BRACE);
        assertEquals("{", DelimiterConstant.OPEN_BRACE);
        assertEquals("{{", DelimiterConstant.DOUBLE_OPEN_BRACE);
        assertEquals("}}", DelimiterConstant.DOUBLE_CLOSE_BRACE);
        assertEquals("=", DelimiterConstant.EQUALS_MARK);
        assertEquals(";", DelimiterConstant.SEMICOLON);
        assertEquals(":", DelimiterConstant.COLON);
        assertEquals(",", DelimiterConstant.COMMA);
        assertEquals("\\s{2,}", DelimiterConstant.REGEX_MANY_SPACES);
        assertEquals("/", DelimiterConstant.SLASH_SEPARATOR);
        assertEquals("_", DelimiterConstant.UNDERSCORE);
        assertEquals("-", DelimiterConstant.DASH);
        assertEquals(" +", DelimiterConstant.SPACE_WITH_PLUS);
        assertEquals("x", DelimiterConstant.X);
        assertEquals("$", DelimiterConstant.DOLLAR_SIGN);
    }
}
