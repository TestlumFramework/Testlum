package com.knubisoft.comparator;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class XmlErrorHandlerTest {

    private final XmlErrorHandler handler = new XmlErrorHandler();

    @Test
    void warningDoesNotThrow() {
        final SAXParseException ex = new SAXParseException("test warning", null);
        assertDoesNotThrow(() -> handler.warning(ex));
    }

    @Test
    void errorDoesNotThrow() {
        final SAXParseException ex = new SAXParseException("test error", null);
        assertDoesNotThrow(() -> handler.error(ex));
    }

    @Test
    void fatalErrorDoesNotThrow() {
        final SAXParseException ex = new SAXParseException("test fatal", null);
        assertDoesNotThrow(() -> handler.fatalError(ex));
    }
}
