package com.testlum.testing.framework.xml;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XSDValidatorTest {

    private static Schema schema;
    private static Schema facetSchema;

    @BeforeAll
    static void setUp() {
        schema = schemaOf("test-schema/test-item.xsd");
        facetSchema = schemaOf("test-schema/facet-item.xsd");
    }

    @Test
    void validateBySchemaWithValidXml() {
        File validFile = getResourceFile("test-schema/valid-item.xml");
        assertDoesNotThrow(() -> XSDValidator.validateBySchema(validFile, schema));
    }

    @Test
    void validateBySchemaWithInvalidXmlThrowsXSDException() {
        XSDException exception = validationFailureOf("test-schema/invalid-item.xml", schema);
        assertNotNull(exception.getMessage());
        assertFalse(exception.getIssues().isEmpty());
    }

    @Test
    void validateBySchemaWithMalformedXmlThrowsXSDException() {
        XSDException exception = validationFailureOf("test-schema/malformed-item.xml", schema);
        assertNotNull(exception.getMessage());
    }

    @Test
    void validateBySchemaWithNonExistentFileThrowsXSDException() {
        File nonExistent = new File("/nonexistent/path/file.xml");
        XSDException exception = assertThrows(XSDException.class,
                () -> XSDValidator.validateBySchema(nonExistent, facetSchema));
        assertTrue(exception.getMessage().startsWith("Cannot read file.xml"));
    }

    @Test
    void exceptionCarriesFileAndStructuredIssues() {
        XSDException exception = validationFailureOf("test-schema/facet-item.xml", facetSchema);
        assertTrue(exception.getFile().endsWith("facet-item.xml"));
        assertTrue(exception.getIssues().stream().anyMatch(issue -> "cvc-attribute.3".equals(issue.code())));
    }

    @Test
    void tooShortAttributeIsReportedInPlainWords() {
        String message = validationFailureOf("test-schema/facet-item.xml", facetSchema).getMessage();
        assertTrue(message.contains("<postgres comment=\"Get all\">"), message);
        assertTrue(message.contains("'comment' is too short: 7 characters, minimum is 10"), message);
    }

    /**
     * Xerces reports one mistake twice - the broken facet and the attribute it sits on. Both belong to the same
     * position and must collapse into a single block instead of reading as two unrelated failures.
     */
    @Test
    void facetAndLocatorCollapseIntoOneBlock() {
        String message = validationFailureOf("test-schema/facet-item.xml", facetSchema).getMessage();
        assertEquals(1, countOccurrences(message, "line 3"), message);
        assertFalse(message.contains("cvc-"), message);
    }

    @Test
    void missingRequiredAttributeIsReportedOnOneLine() {
        String message = validationFailureOf("test-schema/facet-missing-attribute.xml", facetSchema).getMessage();
        assertTrue(message.contains("<postgres> is missing required attribute 'comment'"), message);
    }

    @Test
    void enumerationViolationListsAllowedValues() {
        String message = validationFailureOf("test-schema/facet-enum.xml", facetSchema).getMessage();
        assertTrue(message.contains("'strategy' must be one of: locatorId, xpath, id"), message);
    }

    @Test
    void unknownElementIsReportedWithoutNamespaces() {
        String message = validationFailureOf("test-schema/facet-unknown-element.xml", facetSchema).getMessage();
        assertTrue(message.contains("<mysql> cannot be used here"), message);
        assertFalse(message.contains("http://"), message);
    }

    @Test
    void unknownAttributeIsReported() {
        String message = validationFailureOf("test-schema/facet-unknown-attribute.xml", facetSchema).getMessage();
        assertTrue(message.contains("<postgres> has no attribute 'unknownAttr'"), message);
    }

    @Test
    void errorHandlerRecordsSeverityPerCallback() throws Exception {
        List<XsdIssue> issues = new ArrayList<>();
        ErrorHandler handler = createErrorHandler(issues);

        handler.warning(new SAXParseException("warn msg", null, null, 1, 2));
        handler.error(new SAXParseException("error msg", null, null, 5, 10));
        handler.fatalError(new SAXParseException("fatal msg", null, null, 3, 7));

        assertEquals(List.of(XsdSeverity.WARNING, XsdSeverity.ERROR, XsdSeverity.FATAL),
                issues.stream().map(XsdIssue::severity).toList());
        assertEquals(5, issues.get(1).line());
        assertEquals(10, issues.get(1).column());
    }

    /**
     * A warning describes a schema quirk, not a mistake in the document, so on its own it must not make the
     * document invalid.
     */
    @Test
    void warningAloneDoesNotBlockValidation() throws Exception {
        List<XsdIssue> issues = new ArrayList<>();
        createErrorHandler(issues).warning(new SAXParseException("warn msg", null, null, 1, 2));
        assertFalse(issues.get(0).blocksValidation());
    }

    private static XSDException validationFailureOf(final String path, final Schema against) {
        File file = getResourceFile(path);
        return assertThrows(XSDException.class, () -> XSDValidator.validateBySchema(file, against));
    }

    private static int countOccurrences(final String text, final String token) {
        return text.split(java.util.regex.Pattern.quote(token), -1).length - 1;
    }

    @SuppressWarnings("unchecked")
    private ErrorHandler createErrorHandler(final List<XsdIssue> issues) throws Exception {
        Class<?> errorHandlerClass = null;
        for (Class<?> candidate : XSDValidator.class.getDeclaredClasses()) {
            if (ErrorHandler.class.isAssignableFrom(candidate)) {
                errorHandlerClass = candidate;
            }
        }
        assertNotNull(errorHandlerClass);
        Constructor<?> constructor = errorHandlerClass.getDeclaredConstructor(List.class);
        constructor.setAccessible(true);
        return (ErrorHandler) constructor.newInstance(issues);
    }

    private static Schema schemaOf(final String path) {
        URL schemaUrl = XSDValidatorTest.class.getClassLoader().getResource(path);
        assertNotNull(schemaUrl, "Schema not found: " + path);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return assertDoesNotThrow(() -> factory.newSchema(schemaUrl));
    }

    private static File getResourceFile(final String path) {
        URL url = XSDValidatorTest.class.getClassLoader().getResource(path);
        assertNotNull(url, "Resource not found: " + path);
        return new File(url.getFile());
    }
}
