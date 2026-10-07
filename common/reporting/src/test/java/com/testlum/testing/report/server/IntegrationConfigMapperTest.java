package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.config.ApiAuthStrategy;
import com.testlum.reporting.sdk.model.config.ApiIntegrationConfig;
import com.testlum.reporting.sdk.model.config.AwsConnectionConfig;
import com.testlum.reporting.sdk.model.config.DynamoIntegrationConfig;
import com.testlum.reporting.sdk.model.config.HikariConfig;
import com.testlum.reporting.sdk.model.config.IntegrationConfig;
import com.testlum.reporting.sdk.model.config.IntegrationKind;
import com.testlum.reporting.sdk.model.config.PostgresIntegrationConfig;
import com.testlum.reporting.sdk.model.config.SmtpIntegrationConfig;
import com.testlum.reporting.sdk.model.config.SqlDatabaseIntegrationConfig;
import com.testlum.reporting.sdk.model.config.WebsocketIntegrationConfig;
import com.testlum.testing.model.global_config.Api;
import com.testlum.testing.model.global_config.Apis;
import com.testlum.testing.model.global_config.Auth;
import com.testlum.testing.model.global_config.AuthStrategies;
import com.testlum.testing.model.global_config.CustomTruncateConfig;
import com.testlum.testing.model.global_config.Dynamo;
import com.testlum.testing.model.global_config.DynamoIntegration;
import com.testlum.testing.model.global_config.Hikari;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Postgres;
import com.testlum.testing.model.global_config.PostgresIntegration;
import com.testlum.testing.model.global_config.Smtp;
import com.testlum.testing.model.global_config.SmtpIntegration;
import com.testlum.testing.model.global_config.SqlDatabase;
import com.testlum.testing.model.global_config.SqlDatabaseIntegration;
import com.testlum.testing.model.global_config.WebsocketApi;
import com.testlum.testing.model.global_config.WebsocketProtocol;
import com.testlum.testing.model.global_config.Websockets;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link IntegrationConfigMapper} verifying each JAXB integration maps field by field onto its
 * typed SDK subclass, in kind-then-file order.
 */
class IntegrationConfigMapperTest {

    private final IntegrationConfigMapper mapper = new IntegrationConfigMapper();

    private static Postgres postgres(final String alias) {
        Postgres postgres = new Postgres();
        postgres.setAlias(alias);
        postgres.setEnabled(true);
        postgres.setTruncate(true);
        postgres.setJdbcDriver("org.postgresql.Driver");
        postgres.setUsername("app");
        postgres.setPassword("secret");
        postgres.setConnectionUrl("jdbc:postgresql://db/app");
        return postgres;
    }

    private static Hikari hikari() {
        Hikari hikari = new Hikari();
        hikari.setConnectionTimeout(1000);
        hikari.setIdleTimeout(2000);
        hikari.setMaxLifetime(3000);
        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(2);
        hikari.setConnectionTestQuery("SELECT 1");
        hikari.setPoolName("pool");
        hikari.setAutoCommit(true);
        return hikari;
    }

    private static Integrations withPostgres(final Postgres... postgres) {
        PostgresIntegration wrapper = new PostgresIntegration();
        wrapper.getPostgres().addAll(List.of(postgres));
        Integrations integrations = new Integrations();
        integrations.setPostgresIntegration(wrapper);
        return integrations;
    }

    private static Api api(final String alias, final Auth auth) {
        Api api = new Api();
        api.setAlias(alias);
        api.setEnabled(true);
        api.setUrl("https://api.test");
        api.setAuth(auth);
        return api;
    }

    private static Integrations withApis(final Api... api) {
        Apis apis = new Apis();
        apis.getApi().addAll(List.of(api));
        Integrations integrations = new Integrations();
        integrations.setApis(apis);
        return integrations;
    }

    private static Smtp smtp() {
        Smtp smtp = new Smtp();
        smtp.setAlias("mail");
        smtp.setEnabled(true);
        smtp.setHost("smtp.test");
        smtp.setPort(BigInteger.valueOf(587));
        smtp.setSmtpAuth(true);
        return smtp;
    }

    @Test
    void mapsDatabaseWithHikariAndSchema() {
        Postgres source = postgres("main");
        source.setSchema("public");
        source.setHikari(hikari());

        List<IntegrationConfig> configs = mapper.map(withPostgres(source));

        assertEquals(1, configs.size());
        PostgresIntegrationConfig postgres = assertInstanceOf(PostgresIntegrationConfig.class, configs.get(0));
        assertEquals(IntegrationKind.POSTGRES, postgres.getKind());
        assertEquals("main", postgres.getAlias());
        assertTrue(postgres.isEnabled());
        assertTrue(postgres.isTruncate());
        assertEquals("org.postgresql.Driver", postgres.getJdbcDriver());
        assertEquals("app", postgres.getUsername());
        assertEquals("secret", postgres.getPassword());
        assertEquals("jdbc:postgresql://db/app", postgres.getConnectionUrl());
        assertEquals("public", postgres.getSchema());
        assertEquals(HikariConfig.builder().connectionTimeout(1000).idleTimeout(2000).maxLifetime(3000)
                .maximumPoolSize(10).minimumIdle(2).connectionTestQuery("SELECT 1").poolName("pool")
                .autoCommit(true).build(), postgres.getHikari());
    }

    @Test
    void mapsDatabaseWithoutOptionalSchemaAndHikari() {
        PostgresIntegrationConfig postgres =
                (PostgresIntegrationConfig) mapper.map(withPostgres(postgres("main"))).get(0);

        assertNull(postgres.getSchema());
        assertNull(postgres.getHikari());
    }

