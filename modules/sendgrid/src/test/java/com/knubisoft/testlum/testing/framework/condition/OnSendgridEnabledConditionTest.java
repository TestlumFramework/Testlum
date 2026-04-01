package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sendgrid;
import com.knubisoft.testlum.testing.model.global_config.SendgridIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnSendgridEnabledConditionTest {

    private final OnSendgridEnabledCondition condition = new OnSendgridEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsPresentWhenSendgridIntegrationExists() {
            final Sendgrid sendgrid = new Sendgrid();
            sendgrid.setEnabled(true);
            sendgrid.setAlias("sg1");
            final SendgridIntegration sendgridIntegration = new SendgridIntegration();
            sendgridIntegration.getSendgrid().add(sendgrid);
            final Integrations integrations = new Integrations();
            integrations.setSendgridIntegration(sendgridIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertEquals("sg1", result.get().get(0).getAlias());
        }

        @Test
        void returnsEmptyWhenIntegrationsIsEmpty() {
            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenSendgridIntegrationIsNull() {
            final Integrations integrations = new Integrations();

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyListWhenNoSendgridEntries() {
            final SendgridIntegration sendgridIntegration = new SendgridIntegration();
            final Integrations integrations = new Integrations();
            integrations.setSendgridIntegration(sendgridIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        void returnsMultipleSendgridEntries() {
            final Sendgrid sg1 = new Sendgrid();
            sg1.setEnabled(true);
            sg1.setAlias("sg-primary");
            final Sendgrid sg2 = new Sendgrid();
            sg2.setEnabled(false);
            sg2.setAlias("sg-secondary");
            final SendgridIntegration sendgridIntegration = new SendgridIntegration();
            sendgridIntegration.getSendgrid().add(sg1);
            sendgridIntegration.getSendgrid().add(sg2);
            final Integrations integrations = new Integrations();
            integrations.setSendgridIntegration(sendgridIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }

        @Test
        void returnsCorrectEnabledStatus() {
            final Sendgrid enabled = new Sendgrid();
            enabled.setEnabled(true);
            enabled.setAlias("enabled");
            final Sendgrid disabled = new Sendgrid();
            disabled.setEnabled(false);
            disabled.setAlias("disabled");
            final SendgridIntegration sendgridIntegration = new SendgridIntegration();
            sendgridIntegration.getSendgrid().add(enabled);
            sendgridIntegration.getSendgrid().add(disabled);
            final Integrations integrations = new Integrations();
            integrations.setSendgridIntegration(sendgridIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().get(0).isEnabled());
            assertFalse(result.get().get(1).isEnabled());
        }
    }
}
