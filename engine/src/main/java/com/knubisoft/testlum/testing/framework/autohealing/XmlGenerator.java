package com.knubisoft.testlum.testing.framework.autohealing;

import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.databind.SerializationFeature;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;

public class XmlGenerator {
    private static final XmlMapper XML_MAPPER = XmlMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    public static String toXml(final Object obj) {
        try {
            return XML_MAPPER.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
    }
}
