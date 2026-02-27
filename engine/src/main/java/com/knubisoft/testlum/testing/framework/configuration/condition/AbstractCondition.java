package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.parser.XMLParsers;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
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

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        Integrations defaultIntegrations = getDefaultIntegrations();
        List<? extends Integration> integration = getIntegrations(defaultIntegrations);
        return integration != null && integration.stream().anyMatch(Integration::isEnabled);
    }

    abstract List<? extends Integration> getIntegrations(Integrations integrations);

    private Integrations getDefaultIntegrations() {
        try {
            FileSearcher fileSearcher = new FileSearcher();
            XMLParsers xmlParsers = new XMLParsers();

            GlobalTestConfiguration globalTestConfiguration =
                    xmlParsers.forGlobalTestConfiguration().process(new TestResourceSettings().getConfigFile());

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
            throw new DefaultFrameworkException(ExceptionMessage.NO_ENABLED_ENVIRONMENTS_FOUND);
        }
        return environments.get(0).getFolder();
    }

    private List<Environment> filterEnabledEnvironments(final GlobalTestConfiguration globalTestConfiguration) {
        return globalTestConfiguration.getEnvironments().getEnv().stream()
                .filter(Environment::isEnabled).toList();
    }

}
