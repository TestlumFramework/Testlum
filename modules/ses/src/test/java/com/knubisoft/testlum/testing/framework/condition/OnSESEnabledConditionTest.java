package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Ses;
import com.knubisoft.testlum.testing.model.global_config.SesIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnSESEnabledConditionTest {

    private final OnSESEnabledCondition condition = new OnSESEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsPresentWhenSesIntegrationExists() {
            final Ses ses = new Ses();
            ses.setEnabled(true);
            ses.setAlias("ses1");
            final SesIntegration sesIntegration = new SesIntegration();
            sesIntegration.getSes().add(ses);
            final Integrations integrations = new Integrations();
            integrations.setSesIntegration(sesIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertEquals("ses1", result.get().get(0).getAlias());
        }

        @Test
        void returnsEmptyWhenIntegrationsIsEmpty() {
            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenSesIntegrationIsNull() {
            final Integrations integrations = new Integrations();

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyListWhenNoSesEntries() {
            final SesIntegration sesIntegration = new SesIntegration();
            final Integrations integrations = new Integrations();
            integrations.setSesIntegration(sesIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        void returnsMultipleSesEntries() {
            final Ses ses1 = new Ses();
            ses1.setEnabled(true);
            ses1.setAlias("ses-primary");
            final Ses ses2 = new Ses();
            ses2.setEnabled(false);
            ses2.setAlias("ses-secondary");
            final SesIntegration sesIntegration = new SesIntegration();
            sesIntegration.getSes().add(ses1);
            sesIntegration.getSes().add(ses2);
            final Integrations integrations = new Integrations();
            integrations.setSesIntegration(sesIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }

        @Test
        void preservesAliasOrder() {
            final Ses ses1 = new Ses();
            ses1.setAlias("alpha");
            ses1.setEnabled(true);
            final Ses ses2 = new Ses();
            ses2.setAlias("beta");
            ses2.setEnabled(true);
            final SesIntegration sesIntegration = new SesIntegration();
            sesIntegration.getSes().add(ses1);
            sesIntegration.getSes().add(ses2);
            final Integrations integrations = new Integrations();
            integrations.setSesIntegration(sesIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals("alpha", result.get().get(0).getAlias());
            assertEquals("beta", result.get().get(1).getAlias());
        }
    }
}
