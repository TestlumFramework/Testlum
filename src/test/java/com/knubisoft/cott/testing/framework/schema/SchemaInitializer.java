package com.knubisoft.cott.testing.framework.schema;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;

import static java.io.File.separator;

@UtilityClass
public class SchemaInitializer {

    public static final Schema GLOBAL_CONFIG_SCHEMA = initSchema("schema" + separator + "global-config.xsd");
    public static final Schema INTEGRATION_SCHEMA = initSchema("schema" + separator + "integration-config.xsd");
    public static final Schema UI_SCHEMA = initSchema("schema" + separator + "ui-config.xsd");
    public static final Schema PAGES_SCHEMA = initSchema("schema" + separator + "pages.xsd");
    public static final Schema SCENARIO_SCHEMA = initSchema("schema" + separator + "scenario.xsd");

    @SneakyThrows
    public Schema initSchema(final String path) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new LSResourceResolverImpl(TestResourceSettings.SCHEMAS_FOLDER));
        InputStream xsdFileInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        return factory.newSchema(new StreamSource(xsdFileInputStream));
    }
}
