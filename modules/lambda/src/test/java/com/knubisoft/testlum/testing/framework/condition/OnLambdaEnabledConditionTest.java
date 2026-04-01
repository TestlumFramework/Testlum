package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Lambda;
import com.knubisoft.testlum.testing.model.global_config.LambdaIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnLambdaEnabledConditionTest {

    private final OnLambdaEnabledCondition condition = new OnLambdaEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsPresentWhenLambdaIntegrationExists() {
            final Lambda lambda = new Lambda();
            lambda.setEnabled(true);
            lambda.setAlias("lambda1");
            final LambdaIntegration lambdaIntegration = new LambdaIntegration();
            lambdaIntegration.getLambda().add(lambda);
            final Integrations integrations = new Integrations();
            integrations.setLambdaIntegration(lambdaIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertEquals("lambda1", result.get().get(0).getAlias());
        }

        @Test
        void returnsEmptyWhenIntegrationsIsEmpty() {
            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenLambdaIntegrationIsNull() {
            final Integrations integrations = new Integrations();

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyListWhenNoLambdaEntries() {
            final LambdaIntegration lambdaIntegration = new LambdaIntegration();
            final Integrations integrations = new Integrations();
            integrations.setLambdaIntegration(lambdaIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        void returnsMultipleLambdaEntries() {
            final Lambda l1 = new Lambda();
            l1.setEnabled(true);
            l1.setAlias("lambda-primary");
            final Lambda l2 = new Lambda();
            l2.setEnabled(false);
            l2.setAlias("lambda-secondary");
            final LambdaIntegration lambdaIntegration = new LambdaIntegration();
            lambdaIntegration.getLambda().add(l1);
            lambdaIntegration.getLambda().add(l2);
            final Integrations integrations = new Integrations();
            integrations.setLambdaIntegration(lambdaIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }

        @Test
        void returnsCorrectEnabledStatus() {
            final Lambda enabled = new Lambda();
            enabled.setEnabled(true);
            enabled.setAlias("enabled");
            final Lambda disabled = new Lambda();
            disabled.setEnabled(false);
            disabled.setAlias("disabled");
            final LambdaIntegration lambdaIntegration = new LambdaIntegration();
            lambdaIntegration.getLambda().add(enabled);
            lambdaIntegration.getLambda().add(disabled);
            final Integrations integrations = new Integrations();
            integrations.setLambdaIntegration(lambdaIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().get(0).isEnabled());
            assertFalse(result.get().get(1).isEnabled());
        }
    }
}
