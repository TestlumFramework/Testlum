package com.knubisoft.testlum.testing.framework.xml;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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

import static org.junit.jupiter.api.Assertions.*;

class XSDValidatorTest {

    private static Schema schema;

    @BeforeAll
    static void setUp() throws Exception {
        URL schemaUrl = XSDValidatorTest.class.getClassLoader().getResource("test-schema/test-item.xsd");
        assertNotNull(schemaUrl);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = factory.newSchema(schemaUrl);
    }

    @Test
    void canBeInstantiated() {
        XSDValidator validator = new XSDValidator();
        assertNotNull(validator);
    }

    @Test
    void validateBySchemaWithValidXml() {
        File validFile = getResourceFile("test-schema/valid-item.xml");
        assertDoesNotThrow(() -> XSDValidator.validateBySchema(validFile, schema));
    }

    @Test
    void validateBySchemaWithInvalidXmlThrowsXSDException() {
        File invalidFile = getResourceFile("test-schema/invalid-item.xml");
        XSDException exception = assertThrows(XSDException.class,
                () -> XSDValidator.validateBySchema(invalidFile, schema));
        assertNotNull(exception.getMessage());
    }

    @Test
    void validateBySchemaWithMalformedXmlThrowsXSDException() {
        File malformedFile = getResourceFile("test-schema/malformed-item.xml");
        XSDException exception = assertThrows(XSDException.class,
                () -> XSDValidator.validateBySchema(malformedFile, schema));
        assertNotNull(exception.getMessage());
    }

    @Test
    void validateBySchemaWithNonExistentFileThrowsRuntimeException() {
        File nonExistent = new File("/nonexistent/path/file.xml");
        assertThrows(RuntimeException.class,
                () -> XSDValidator.validateBySchema(nonExistent, schema));
    }

    @Test
    void xsdExceptionMessageContainsErrorDetails() {
        File invalidFile = getResourceFile("test-schema/invalid-item.xml");
        XSDException exception = assertThrows(XSDException.class,
                () -> XSDValidator.validateBySchema(invalidFile, schema));
        String message = exception.getMessage();
        assertTrue(message.contains("ERROR") || message.contains("FATAL_ERROR"));
    }

    @Test
    void errorHandlerWarningCollectsIssue() throws Exception {
        Multimap<String, XSDException.XSDIssue> errors = ArrayListMultimap.create();
        File file = new File("/test.xml");
        ErrorHandler handler = createErrorHandler(errors, file);
        SAXParseException saxException = new SAXParseException("warn msg", null, null, 1, 2);

        handler.warning(saxException);

        assertTrue(errors.containsKey("WARNING"));
        XSDException.XSDIssue issue = errors.get("WARNING").iterator().next();
        assertTrue(issue.message.contains("warn msg"));
    }

    @Test
    void errorHandlerErrorCollectsIssue() throws Exception {
        Multimap<String, XSDException.XSDIssue> errors = ArrayListMultimap.create();
        File file = new File("/test.xml");
        ErrorHandler handler = createErrorHandler(errors, file);
        SAXParseException saxException = new SAXParseException("error msg", null, null, 5, 10);

        handler.error(saxException);

        assertTrue(errors.containsKey("ERROR"));
    }

    @Test
    void errorHandlerFatalErrorCollectsIssue() throws Exception {
        Multimap<String, XSDException.XSDIssue> errors = ArrayListMultimap.create();
        File file = new File("/test.xml");
        ErrorHandler handler = createErrorHandler(errors, file);
        SAXParseException saxException = new SAXParseException("fatal msg", null, null, 3, 7);

        handler.fatalError(saxException);

        assertTrue(errors.containsKey("FATAL_ERROR"));
    }

    @SuppressWarnings("unchecked")
    private ErrorHandler createErrorHandler(final Multimap<String, XSDException.XSDIssue> errors,
                                            final File file) throws Exception {
        Class<?>[] innerClasses = XSDValidator.class.getDeclaredClasses();
        Class<?> errorHandlerClass = null;
        for (Class<?> c : innerClasses) {
            if (ErrorHandler.class.isAssignableFrom(c)) {
                errorHandlerClass = c;
                break;
            }
        }
        assertNotNull(errorHandlerClass);
        Constructor<?> constructor = errorHandlerClass.getDeclaredConstructor(Multimap.class, File.class);
        constructor.setAccessible(true);
        return (ErrorHandler) constructor.newInstance(errors, file);
    }

    private File getResourceFile(final String path) {
        URL url = getClass().getClassLoader().getResource(path);
        assertNotNull(url, "Resource not found: " + path);
        return new File(url.getFile());
    }
}
