package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.exception.IntegrationDisabledException;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.Dynamo;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import com.knubisoft.testlum.testing.model.global_config.GraphqlApi;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import com.knubisoft.testlum.testing.model.global_config.Lambda;
import com.knubisoft.testlum.testing.model.global_config.Mongo;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
import com.knubisoft.testlum.testing.model.global_config.Oracle;
import com.knubisoft.testlum.testing.model.global_config.Postgres;
import com.knubisoft.testlum.testing.model.global_config.Rabbitmq;
import com.knubisoft.testlum.testing.model.global_config.Redis;
import com.knubisoft.testlum.testing.model.global_config.S3;
import com.knubisoft.testlum.testing.model.global_config.Sendgrid;
import com.knubisoft.testlum.testing.model.global_config.Ses;
import com.knubisoft.testlum.testing.model.global_config.Smtp;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabase;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import com.knubisoft.testlum.testing.model.global_config.Twilio;
import com.knubisoft.testlum.testing.model.global_config.WebsocketApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@Slf4j
public class IntegrationsUtil {

    private final EnvToIntegrationMap envToIntegrations;
    private final Map<IntegrationsPredicate, IntegrationListMethod> configToIntegrationListMap;

    public IntegrationsUtil(final EnvToIntegrationMap envToIntegrations) {
        this.envToIntegrations = envToIntegrations;
        this.configToIntegrationListMap = createConfigToIntegratonMap();
    }

    private Map<IntegrationsPredicate, IntegrationListMethod> createConfigToIntegratonMap() {
        Map<IntegrationsPredicate, IntegrationListMethod> map = new LinkedHashMap<>();
        registerApiIntegrations(map);
        registerDatabaseIntegrations(map);
        registerMessagingIntegrations(map);
        registerCloudIntegrations(map);
        registerNotificationIntegrations(map);
        return Collections.unmodifiableMap(map);
    }

    private void registerApiIntegrations(final Map<IntegrationsPredicate, IntegrationListMethod> map) {
        map.put(c -> c.equals(Api.class), i -> i.getApis().getApi());
        map.put(c -> c.equals(WebsocketApi.class), i -> i.getWebsockets().getApi());
        map.put(c -> c.equals(GraphqlApi.class), i -> i.getGraphqlIntegration().getApi());
    }

    private void registerDatabaseIntegrations(final Map<IntegrationsPredicate, IntegrationListMethod> map) {
        map.put(c -> c.equals(Postgres.class), i -> i.getPostgresIntegration().getPostgres());
        map.put(c -> c.equals(Mysql.class), i -> i.getMysqlIntegration().getMysql());
        map.put(c -> c.equals(Oracle.class), i -> i.getOracleIntegration().getOracle());
        map.put(c -> c.equals(Clickhouse.class), i -> i.getClickhouseIntegration().getClickhouse());
        map.put(c -> c.equals(SqlDatabase.class), i -> i.getSqlDatabaseIntegration().getSqlDatabase());
        map.put(c -> c.equals(Redis.class), i -> i.getRedisIntegration().getRedis());
        map.put(c -> c.equals(Mongo.class), i -> i.getMongoIntegration().getMongo());
        map.put(c -> c.equals(Dynamo.class), i -> i.getDynamoIntegration().getDynamo());
        map.put(c -> c.equals(Elasticsearch.class), i -> i.getElasticsearchIntegration().getElasticsearch());
    }

    private void registerMessagingIntegrations(final Map<IntegrationsPredicate, IntegrationListMethod> map) {
        map.put(c -> c.equals(Kafka.class), i -> i.getKafkaIntegration().getKafka());
        map.put(c -> c.equals(Rabbitmq.class), i -> i.getRabbitmqIntegration().getRabbitmq());
        map.put(c -> c.equals(Sqs.class), i -> i.getSqsIntegration().getSqs());
    }

    private void registerCloudIntegrations(final Map<IntegrationsPredicate, IntegrationListMethod> map) {
        map.put(c -> c.equals(S3.class), i -> i.getS3Integration().getS3());
        map.put(c -> c.equals(Lambda.class), i -> i.getLambdaIntegration().getLambda());
    }

    private void registerNotificationIntegrations(final Map<IntegrationsPredicate, IntegrationListMethod> map) {
        map.put(c -> c.equals(Ses.class), i -> i.getSesIntegration().getSes());
        map.put(c -> c.equals(Smtp.class), i -> i.getSmtpIntegration().getSmtp());
        map.put(c -> c.equals(Sendgrid.class), i -> i.getSendgridIntegration().getSendgrid());
        map.put(c -> c.equals(Twilio.class), i -> i.getTwilioIntegration().getTwilio());
    }

    public <T extends Integration> T findForAliasEnv(final Class<T> clazz, final AliasEnv aliasEnv) {
        List<T> intList = findListByEnv(clazz, aliasEnv.getEnvironment());
        return findForAlias(intList, aliasEnv.getAlias());
    }

    @SuppressWarnings("unchecked")
    public <T extends Integration> List<T> findListByEnv(final Class<T> clazz, final String env) {
        Integrations integration = envToIntegrations.get(env);
        if (integration == null) {
            return Collections.emptyList();
        }
        IntegrationListMethod integrationListMethod = configToIntegrationListMap.entrySet().stream()
                .filter(e -> e.getKey().test(clazz))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new DefaultFrameworkException(
                        String.format(ExceptionMessage.INTEGRATION_NOT_FOUND, clazz.getSimpleName())));
        return (List<T>) integrationListMethod.apply(integration);
    }

    public <T extends Integration> T findApiForAlias(final List<T> apiIntegrations, final String alias) {
        return getIntegrationByAliasOrThrow(apiIntegrations, alias, ExceptionMessage.API_NOT_FOUND);
    }

    public <T extends Integration> T findForAlias(final List<T> integrationList, final String alias) {
        return getIntegrationByAliasOrThrow(integrationList, alias, ExceptionMessage.ALIAS_NOT_FOUND);
    }

    private <T extends Integration> T getIntegrationByAliasOrThrow(final List<T> integrations,
                                                                   final String alias,
                                                                   final String message) {
        String computedAlias = alias == null ? "DEFAULT" : alias;
        return integrations.stream()
                .filter(Integration::isEnabled)
                .filter(integration -> integration.getAlias().equals(computedAlias))
                .findFirst()
                .orElseThrow(() -> new IntegrationDisabledException(message, computedAlias));
    }

    public <T extends Integration> boolean isEnabled(final List<T> integrations) {
        return integrations.stream().anyMatch(Integration::isEnabled);
    }

    private interface IntegrationsPredicate extends Predicate<Class<? extends Integration>> {
    }

    private interface IntegrationListMethod extends Function<Integrations, List<? extends Integration>> {
    }
}