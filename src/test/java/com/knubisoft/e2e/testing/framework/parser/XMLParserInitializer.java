package com.knubisoft.e2e.testing.framework.parser;

import lombok.experimental.UtilityClass;
import javax.xml.validation.Schema;

@UtilityClass
public class XMLParserInitializer {

    public <T> XMLParser<T> createWithSourcesFor(final Class<T> objectClass,
                                                 final Class<?> objectFactoryClass,
                                                 final String... schemaPaths) {
        Schema schema = SchemaInitializer.initWithSources(schemaPaths);
        return new XMLParser<>(schema, objectClass, objectFactoryClass);
    }
}
