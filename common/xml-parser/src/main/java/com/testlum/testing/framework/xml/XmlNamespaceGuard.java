package com.testlum.testing.framework.xml;

import com.testlum.testing.framework.constant.ExceptionMessage;
import jakarta.xml.bind.annotation.XmlSchema;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

final class XmlNamespaceGuard {

    private XmlNamespaceGuard() {
    }

    static void verify(final File file, final Class<?> cls) {
        String expected = expectedNamespaceOf(cls);
        if (expected == null || expected.isEmpty()) {
            return;
        }
        String actual = rootNamespaceOf(file);
        if (actual == null || expected.equals(actual)) {
            return;
        }
        throw new XSDException(file,
                String.format(ExceptionMessage.UNEXPECTED_XML_NAMESPACE, file.getName(), actual, expected),
                null);
    }

    private static String expectedNamespaceOf(final Class<?> cls) {
        Package pkg = cls.getPackage();
        if (pkg == null) {
            return null;
        }
        XmlSchema schema = pkg.getAnnotation(XmlSchema.class);
        return schema == null ? null : schema.namespace();
    }

    private static String rootNamespaceOf(final File file) {
        try (InputStream stream = new FileInputStream(file)) {
            return firstElementNamespace(secureFactory().createXMLStreamReader(stream));
        } catch (XMLStreamException | IOException e) {
            return null;
        }
    }

    private static XMLInputFactory secureFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return factory;
    }

    private static String firstElementNamespace(final XMLStreamReader reader) throws XMLStreamException {
        try {
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    String namespace = reader.getNamespaceURI();
                    return namespace == null ? "" : namespace;
                }
            }
        } finally {
            reader.close();
        }
        return null;
    }
}
