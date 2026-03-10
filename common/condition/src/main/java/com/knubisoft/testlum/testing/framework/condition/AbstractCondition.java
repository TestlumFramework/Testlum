package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
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
import java.util.Objects;
import java.util.Optional;

@Setter
@Getter
public abstract class AbstractCondition<T extends Integration> implements Condition {

    private static final String NO_ENABLED_ENVIRONMENTS_FOUND = "No enabled environments found in configuration file";

    private static volatile Integrations cache;

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        synchronized (AbstractCondition.class) {
            if (cache == null) {
                cache = getDefaultIntegrations(context);
            }
        }
        Optional<Integrations> defaultIntegrations = Optional.ofNullable(cache);
        Optional<List<? extends Integration>> integration = getIntegrations(defaultIntegrations);
        return integration.map(e -> e.stream().anyMatch(Integration::isEnabled)).orElse(false);
    }

    protected abstract Optional<List<? extends Integration>> getIntegrations(Optional<Integrations> integrations);

    private Integrations getDefaultIntegrations(final ConditionContext context) {
        try {
            FileSearcher fileSearcher = getBean(context, FileSearcher.class);
            XMLParsers xmlParsers = getBean(context, XMLParsers.class);
            TestResourceSettings settings = getBean(context, TestResourceSettings.class);
            GlobalTestConfiguration globalCfg = xmlParsers.forGlobalTestConfiguration().
                    process(settings.getConfigFile());
            String envFolder = getCurrentEnv(filterEnabledEnvironments(globalCfg));
            return loadIntegrations(fileSearcher, xmlParsers, envFolder);
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize default integrations", e);
        }
    }

    private <B> B getBean(final ConditionContext context, final Class<B> beanType) {
        return Objects.requireNonNull(context.getBeanFactory()).getBean(beanType);
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

