package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.S3;
import com.knubisoft.testlum.testing.model.global_config.S3Integration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnS3EnabledConditionTest {

    private final OnS3EnabledCondition condition = new OnS3EnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsPresentWhenS3IntegrationExists() {
            final S3 s3 = new S3();
            s3.setEnabled(true);
            s3.setAlias("s3-primary");
            final S3Integration s3Integration = new S3Integration();
            s3Integration.getS3().add(s3);
            final Integrations integrations = new Integrations();
            integrations.setS3Integration(s3Integration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertEquals("s3-primary", result.get().get(0).getAlias());
        }

        @Test
        void returnsEmptyWhenIntegrationsIsEmpty() {
            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenS3IntegrationIsNull() {
            final Integrations integrations = new Integrations();

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyListWhenNoS3Entries() {
            final S3Integration s3Integration = new S3Integration();
            final Integrations integrations = new Integrations();
            integrations.setS3Integration(s3Integration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        void returnsMultipleS3Entries() {
            final S3 s3a = new S3();
            s3a.setEnabled(true);
            s3a.setAlias("bucket-a");
            final S3 s3b = new S3();
            s3b.setEnabled(false);
            s3b.setAlias("bucket-b");
            final S3Integration s3Integration = new S3Integration();
            s3Integration.getS3().add(s3a);
            s3Integration.getS3().add(s3b);
            final Integrations integrations = new Integrations();
            integrations.setS3Integration(s3Integration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }

        @Test
        void preservesEnabledState() {
            final S3 s3 = new S3();
            s3.setEnabled(false);
            s3.setAlias("disabled-s3");
            final S3Integration s3Integration = new S3Integration();
            s3Integration.getS3().add(s3);
            final Integrations integrations = new Integrations();
            integrations.setS3Integration(s3Integration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertFalse(result.get().get(0).isEnabled());
        }
    }
}
