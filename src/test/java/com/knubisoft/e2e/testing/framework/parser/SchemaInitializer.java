package com.knubisoft.e2e.testing.framework.parser;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.net.URL;

import static java.util.Objects.requireNonNull;

@Slf4j
@UtilityClass
public class SchemaInitializer {

    public static final Schema SCHEMA_GLOBAL_CFG = init("schema/global-config.xsd");
    public static final Schema SCHEMA_PAGES = init("schema/pages.xsd");
    public static final Schema SCHEMA_SCENARIOS = init("schema/scenario.xsd");

    @SneakyThrows
    private static Schema init(final String path) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL url = requireNonNull(Thread.currentThread().getContextClassLoader().getResource(path));
        File xsdFile = new File(url.getPath());
        return factory.newSchema(xsdFile);
    }
}
