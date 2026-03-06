package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.List;

@Setter
@Getter
public abstract class AbstractCondition<T extends Integration> implements Condition {

    private static final String NO_ENABLED_ENVIRONMENTS_FOUND = "No enabled environments found in configuration file";

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        Integrations defaultIntegrations = getDefaultIntegrations(context);
        List<? extends Integration> integration = getIntegrations(defaultIntegrations);
        return integration != null && integration.stream().anyMatch(Integration::isEnabled);
    }

    abstract List<? extends Integration> getIntegrations(Integrations integrations);

    private Integrations getDefaultIntegrations(final ConditionContext conditionContext) {
        try {
            FileSearcher fileSearcher = this.getBean(conditionContext, FileSearcher.class);
            XMLParsers xmlParsers = this.getBean(conditionContext, XMLParsers.class);
            TestResourceSettings testResourceSettings = this.getBean(conditionContext, TestResourceSettings.class);

            GlobalTestConfiguration globalTestConfiguration =
                    this.loadGlobalConfiguration(xmlParsers, testResourceSettings);

            String envFolder = getCurrentEnv(filterEnabledEnvironments(globalTestConfiguration));

            return loadIntegrations(fileSearcher, xmlParsers, envFolder);
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize default integrations", e);
        }
    }

    private <B> B getBean(final ConditionContext conditionContext, final Class<B> beanType) {
        return conditionContext.getBeanFactory().getBean(beanType);
    }

    private GlobalTestConfiguration loadGlobalConfiguration(final XMLParsers xmlParsers,
                                                            final TestResourceSettings settings) {

        return xmlParsers
                .forGlobalTestConfiguration()
                .process(settings.getConfigFile());
    }

    private Integrations loadIntegrations(final FileSearcher fileSearcher,
                                          final XMLParsers xmlParsers,
                                          final String folder) {

        return fileSearcher
                .searchFileFromEnvFolder(folder, TestResourceSettings.INTEGRATION_CONFIG_FILENAME)
                .map(file -> xmlParsers.forIntegrations().process(file))
                .orElseThrow(() -> new IllegalStateException("Integrations config file not found"));
    }

    private String getCurrentEnv(final List<Environment> environments) {
        return getDefaultEnabledEnvironment(environments);
    }

    private String getDefaultEnabledEnvironment(final List<Environment> environments) {
        if (environments.isEmpty()) {
            throw new DefaultFrameworkException(NO_ENABLED_ENVIRONMENTS_FOUND);
        }
        return environments.get(0).getFolder();
    }

    private List<Environment> filterEnabledEnvironments(final GlobalTestConfiguration globalTestConfiguration) {
        return globalTestConfiguration.getEnvironments().getEnv().stream()
                .filter(Environment::isEnabled).toList();
    }

}

