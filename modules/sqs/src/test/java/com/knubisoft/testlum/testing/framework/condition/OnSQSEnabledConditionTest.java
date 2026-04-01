package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import com.knubisoft.testlum.testing.model.global_config.SqsIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnSQSEnabledConditionTest {

    private final OnSQSEnabledCondition condition = new OnSQSEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsPresentWhenSqsIntegrationExists() {
            final Sqs sqs = new Sqs();
            sqs.setEnabled(true);
            sqs.setAlias("sqs1");
            final SqsIntegration sqsIntegration = new SqsIntegration();
            sqsIntegration.getSqs().add(sqs);
            final Integrations integrations = new Integrations();
            integrations.setSqsIntegration(sqsIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertEquals("sqs1", result.get().get(0).getAlias());
        }

        @Test
        void returnsEmptyWhenIntegrationsIsEmpty() {
            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenSqsIntegrationIsNull() {
            final Integrations integrations = new Integrations();

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyListWhenNoSqsEntries() {
            final SqsIntegration sqsIntegration = new SqsIntegration();
            final Integrations integrations = new Integrations();
            integrations.setSqsIntegration(sqsIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        void returnsMultipleSqsEntries() {
            final Sqs sqs1 = new Sqs();
            sqs1.setEnabled(true);
            sqs1.setAlias("sqs-primary");
            final Sqs sqs2 = new Sqs();
            sqs2.setEnabled(false);
            sqs2.setAlias("sqs-secondary");
            final SqsIntegration sqsIntegration = new SqsIntegration();
            sqsIntegration.getSqs().add(sqs1);
            sqsIntegration.getSqs().add(sqs2);
            final Integrations integrations = new Integrations();
            integrations.setSqsIntegration(sqsIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }

        @Test
        void returnsCorrectEnabledStatus() {
            final Sqs enabledSqs = new Sqs();
            enabledSqs.setEnabled(true);
            enabledSqs.setAlias("enabled");
            final Sqs disabledSqs = new Sqs();
            disabledSqs.setEnabled(false);
            disabledSqs.setAlias("disabled");
            final SqsIntegration sqsIntegration = new SqsIntegration();
            sqsIntegration.getSqs().add(enabledSqs);
            sqsIntegration.getSqs().add(disabledSqs);
            final Integrations integrations = new Integrations();
            integrations.setSqsIntegration(sqsIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().get(0).isEnabled());
            assertFalse(result.get().get(1).isEnabled());
        }
    }
}
