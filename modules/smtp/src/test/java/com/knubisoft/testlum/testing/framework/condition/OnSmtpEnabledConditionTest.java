package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Smtp;
import com.knubisoft.testlum.testing.model.global_config.SmtpIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnSmtpEnabledConditionTest {

    private final OnSmtpEnabledCondition condition = new OnSmtpEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsPresentWhenSmtpIntegrationExists() {
            final Smtp smtp = new Smtp();
            smtp.setEnabled(true);
            smtp.setAlias("smtp1");
            final SmtpIntegration smtpIntegration = new SmtpIntegration();
            smtpIntegration.getSmtp().add(smtp);
            final Integrations integrations = new Integrations();
            integrations.setSmtpIntegration(smtpIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertEquals("smtp1", result.get().get(0).getAlias());
        }

        @Test
        void returnsEmptyWhenIntegrationsIsEmpty() {
            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenSmtpIntegrationIsNull() {
            final Integrations integrations = new Integrations();

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyListWhenNoSmtpEntries() {
            final SmtpIntegration smtpIntegration = new SmtpIntegration();
            final Integrations integrations = new Integrations();
            integrations.setSmtpIntegration(smtpIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        void returnsMultipleSmtpEntries() {
            final Smtp smtp1 = new Smtp();
            smtp1.setEnabled(true);
            smtp1.setAlias("smtp-primary");
            final Smtp smtp2 = new Smtp();
            smtp2.setEnabled(false);
            smtp2.setAlias("smtp-secondary");
            final SmtpIntegration smtpIntegration = new SmtpIntegration();
            smtpIntegration.getSmtp().add(smtp1);
            smtpIntegration.getSmtp().add(smtp2);
            final Integrations integrations = new Integrations();
            integrations.setSmtpIntegration(smtpIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }

        @Test
        void returnsCorrectEnabledStatus() {
            final Smtp enabledSmtp = new Smtp();
            enabledSmtp.setEnabled(true);
            enabledSmtp.setAlias("enabled");
            final Smtp disabledSmtp = new Smtp();
            disabledSmtp.setEnabled(false);
            disabledSmtp.setAlias("disabled");
            final SmtpIntegration smtpIntegration = new SmtpIntegration();
            smtpIntegration.getSmtp().add(enabledSmtp);
            smtpIntegration.getSmtp().add(disabledSmtp);
            final Integrations integrations = new Integrations();
            integrations.setSmtpIntegration(smtpIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().get(0).isEnabled());
            assertFalse(result.get().get(1).isEnabled());
        }
    }
}
