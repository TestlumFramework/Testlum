package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.xml.XMLParser;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.Environments;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConditionBaseTest {

    private ConditionContext conditionContext;
    private AnnotatedTypeMetadata metadata;

    @BeforeEach
    void setUp() throws Exception {
        Field cacheField = AbstractCondition.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        cacheField.set(null, null);

        conditionContext = mock(ConditionContext.class);
        metadata = mock(AnnotatedTypeMetadata.class);
    }

    @Nested
    class Matches {
        @Test
        void returnsTrueWhenIntegrationIsEnabled() {
            Integrations integrations = mock(Integrations.class);
            setupContextWithIntegrations(integrations);

            TestCondition condition = new TestCondition(true);
            boolean result = condition.matches(conditionContext, metadata);

            assertTrue(result);
        }

        @Test
        void returnsFalseWhenIntegrationIsDisabled() {
            Integrations integrations = mock(Integrations.class);
            setupContextWithIntegrations(integrations);

            TestCondition condition = new TestCondition(false);
            boolean result = condition.matches(conditionContext, metadata);

            assertFalse(result);
        }

        @Test
        void returnsFalseWhenIntegrationListIsEmpty() {
            Integrations integrations = mock(Integrations.class);
            setupContextWithIntegrations(integrations);

            EmptyCondition condition = new EmptyCondition();
            boolean result = condition.matches(conditionContext, metadata);

            assertFalse(result);
        }

        @Test
        void returnsFalseWhenIntegrationsAreAbsent() {
            Integrations integrations = mock(Integrations.class);
            setupContextWithIntegrations(integrations);

            AbsentCondition condition = new AbsentCondition();
            boolean result = condition.matches(conditionContext, metadata);

            assertFalse(result);
        }

        @Test
        void cachesIntegrationsAcrossMultipleCalls() {
            Integrations integrations = mock(Integrations.class);
            setupContextWithIntegrations(integrations);

            TestCondition condition1 = new TestCondition(true);
            TestCondition condition2 = new TestCondition(true);

            condition1.matches(conditionContext, metadata);
            condition2.matches(conditionContext, metadata);

            verify(conditionContext.getBeanFactory(), atMost(1))
                    .getBean(FileSearcher.class);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupContextWithIntegrations(final Integrations integrations) {
        ConfigurableListableBeanFactory beanFactory =
                mock(ConfigurableListableBeanFactory.class);
        FileSearcher fileSearcher = mock(FileSearcher.class);
        XMLParsers xmlParsers = mock(XMLParsers.class);
        TestResourceSettings settings = mock(TestResourceSettings.class);
        GlobalTestConfiguration globalConfig =
                mock(GlobalTestConfiguration.class);
        Environments environments = mock(Environments.class);
        Environment environment = mock(Environment.class);

        when(conditionContext.getBeanFactory()).thenReturn(beanFactory);
        when(beanFactory.getBean(FileSearcher.class)).thenReturn(fileSearcher);
        when(beanFactory.getBean(XMLParsers.class)).thenReturn(xmlParsers);
        when(beanFactory.getBean(TestResourceSettings.class))
                .thenReturn(settings);
        when(settings.getConfigFile()).thenReturn(new File("config.xml"));

        XMLParser<GlobalTestConfiguration> configParser =
                mock(XMLParser.class);
        when(xmlParsers.forGlobalTestConfiguration()).thenReturn(configParser);
        when(configParser.process(any(File.class))).thenReturn(globalConfig);
        when(globalConfig.getEnvironments()).thenReturn(environments);
        when(environment.isEnabled()).thenReturn(true);
        when(environment.getFolder()).thenReturn("env1");
        when(environments.getEnv()).thenReturn(List.of(environment));

        XMLParser<Integrations> intParser =
                mock(XMLParser.class);
        when(xmlParsers.forIntegrations()).thenReturn(intParser);
        String configFilename =
                TestResourceSettings.INTEGRATION_CONFIG_FILENAME;
        when(fileSearcher.searchFileFromEnvFolder(eq("env1"),
                eq(configFilename)))
                .thenReturn(Optional.of(new File("integrations.xml")));
        when(intParser.process(any(File.class))).thenReturn(integrations);
    }

    private static class TestIntegration extends Integration {
        TestIntegration(final boolean isEnabled) {
            this.enabled = isEnabled;
        }
    }

    private static class TestCondition
            extends AbstractCondition<TestIntegration> {
        private final boolean enabled;

        TestCondition(final boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        protected Optional<List<? extends Integration>> getIntegrations(
                final Optional<Integrations> integrations) {
            TestIntegration integration = new TestIntegration(enabled);
            return Optional.of(List.of(integration));
        }
    }

    private static class EmptyCondition
            extends AbstractCondition<TestIntegration> {
        @Override
        protected Optional<List<? extends Integration>> getIntegrations(
                final Optional<Integrations> integrations) {
            return Optional.of(List.of());
        }
    }

    private static class AbsentCondition
            extends AbstractCondition<TestIntegration> {
        @Override
        protected Optional<List<? extends Integration>> getIntegrations(
                final Optional<Integrations> integrations) {
            return Optional.empty();
        }
    }
}
