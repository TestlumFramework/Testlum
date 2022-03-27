package com.knubisoft.e2e.testing.framework.parser;

import com.knubisoft.e2e.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.e2e.testing.model.pages.Component;
import com.knubisoft.e2e.testing.model.pages.Page;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.knubisoft.e2e.testing.framework.constant.XSDSchemasPaths.COMPONENT_SCHEMA;
import static com.knubisoft.e2e.testing.framework.constant.XSDSchemasPaths.GLOBAL_CONFIGURATION_SCHEMA;
import static com.knubisoft.e2e.testing.framework.constant.XSDSchemasPaths.LOCATORS_SCHEMA;
import static com.knubisoft.e2e.testing.framework.constant.XSDSchemasPaths.PAGES_SCHEMA;
import static com.knubisoft.e2e.testing.framework.constant.XSDSchemasPaths.SCENARIO_SCHEMA;
import static com.knubisoft.e2e.testing.framework.constant.XSDSchemasPaths.SHARED_CONFIG_SCHEMA;
import static com.knubisoft.e2e.testing.framework.constant.XSDSchemasPaths.SHARED_SCHEMA;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XMLParsers {

    public static XMLParser<GlobalTestConfiguration> forGlobalTestConfiguration() {
        return XMLParserInitializer.createWithSourcesFor(
                com.knubisoft.e2e.testing.model.global_config.GlobalTestConfiguration.class,
                com.knubisoft.e2e.testing.model.global_config.ObjectFactory.class,
                SHARED_CONFIG_SCHEMA,
                GLOBAL_CONFIGURATION_SCHEMA);
    }

    public static XMLParser<Page> forPageLocators() {
        return XMLParserInitializer.createWithSourcesFor(
                com.knubisoft.e2e.testing.model.pages.Page.class,
                com.knubisoft.e2e.testing.model.pages.ObjectFactory.class,
                LOCATORS_SCHEMA,
                COMPONENT_SCHEMA,
                PAGES_SCHEMA);
    }

    public static XMLParser<Component> forComponentLocators() {
        return XMLParserInitializer.createWithSourcesFor(
                com.knubisoft.e2e.testing.model.pages.Component.class,
                com.knubisoft.e2e.testing.model.pages.ObjectFactory.class,
                LOCATORS_SCHEMA,
                COMPONENT_SCHEMA,
                PAGES_SCHEMA);
    }

    public static XMLParser<Scenario> forScenarios() {
        return XMLParserInitializer.createWithSourcesFor(
                com.knubisoft.e2e.testing.model.scenario.Scenario.class,
                com.knubisoft.e2e.testing.model.scenario.ObjectFactory.class,
                SHARED_SCHEMA,
                SCENARIO_SCHEMA);
    }
}
