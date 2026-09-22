package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.config.IntegrationConfig;
import com.testlum.reporting.sdk.model.config.IntegrationKind;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Postgres;
import com.testlum.testing.model.global_config.PostgresIntegration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link IntegrationConfigMapper} verifying one entry per configured integration alias.
 */
class IntegrationConfigMapperTest {

    private final IntegrationConfigMapper mapper = new IntegrationConfigMapper(new JacksonService());

    private static Integrations integrationsWithPostgres() {
        Postgres postgres = new Postgres();
        postgres.setAlias("main");
        postgres.setEnabled(true);
        postgres.setConnectionUrl("jdbc:postgresql://db/app");
        postgres.setPassword("secret");
        PostgresIntegration postgresIntegration = new PostgresIntegration();
        postgresIntegration.getPostgres().add(postgres);
        Integrations integrations = new Integrations();
        integrations.setPostgresIntegration(postgresIntegration);
        return integrations;
    }

    @Test
    void mapsEachAliasWithFullBody() {
        List<IntegrationConfig> configs = mapper.map(integrationsWithPostgres());

        assertEquals(1, configs.size());
        IntegrationConfig postgres = configs.get(0);
        assertEquals(IntegrationKind.POSTGRES, postgres.getKind());
        assertEquals("main", postgres.getAlias());
        assertTrue(postgres.isEnabled());
        assertEquals("jdbc:postgresql://db/app", postgres.getBody().get("connectionUrl"));
        assertEquals("secret", postgres.getBody().get("password"));
    }

    @Test
    void mapsMissingIntegrationsToEmptyList() {
        assertTrue(mapper.map(null).isEmpty());
        assertTrue(mapper.map(new Integrations()).isEmpty());
    }
}
