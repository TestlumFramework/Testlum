package com.knubisoft.cott.testing.framework.parser;

import com.knubisoft.cott.testing.framework.schema.SchemaInitializer;
import com.knubisoft.cott.testing.model.global_config.Environment;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.cott.testing.model.pages.Component;
import com.knubisoft.cott.testing.model.pages.Page;
import com.knubisoft.cott.testing.model.scenario.Scenario;
import lombok.experimental.UtilityClass;

@UtilityClass
public class XMLParsers {

    public XMLParser<GlobalTestConfiguration> forGlobalTestConfiguration() {
        return new XMLParser<>(SchemaInitializer.GLOBAL_CONFIG_SCHEMA,
                com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration.class,
                com.knubisoft.cott.testing.model.global_config.ObjectFactory.class);
    }

    public XMLParser<Environment> forEnvironmentConfiguration() {
        return new XMLParser<>(SchemaInitializer.ENVIRONMENT_SCHEMA,
                com.knubisoft.cott.testing.model.global_config.Environment.class,
                com.knubisoft.cott.testing.model.global_config.ObjectFactory.class);
    }

    public XMLParser<Page> forPageLocator() {
        return new XMLParser<>(SchemaInitializer.PAGES_SCHEMA,
                com.knubisoft.cott.testing.model.pages.Page.class,
                com.knubisoft.cott.testing.model.pages.ObjectFactory.class);
    }

    public XMLParser<Component> forComponentLocator() {
        return new XMLParser<>(SchemaInitializer.PAGES_SCHEMA,
                com.knubisoft.cott.testing.model.pages.Component.class,
                com.knubisoft.cott.testing.model.pages.ObjectFactory.class);
    }

    public XMLParser<Scenario> forScenario() {
        return new XMLParser<>(SchemaInitializer.SCENARIO_SCHEMA,
                com.knubisoft.cott.testing.model.scenario.Scenario.class,
                com.knubisoft.cott.testing.model.scenario.ObjectFactory.class);
    }
}
