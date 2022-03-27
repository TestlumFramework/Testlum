package com.knubisoft.e2e.testing.framework.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaInitializer {

    @SneakyThrows
    public static Schema initWithSources(final String... paths) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source[] sources = new StreamSource[paths.length];
        for (int i = 0; i < paths.length; i++) {
            InputStream xsdFile = requireNonNull(new ClassPathResource(paths[i]).getInputStream());
            sources[i] = new StreamSource(xsdFile);
        }
        return schemaFactory.newSchema(sources);
    }
}
