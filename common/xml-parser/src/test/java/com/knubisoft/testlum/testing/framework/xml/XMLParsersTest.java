package com.knubisoft.testlum.testing.framework.xml;

import org.junit.jupiter.api.Test;

import javax.xml.validation.Schema;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class XMLParsersTest {

    @Test
    void initSchemaSucceedsWithValidSchema() throws Exception {
        XMLParsers parsers = new XMLParsers();
        Method initSchema = XMLParsers.class.getDeclaredMethod("initSchema", String.class);
        initSchema.setAccessible(true);

        Schema schema = (Schema) initSchema.invoke(parsers, "test-schema/test-item.xsd");
        assertNotNull(schema);
    }

    @Test
    void initSchemaThrowsForMissingSchema() throws Exception {
        XMLParsers parsers = new XMLParsers();
        Method initSchema = XMLParsers.class.getDeclaredMethod("initSchema", String.class);
        initSchema.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> initSchema.invoke(parsers, "nonexistent/schema.xsd"));
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    void getFileURLReturnsUrlForExistingResource() throws Exception {
        XMLParsers parsers = new XMLParsers();
        Method getFileURL = XMLParsers.class.getDeclaredMethod("getFileURL", String.class);
        getFileURL.setAccessible(true);

        URL url = (URL) getFileURL.invoke(parsers, "test-schema/test-item.xsd");
        assertNotNull(url);
    }

    @Test
    void getFileURLThrowsForMissingFile() throws Exception {
        XMLParsers parsers = new XMLParsers();
        Method getFileURL = XMLParsers.class.getDeclaredMethod("getFileURL", String.class);
        getFileURL.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> getFileURL.invoke(parsers, "nonexistent.xsd"));
        assertTrue(exception.getCause() instanceof FileNotFoundException);
    }

    @Test
    void getFileURLErrorMessageContainsFileName() throws Exception {
        XMLParsers parsers = new XMLParsers();
        Method getFileURL = XMLParsers.class.getDeclaredMethod("getFileURL", String.class);
        getFileURL.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> getFileURL.invoke(parsers, "missing-file.xsd"));
        String message = exception.getCause().getMessage();
        assertTrue(message.contains("missing-file.xsd"));
    }

    @Test
    void forGlobalTestConfigurationThrowsWhenSchemaNotOnClasspath() {
        XMLParsers parsers = new XMLParsers();
        RuntimeException exception = assertThrows(RuntimeException.class,
                parsers::forGlobalTestConfiguration);
        assertNotNull(exception.getCause());
    }

    @Test
    void forIntegrationsThrowsWhenSchemaNotOnClasspath() {
        XMLParsers parsers = new XMLParsers();
        RuntimeException exception = assertThrows(RuntimeException.class,
                parsers::forIntegrations);
        assertNotNull(exception.getCause());
    }

    @Test
    void forUiConfigThrowsWhenSchemaNotOnClasspath() {
        XMLParsers parsers = new XMLParsers();
        RuntimeException exception = assertThrows(RuntimeException.class,
                parsers::forUiConfig);
        assertNotNull(exception.getCause());
    }

    @Test
    void forPageLocatorThrowsWhenSchemaNotOnClasspath() {
        XMLParsers parsers = new XMLParsers();
        RuntimeException exception = assertThrows(RuntimeException.class,
                parsers::forPageLocator);
        assertNotNull(exception.getCause());
    }

    @Test
    void forComponentLocatorThrowsWhenSchemaNotOnClasspath() {
        XMLParsers parsers = new XMLParsers();
        RuntimeException exception = assertThrows(RuntimeException.class,
                parsers::forComponentLocator);
        assertNotNull(exception.getCause());
    }

    @Test
    void forScenarioThrowsWhenSchemaNotOnClasspath() {
        XMLParsers parsers = new XMLParsers();
        RuntimeException exception = assertThrows(RuntimeException.class,
                parsers::forScenario);
        assertNotNull(exception.getCause());
    }
}
