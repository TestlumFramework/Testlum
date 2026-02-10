package com.knubisoft.testlum.testing.framework.parser;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.schema.LSResourceResolverImpl;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.pages.Component;
import com.knubisoft.testlum.testing.model.pages.Page;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class XMLParsers {

    private final Supplier<XMLParser<GlobalTestConfiguration>> globalTestConfigurationXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("global-config.xsd"),
                    GlobalTestConfiguration.class,
                    com.knubisoft.testlum.testing.model.global_config.ObjectFactory.class));

    private final Supplier<XMLParser<Integrations>> integrationsXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("integration-config.xsd"),
                    com.knubisoft.testlum.testing.model.global_config.Integrations.class,
                    com.knubisoft.testlum.testing.model.global_config.ObjectFactory.class));

    private final Supplier<XMLParser<UiConfig>> uiConfigXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("ui-config.xsd"),
                    com.knubisoft.testlum.testing.model.global_config.UiConfig.class,
                    com.knubisoft.testlum.testing.model.global_config.ObjectFactory.class));

    private final Supplier<XMLParser<Page>> pageXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("pages.xsd"),
                    com.knubisoft.testlum.testing.model.pages.Page.class,
                    com.knubisoft.testlum.testing.model.pages.ObjectFactory.class));

    private final Supplier<XMLParser<Component>> componentXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("pages.xsd"),
                    com.knubisoft.testlum.testing.model.pages.Component.class,
                    com.knubisoft.testlum.testing.model.pages.ObjectFactory.class));

    private final Supplier<XMLParser<Scenario>> scenarioXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("scenario.xsd"),
                    com.knubisoft.testlum.testing.model.scenario.Scenario.class,
                    com.knubisoft.testlum.testing.model.scenario.ObjectFactory.class));

    private XMLParsers() {
    }

    private static class Holder {
        private static final XMLParsers INSTANCE = new XMLParsers();
    }

    public static XMLParsers getInstance() {
        return Holder.INSTANCE;
    }

    public XMLParser<GlobalTestConfiguration> forGlobalTestConfiguration() {
        return globalTestConfigurationXMLParser.get();
    }

    public XMLParser<Integrations> forIntegrations() {
        return integrationsXMLParser.get();
    }

    public XMLParser<UiConfig> forUiConfig() {
        return uiConfigXMLParser.get();
    }

    public XMLParser<Page> forPageLocator() {
        return pageXMLParser.get();
    }

    public XMLParser<Component> forComponentLocator() {
        return componentXMLParser.get();
    }

    public XMLParser<Scenario> forScenario() {
        return scenarioXMLParser.get();
    }

    @SneakyThrows
    public Schema initSchema(final String xsd) {
        String pathToSchemaFolder = resolvePathToSchemaFolder();

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new LSResourceResolverImpl(TestResourceSettings.SCHEMAS_FOLDER));
        return factory.newSchema(new File(pathToSchemaFolder + File.separator + xsd));
    }

    private static @NotNull String resolvePathToSchemaFolder() {
        String fullPathToResourceFolder = TestResourceSettings.getInstance().
                getTestResourcesFolder().getAbsolutePath();

        int indexOfLastFolderSeparator = fullPathToResourceFolder.lastIndexOf(File.separator);
        String pathToSchemaFolder;
        if (indexOfLastFolderSeparator == -1) {
            pathToSchemaFolder = "schema";
        } else {
            String projectRootDirectoryFullPath = fullPathToResourceFolder.substring(0, indexOfLastFolderSeparator);
            pathToSchemaFolder = projectRootDirectoryFullPath + File.separator + "schema";
        }
        return pathToSchemaFolder;
    }
}
