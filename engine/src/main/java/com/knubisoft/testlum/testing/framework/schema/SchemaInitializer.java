package com.knubisoft.testlum.testing.framework.schema;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

@UtilityClass
public class SchemaInitializer {

    public static final Schema GLOBAL_CONFIG_SCHEMA = initSchema("global-config.xsd");
    public static final Schema INTEGRATION_SCHEMA = initSchema("integration-config.xsd");
    public static final Schema UI_SCHEMA = initSchema("ui-config.xsd");
    public static final Schema PAGES_SCHEMA = initSchema("pages.xsd");
    public static final Schema SCENARIO_SCHEMA = initSchema("scenario.xsd");

    @SneakyThrows
    public Schema initSchema(final String path) {
        String fullPathToResourceFolder = System.getProperty("resource").replace("-p=", "");
        int indexOfLastFolderSeparator = fullPathToResourceFolder.lastIndexOf(File.separator);
        String projectRootDirectoryFullPath;
        String pathToSchemaFolder;
        if (indexOfLastFolderSeparator == -1) {
            pathToSchemaFolder = "schema";
        } else {
             projectRootDirectoryFullPath = fullPathToResourceFolder.substring(0, indexOfLastFolderSeparator);
             pathToSchemaFolder = projectRootDirectoryFullPath + File.separator + "schema";
        }

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new LSResourceResolverImpl(TestResourceSettings.SCHEMAS_FOLDER));
        return factory.newSchema(new File(pathToSchemaFolder + File.separator + path));
    }
}
