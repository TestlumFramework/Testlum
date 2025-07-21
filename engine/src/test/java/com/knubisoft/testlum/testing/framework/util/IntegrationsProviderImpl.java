package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.*;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ALIAS_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.API_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INTEGRATION_NOT_FOUND;

@Component
public class IntegrationsProviderImpl implements IntegrationsProvider {

    @Override
    public <T extends Integration> T findForAliasEnv(final Class<T> clazz, final AliasEnv aliasEnv) {
        return IntegrationsUtil.findForAliasEnv(clazz, aliasEnv);
    }

    @Override
    public <T extends Integration> List<T> findListByEnv(final Class<T> clazz, final String env) {
        return IntegrationsUtil.findListByEnv(clazz, env);
    }

    @Override
    public <T extends Integration> T findApiForAlias(final List<T> apiIntegrations, final String env) {
        return IntegrationsUtil.findApiForAlias(apiIntegrations, env);
    }

    @Override
    public <T extends Integration> T findForAlias(final List<T> integrationList, final String env) {
        return IntegrationsUtil.findForAlias(integrationList, env);
    }

    @Override
    public <T extends Integration> boolean isEnabled(final List<T> integrations) {
        return false;
    }

    @UtilityClass
    public static class IntegrationsUtil {

        private static final Function<String, Integrations> TO_INTEGRATIONS =
                i -> GlobalTestConfigurationProvider.getIntegrations().get(i);
        private static final Map<IntegrationsPredicate, IntegrationListMethod> CONFIG_TO_INTEGRATION_LIST_MAP;

        static {
            CONFIG_TO_INTEGRATION_LIST_MAP = Map.ofEntries(
                    Map.entry(c -> c.equals(Api.class), i -> i.getApis().getApi()),
                    Map.entry(c -> c.equals(WebsocketApi.class), i -> i.getWebsockets().getApi()),
                    Map.entry(c -> c.equals(S3.class), i -> i.getS3Integration().getS3()),
                    Map.entry(c -> c.equals(Ses.class), i -> i.getSesIntegration().getSes()),
                    Map.entry(c -> c.equals(Sqs.class), i -> i.getSqsIntegration().getSqs()),
                    Map.entry(c -> c.equals(Smtp.class), i -> i.getSmtpIntegration().getSmtp()),
                    Map.entry(c -> c.equals(Redis.class), i -> i.getRedisIntegration().getRedis()),
                    Map.entry(c -> c.equals(Mongo.class), i -> i.getMongoIntegration().getMongo()),
                    Map.entry(c -> c.equals(Mysql.class), i -> i.getMysqlIntegration().getMysql()),
                    Map.entry(c -> c.equals(Kafka.class), i -> i.getKafkaIntegration().getKafka()),
                    Map.entry(c -> c.equals(GraphqlApi.class), i -> i.getGraphqlIntegration().getApi()),
                    Map.entry(c -> c.equals(Twilio.class), i -> i.getTwilioIntegration().getTwilio()),
                    Map.entry(c -> c.equals(Oracle.class), i -> i.getOracleIntegration().getOracle()),
                    Map.entry(c -> c.equals(Dynamo.class), i -> i.getDynamoIntegration().getDynamo()),
                    Map.entry(c -> c.equals(Lambda.class), i -> i.getLambdaIntegration().getLambda()),
                    Map.entry(c -> c.equals(Sendgrid.class), i -> i.getSendgridIntegration().getSendgrid()),
                    Map.entry(c -> c.equals(Postgres.class), i -> i.getPostgresIntegration().getPostgres()),
                    Map.entry(c -> c.equals(SqlDatabase.class), i -> i.getSqlDatabaseIntegration().getSqlDatabase()),
                    Map.entry(c -> c.equals(Rabbitmq.class), i -> i.getRabbitmqIntegration().getRabbitmq()),
                    Map.entry(c -> c.equals(Clickhouse.class), i -> i.getClickhouseIntegration().getClickhouse()),
                    Map.entry(c -> c.equals(Elasticsearch.class),
                            i -> i.getElasticsearchIntegration().getElasticsearch()));
        }

        public <T extends Integration> T findForAliasEnv(final Class<T> clazz, final AliasEnv aliasEnv) {
            List<T> intList = findListByEnv(clazz, aliasEnv.getEnvironment());
            return findForAlias(intList, aliasEnv.getAlias());
        }

        @SuppressWarnings("unchecked")
        public <T extends Integration> List<T> findListByEnv(final Class<T> clazz, final String env) {
            IntegrationListMethod integrationListMethod = CONFIG_TO_INTEGRATION_LIST_MAP.entrySet().stream()
                    .filter(e -> e.getKey().test(clazz))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElseThrow(() -> new DefaultFrameworkException(
                            String.format(INTEGRATION_NOT_FOUND, clazz.getSimpleName())));
            return (List<T>) integrationListMethod.apply(TO_INTEGRATIONS.apply(env));
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

        private interface IntegrationListMethod extends Function<Integrations, List<? extends Integration>> { }
    }
}
