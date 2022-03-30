package com.knubisoft.e2e.testing.framework.schema;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;

@UtilityClass
public class SchemaInitializer {

    public static final Schema GLOBAL_CONFIG_SCHEMA = initSchema("schema/global-config.xsd");
    public static final Schema PAGES_SCHEMA = initSchema("schema/pages.xsd");
    public static final Schema SCENARIO_SCHEMA = initSchema("schema/scenario.xsd");

    @SneakyThrows
    public Schema initSchema(final String path) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new LSResourceResolverImpl(TestResourceSettings.SCHEMAS_FOLDER));
        InputStream xsdFileInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        return factory.newSchema(new StreamSource(xsdFileInputStream));
    }
}
