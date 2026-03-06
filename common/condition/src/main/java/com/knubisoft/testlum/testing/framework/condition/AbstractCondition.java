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
import java.util.Optional;

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
            FileSearcher fileSearcher = conditionContext.getBeanFactory().getBean(FileSearcher.class);
            XMLParsers xmlParsers = conditionContext.getBeanFactory().getBean(XMLParsers.class);
            TestResourceSettings testResourceSettings = conditionContext.getBeanFactory().getBean(TestResourceSettings.class);

            GlobalTestConfiguration globalTestConfiguration =
                    xmlParsers.forGlobalTestConfiguration().process(testResourceSettings.getConfigFile());

            String folder = getCurrentEnv(filterEnabledEnvironments(globalTestConfiguration));

            Optional<Integrations> result = fileSearcher
                    .searchFileFromEnvFolder(folder, TestResourceSettings.INTEGRATION_CONFIG_FILENAME)
                    .map(configFile -> xmlParsers.forIntegrations().process(configFile));

            return result.get();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize default integrations", e);
        }
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

