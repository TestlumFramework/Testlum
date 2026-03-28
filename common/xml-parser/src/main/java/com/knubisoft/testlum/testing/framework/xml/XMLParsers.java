package com.knubisoft.testlum.testing.framework.xml;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.pages.Page;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.FileNotFoundException;
import java.net.URL;

@Component
public class XMLParsers {

    private final Supplier<XMLParser<GlobalTestConfiguration>> globalTestConfigurationXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/global-config.xsd"),
                    GlobalTestConfiguration.class,
                    com.knubisoft.testlum.testing.model.global_config.ObjectFactory.class));

    private final Supplier<XMLParser<Integrations>> integrationsXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/integration-config.xsd"),
                    Integrations.class,
                    com.knubisoft.testlum.testing.model.global_config.ObjectFactory.class));

    private final Supplier<XMLParser<UiConfig>> uiConfigXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/ui-config.xsd"),
                    UiConfig.class,
                    com.knubisoft.testlum.testing.model.global_config.ObjectFactory.class));

    private final Supplier<XMLParser<Page>> pageXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/pages.xsd"),
                    Page.class,
                    com.knubisoft.testlum.testing.model.pages.ObjectFactory.class));

    private final Supplier<XMLParser<com.knubisoft.testlum.testing.model.pages.Component>> componentXMLParser =
            Suppliers.memoize(() -> new XMLParser<>(initSchema("schema/pages.xsd"),
                    com.knubisoft.testlum.testing.model.pages.Component.class,
                    com.knubisoft.testlum.testing.model.pages.ObjectFactory.class));

    private final Supplier<XMLParser<Scenario>> scenarioXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/scenario.xsd"),
                    Scenario.class,
                    com.knubisoft.testlum.testing.model.scenario.ObjectFactory.class));

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

    public XMLParser<com.knubisoft.testlum.testing.model.pages.Component> forComponentLocator() {
        return componentXMLParser.get();
    }

    public XMLParser<Scenario> forScenario() {
        return scenarioXMLParser.get();
    }

    private Schema initSchema(final String xsd) {
        try {
            URL file = getFileURL(xsd);
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setResourceResolver(new LSResourceResolverImpl("schema"));
            return factory.newSchema(file);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private @NotNull URL getFileURL(final String xsd) throws Exception {
        URL file = getClass().getClassLoader().getResource(xsd);
        if (file == null) {
            throw new FileNotFoundException("File not found: " + xsd);
        }
        return file;
    }
}

