package com.knubisoft.testlum.testing.framework.autohealing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;

import java.io.IOException;

public class XmlGenerator {
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    static {
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
        XML_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static String toXml(Object obj) {
        try {
            return XML_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
    }
}
