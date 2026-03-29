package com.knubisoft.testlum.testing.framework.xml;

import com.knubisoft.testlum.testing.framework.xml.model.ObjectFactory;
import com.knubisoft.testlum.testing.framework.xml.model.TestItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class XMLParserTest {

    private static Schema schema;
    private static XMLParser<TestItem> parser;

    @BeforeAll
    static void setUp() throws Exception {
        URL schemaUrl = XMLParserTest.class.getClassLoader().getResource("test-schema/test-item.xsd");
        assertNotNull(schemaUrl);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = factory.newSchema(schemaUrl);
        parser = new XMLParser<>(schema, TestItem.class, ObjectFactory.class);
    }

    @Test
    void processValidXmlWithoutValidator() {
        File validFile = getResourceFile("test-schema/valid-item.xml");
        TestItem result = parser.process(validFile);
        assertNotNull(result);
        assertEquals("TestName", result.getName());
        assertEquals(42, result.getValue());
    }

    @Test
    void processValidXmlWithValidator() {
        File validFile = getResourceFile("test-schema/valid-item.xml");
        AtomicBoolean validatorCalled = new AtomicBoolean(false);
        XMLValidator<TestItem> validator = (item, file) -> validatorCalled.set(true);

        TestItem result = parser.process(validFile, validator);
        assertNotNull(result);
        assertEquals("TestName", result.getName());
        assertTrue(validatorCalled.get());
    }

    @Test
    void processInvalidXmlThrowsXSDException() {
        File invalidFile = getResourceFile("test-schema/invalid-item.xml");
        assertThrows(XSDException.class, () -> parser.process(invalidFile));
    }

    @Test
    void processInvalidXmlWithValidatorThrowsXSDException() {
        File invalidFile = getResourceFile("test-schema/invalid-item.xml");
        XMLValidator<TestItem> validator = (item, file) -> { };
        assertThrows(XSDException.class, () -> parser.process(invalidFile, validator));
    }

    @Test
    void processMalformedXmlThrowsXSDException() {
        File malformedFile = getResourceFile("test-schema/malformed-item.xml");
        assertThrows(XSDException.class, () -> parser.process(malformedFile));
    }

    @Test
    void processWithValidatorThatThrowsWrapsInRuntimeException() {
        File validFile = getResourceFile("test-schema/valid-item.xml");
        XMLValidator<TestItem> validator = (item, file) -> {
            throw new IllegalStateException("Validation failed");
        };
        assertThrows(IllegalStateException.class, () -> parser.process(validFile, validator));
    }

    @Test
    void processNonExistentFileThrowsRuntimeException() {
        File nonExistent = new File("/nonexistent/file.xml");
        assertThrows(RuntimeException.class, () -> parser.process(nonExistent));
    }

    @Test
    void processNonExistentFileWithValidatorThrowsRuntimeException() {
        File nonExistent = new File("/nonexistent/file.xml");
        XMLValidator<TestItem> validator = (item, file) -> { };
        assertThrows(RuntimeException.class, () -> parser.process(nonExistent, validator));
    }

    private File getResourceFile(final String path) {
        URL url = getClass().getClassLoader().getResource(path);
        assertNotNull(url, "Resource not found: " + path);
        return new File(url.getFile());
    }
}
