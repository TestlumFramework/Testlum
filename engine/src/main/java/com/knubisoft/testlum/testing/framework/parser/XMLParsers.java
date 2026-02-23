package com.knubisoft.testlum.testing.framework.parser;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.knubisoft.testlum.testing.framework.schema.LSResourceResolverImpl;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.pages.Component;
import com.knubisoft.testlum.testing.model.pages.Page;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class XMLParsers {

    private final Supplier<XMLParser<GlobalTestConfiguration>> globalTestConfigurationXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/global-config.xsd"),
                    GlobalTestConfiguration.class,
                    com.knubisoft.testlum.testing.model.global_config.ObjectFactory.class));

    private final Supplier<XMLParser<Integrations>> integrationsXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/integration-config.xsd"),
                    com.knubisoft.testlum.testing.model.global_config.Integrations.class,
                    com.knubisoft.testlum.testing.model.global_config.ObjectFactory.class));

    private final Supplier<XMLParser<UiConfig>> uiConfigXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/ui-config.xsd"),
                    com.knubisoft.testlum.testing.model.global_config.UiConfig.class,
                    com.knubisoft.testlum.testing.model.global_config.ObjectFactory.class));

    private final Supplier<XMLParser<Page>> pageXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/pages.xsd"),
                    com.knubisoft.testlum.testing.model.pages.Page.class,
                    com.knubisoft.testlum.testing.model.pages.ObjectFactory.class));

    private final Supplier<XMLParser<Component>> componentXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/pages.xsd"),
                    com.knubisoft.testlum.testing.model.pages.Component.class,
                    com.knubisoft.testlum.testing.model.pages.ObjectFactory.class));

    private final Supplier<XMLParser<Scenario>> scenarioXMLParser = Suppliers.
            memoize(() -> new XMLParser<>(initSchema("schema/scenario.xsd"),
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
        File file = getFile(xsd);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new LSResourceResolverImpl(file.getAbsolutePath()));
        return factory.newSchema(file);
    }

    private @NotNull File getFile(final String xsd) throws Exception {
        File file;
        try {
            file = new ClassPathResource(xsd).getFile();
        } catch (Exception e) {
            URL resource = getClass().getResource(xsd);
            if (resource == null) {
                throw new FileNotFoundException(e.getMessage());
            }
            try (FileSystem ignored = getOrCreateFileSystem(resource.toURI())) {
                file = Paths.get(resource.toURI()).toFile();
            }
        }
        return file;
    }

    private FileSystem getOrCreateFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            return FileSystems.newFileSystem(uri, env);
        }
    }
}
