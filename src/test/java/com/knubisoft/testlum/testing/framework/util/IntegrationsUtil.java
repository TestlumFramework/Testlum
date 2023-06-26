package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.Dynamo;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import com.knubisoft.testlum.testing.model.global_config.GraphqlIntegration;
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
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import com.knubisoft.testlum.testing.model.global_config.Twilio;
import com.knubisoft.testlum.testing.model.global_config.Websockets;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ALIAS_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.API_NOT_FOUND;

@UtilityClass
public class IntegrationsUtil {

    private static final Function<String, Integrations> TO_INTEGRATIONS =
            i -> GlobalTestConfigurationProvider.getIntegrations().get(i);
    private final Map<IntegrationsPredicate, IntegrationListMethod> configToIntegrationListMap;

    static {
        final Map<IntegrationsPredicate, IntegrationListMethod> map = new HashMap<>(20);
        map.put(c -> c.equals(Api.class), i -> TO_INTEGRATIONS.apply(i).getApis().getApi());
        map.put(c -> c.equals(Websockets.class), i -> TO_INTEGRATIONS.apply(i).getWebsockets().getApi());
        map.put(c -> c.equals(S3.class), i -> TO_INTEGRATIONS.apply(i).getS3Integration().getS3());
        map.put(c -> c.equals(Ses.class), i -> TO_INTEGRATIONS.apply(i).getSesIntegration().getSes());
        map.put(c -> c.equals(Sqs.class), i -> TO_INTEGRATIONS.apply(i).getSqsIntegration().getSqs());
        map.put(c -> c.equals(Smtp.class), i -> TO_INTEGRATIONS.apply(i).getSmtpIntegration().getSmtp());
        map.put(c -> c.equals(Redis.class), i -> TO_INTEGRATIONS.apply(i).getRedisIntegration().getRedis());
        map.put(c -> c.equals(Mongo.class), i -> TO_INTEGRATIONS.apply(i).getMongoIntegration().getMongo());
        map.put(c -> c.equals(Mysql.class), i -> TO_INTEGRATIONS.apply(i).getMysqlIntegration().getMysql());
        map.put(c -> c.equals(Kafka.class), i -> TO_INTEGRATIONS.apply(i).getKafkaIntegration().getKafka());
        map.put(c -> c.equals(GraphqlIntegration.class),
                i -> TO_INTEGRATIONS.apply(i).getGraphqlIntegration().getApi());
        map.put(c -> c.equals(Twilio.class), i -> TO_INTEGRATIONS.apply(i).getTwilioIntegration().getTwilio());
        map.put(c -> c.equals(Oracle.class), i -> TO_INTEGRATIONS.apply(i).getOracleIntegration().getOracle());
        map.put(c -> c.equals(Dynamo.class), i -> TO_INTEGRATIONS.apply(i).getDynamoIntegration().getDynamo());
        map.put(c -> c.equals(Lambda.class), i -> TO_INTEGRATIONS.apply(i).getLambdaIntegration().getLambda());
        map.put(c -> c.equals(Sendgrid.class), i -> TO_INTEGRATIONS.apply(i).getSendgridIntegration().getSendgrid());
        map.put(c -> c.equals(Postgres.class), i -> TO_INTEGRATIONS.apply(i).getPostgresIntegration().getPostgres());
        map.put(c -> c.equals(Rabbitmq.class), i -> TO_INTEGRATIONS.apply(i).getRabbitmqIntegration().getRabbitmq());
        map.put(c -> c.equals(Clickhouse.class),
                i -> TO_INTEGRATIONS.apply(i).getClickhouseIntegration().getClickhouse());
        map.put(c -> c.equals(Elasticsearch.class),
                i -> TO_INTEGRATIONS.apply(i).getElasticsearchIntegration().getElasticsearch());
        configToIntegrationListMap = Collections.unmodifiableMap(map);
    }

    public <T extends Integration> T getIntegrationByClassAndAlias(final Class<T> clazz, final AliasEnv aliasEnv) {
        List<T> intList = getIntegrationsListByEnv(clazz, aliasEnv.getEnvironment());
        return findForAlias(intList, aliasEnv.getAlias());
    }

    @SuppressWarnings("unchecked")
    public <T extends Integration> List<T> getIntegrationsListByEnv(final Class<T> clazz, final String env) {
        IntegrationListMethod integrationListMethod = configToIntegrationListMap.entrySet().stream()
                .filter(e -> e.getKey().test(clazz))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new DefaultFrameworkException(
                        String.format(ExceptionMessage.INTEGRATION_NOT_FOUND, clazz.getSimpleName())));
        return (List<T>) integrationListMethod.apply(env);
    }

    public <T extends Integration> T findApiForAlias(final List<T> apiIntegrations, final String alias) {
        return filterIntegrationByAlias(apiIntegrations, alias, API_NOT_FOUND);
    }

    public <T extends Integration> T findForAlias(final List<T> integrationList, final String alias) {
        return filterIntegrationByAlias(integrationList, alias, ALIAS_NOT_FOUND);
    }

    private <T extends Integration> T filterIntegrationByAlias(final List<T> integrations,
                                                               final String alias,
                                                               final String message) {
        return integrations.stream()
                .filter(Integration::isEnabled)
                .filter(integration -> integration.getAlias().equals(alias))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(message, alias));
    }

    public <T extends Integration> boolean isEnabled(final List<T> integrations) {
        return integrations.stream().anyMatch(Integration::isEnabled);
    }

    private interface IntegrationsPredicate extends Predicate<Class<? extends Integration>> { }
    private interface IntegrationListMethod extends Function<String, List<? extends Integration>> { }
}