    @Test
    void mapsSqlDatabaseCustomTruncateFile() {
        SqlDatabase withTruncate = new SqlDatabase();
        withTruncate.setAlias("custom");
        CustomTruncateConfig truncate = new CustomTruncateConfig();
        truncate.setTruncateFile("truncate.sql");
        withTruncate.setCustomTruncate(truncate);
        SqlDatabase withoutTruncate = new SqlDatabase();
        withoutTruncate.setAlias("plain");
        SqlDatabaseIntegration wrapper = new SqlDatabaseIntegration();
        wrapper.getSqlDatabase().addAll(List.of(withTruncate, withoutTruncate));
        Integrations integrations = new Integrations();
        integrations.setSqlDatabaseIntegration(wrapper);

        List<IntegrationConfig> configs = mapper.map(integrations);

        assertEquals("truncate.sql", ((SqlDatabaseIntegrationConfig) configs.get(0)).getCustomTruncateFile());
        assertNull(((SqlDatabaseIntegrationConfig) configs.get(1)).getCustomTruncateFile());
        assertEquals(IntegrationKind.SQL_DATABASE, configs.get(1).getKind());
    }

    @Test
    void mapsAwsConnectionAndDynamoSessionToken() {
        Dynamo source = new Dynamo();
        source.setAlias("dynamo");
        source.setEnabled(true);
        source.setRegion("eu-west-1");
        source.setEndpoint("http://localstack:4566");
        source.setAccessKeyId("key");
        source.setSecretAccessKey("secret");
        source.setSessionToken("session");
        DynamoIntegration wrapper = new DynamoIntegration();
        wrapper.getDynamo().add(source);
        Integrations integrations = new Integrations();
        integrations.setDynamoIntegration(wrapper);

        DynamoIntegrationConfig dynamo = (DynamoIntegrationConfig) mapper.map(integrations).get(0);

        assertEquals(IntegrationKind.DYNAMO, dynamo.getKind());
        assertEquals(AwsConnectionConfig.builder().region("eu-west-1").endpoint("http://localstack:4566")
                .accessKeyId("key").secretAccessKey("secret").build(), dynamo.getAws());
        assertEquals("session", dynamo.getSessionToken());
    }

    @Test
    void mapsApiWithAndWithoutAuth() {
        Auth auth = new Auth();
        auth.setAutoLogout(true);
        auth.setAuthStrategy(AuthStrategies.JWT);
        auth.setTokenName("token");

        List<IntegrationConfig> configs = mapper.map(withApis(api("secured", auth), api("open", null)));

        ApiIntegrationConfig secured = (ApiIntegrationConfig) configs.get(0);
        assertEquals("https://api.test", secured.getUrl());
        assertTrue(secured.getAuth().isAutoLogout());
        assertEquals(ApiAuthStrategy.JWT, secured.getAuth().getAuthStrategy());
        assertEquals("token", secured.getAuth().getTokenName());
        assertNull(secured.getAuth().getAuthCustomClassName());
        assertNull(((ApiIntegrationConfig) configs.get(1)).getAuth());
    }

    @Test
    void mapsWebsocketProtocol() {
        WebsocketApi source = new WebsocketApi();
        source.setAlias("ws");
        source.setUrl("ws://host");
        source.setProtocol(WebsocketProtocol.STOMP);
        Websockets websockets = new Websockets();
        websockets.getApi().add(source);
        Integrations integrations = new Integrations();
        integrations.setWebsockets(websockets);

        WebsocketIntegrationConfig websocket = (WebsocketIntegrationConfig) mapper.map(integrations).get(0);

        assertEquals("ws://host", websocket.getUrl());
        assertEquals(com.testlum.reporting.sdk.model.config.WebsocketProtocol.STOMP, websocket.getProtocol());
    }

    @Test
    void mapsSmtpPortToInteger() {
        SmtpIntegration wrapper = new SmtpIntegration();
        wrapper.getSmtp().add(smtp());
        Integrations integrations = new Integrations();
        integrations.setSmtpIntegration(wrapper);

        SmtpIntegrationConfig smtp = (SmtpIntegrationConfig) mapper.map(integrations).get(0);

        assertEquals("smtp.test", smtp.getHost());
        assertEquals(Integer.valueOf(587), smtp.getPort());
        assertTrue(smtp.isSmtpAuth());
        assertFalse(smtp.isSmtpStarttlsEnable());
    }

    @Test
    void keepsDisabledInstances() {
        Postgres disabled = postgres("off");
        disabled.setEnabled(false);

        List<IntegrationConfig> configs = mapper.map(withPostgres(disabled));

        assertEquals(1, configs.size());
        assertEquals("off", configs.get(0).getAlias());
        assertFalse(configs.get(0).isEnabled());
    }

    @Test
    void ordersByKindThenFileOrder() {
        Integrations integrations = withApis(api("api-1", null), api("api-2", null));
        PostgresIntegration postgres = new PostgresIntegration();
        postgres.getPostgres().addAll(List.of(postgres("pg-2"), postgres("pg-1")));
        integrations.setPostgresIntegration(postgres);
        SmtpIntegration smtp = new SmtpIntegration();
        smtp.getSmtp().add(smtp());
        integrations.setSmtpIntegration(smtp);

        List<String> aliases = mapper.map(integrations).stream().map(IntegrationConfig::getAlias).toList();

        assertEquals(List.of("pg-2", "pg-1", "mail", "api-1", "api-2"), aliases);
    }

    @Test
    void mapsMissingIntegrationsToEmptyList() {
        assertTrue(mapper.map(null).isEmpty());
        assertTrue(mapper.map(new Integrations()).isEmpty());
    }
}
