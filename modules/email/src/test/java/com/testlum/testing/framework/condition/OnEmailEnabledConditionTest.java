package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Email;
import com.testlum.testing.model.global_config.EmailIntegration;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnEmailEnabledConditionTest {

    private final OnEmailEnabledCondition condition = new OnEmailEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsPresentWhenEmailIntegrationExists() {
            final Email email = new Email();
            email.setEnabled(true);
            email.setAlias("email1");
            final EmailIntegration emailIntegration = new EmailIntegration();
            emailIntegration.getEmail().add(email);
            final Integrations integrations = new Integrations();
            integrations.setEmailIntegration(emailIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertEquals("email1", result.get().get(0).getAlias());
        }

        @Test
        void returnsEmptyWhenIntegrationsIsEmpty() {
            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenEmailIntegrationIsNull() {
            final Integrations integrations = new Integrations();

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyListWhenNoEmailEntries() {
            final EmailIntegration emailIntegration = new EmailIntegration();
            final Integrations integrations = new Integrations();
            integrations.setEmailIntegration(emailIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        void returnsMultipleEmailEntries() {
            final Email email1 = new Email();
            email1.setEnabled(true);
            email1.setAlias("email-primary");
            final Email email2 = new Email();
            email2.setEnabled(false);
            email2.setAlias("email-secondary");
            final EmailIntegration emailIntegration = new EmailIntegration();
            emailIntegration.getEmail().add(email1);
            emailIntegration.getEmail().add(email2);
            final Integrations integrations = new Integrations();
            integrations.setEmailIntegration(emailIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }

        @Test
        void returnsCorrectEnabledStatus() {
            final Email enabledEmail = new Email();
            enabledEmail.setEnabled(true);
            enabledEmail.setAlias("enabled");
            final Email disabledEmail = new Email();
            disabledEmail.setEnabled(false);
            disabledEmail.setAlias("disabled");
            final EmailIntegration emailIntegration = new EmailIntegration();
            emailIntegration.getEmail().add(enabledEmail);
            emailIntegration.getEmail().add(disabledEmail);
            final Integrations integrations = new Integrations();
            integrations.setEmailIntegration(emailIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().get(0).isEnabled());
            assertFalse(result.get().get(1).isEnabled());
        }
    }
}
