package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Auth;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.DatabaseConfig;
import com.knubisoft.testlum.testing.model.global_config.Dynamo;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import com.knubisoft.testlum.testing.model.global_config.Lambda;
import com.knubisoft.testlum.testing.model.global_config.Mongo;
import com.knubisoft.testlum.testing.model.global_config.Rabbitmq;
import com.knubisoft.testlum.testing.model.global_config.Redis;
import com.knubisoft.testlum.testing.model.global_config.S3;
import com.knubisoft.testlum.testing.model.global_config.Sendgrid;
import com.knubisoft.testlum.testing.model.global_config.Ses;
import com.knubisoft.testlum.testing.model.global_config.Sqs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings.INTEGRATION_CONFIG_FILENAME;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.AUTH_CUSTOM_CLASS_NAME_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.AUTH_LOGOUT_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.AUTH_NOT_PRESENT_IN_ALL_ENVS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.AUTH_STRATEGY_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INTEGRATIONS_MISMATCH_ENVS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INTEGRATION_ALIAS_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_INTEGRATION_ALIAS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_PORT_OR_ENDPOINT;
import static java.util.Objects.nonNull;

public class IntegrationsValidator {

    private static final Map<IntegrationsPredicate, IntegrationListMethod> INTEGRATIONS_TO_LISTS_MAP;
    private static final Map<IntegrationPredicate, Function<Integration, ?>> INTEGRATIONS_LISTS;
    private static final String URL = "url";
    private static final String PORT = "port";
    private static final String API_KEY = "api key";
    private static final String ENDPOINT = "endpoint";

    static {
        final Map<IntegrationsPredicate, IntegrationListMethod> map1 = new HashMap<>(20);
        map1.put(i -> nonNull(i.getApis()), i -> i.getApis().getApi());
        map1.put(i -> nonNull(i.getWebsockets()), i -> i.getWebsockets().getApi());
        map1.put(i -> nonNull(i.getS3Integration()), i -> i.getS3Integration().getS3());
        map1.put(i -> nonNull(i.getSesIntegration()), i -> i.getSesIntegration().getSes());
        map1.put(i -> nonNull(i.getSqsIntegration()), i -> i.getSqsIntegration().getSqs());
        map1.put(i -> nonNull(i.getSmtpIntegration()), i -> i.getSmtpIntegration().getSmtp());
        map1.put(i -> nonNull(i.getRedisIntegration()), i -> i.getRedisIntegration().getRedis());
        map1.put(i -> nonNull(i.getMongoIntegration()), i -> i.getMongoIntegration().getMongo());
        map1.put(i -> nonNull(i.getMysqlIntegration()), i -> i.getMysqlIntegration().getMysql());
        map1.put(i -> nonNull(i.getKafkaIntegration()), i -> i.getKafkaIntegration().getKafka());
        map1.put(i -> nonNull(i.getGraphqlIntegration()), i -> i.getGraphqlIntegration().getApi());
        map1.put(i -> nonNull(i.getTwilioIntegration()), i -> i.getTwilioIntegration().getTwilio());
        map1.put(i -> nonNull(i.getOracleIntegration()), i -> i.getOracleIntegration().getOracle());
        map1.put(i -> nonNull(i.getDynamoIntegration()), i -> i.getDynamoIntegration().getDynamo());
        map1.put(i -> nonNull(i.getLambdaIntegration()), i -> i.getLambdaIntegration().getLambda());
        map1.put(i -> nonNull(i.getSendgridIntegration()), i -> i.getSendgridIntegration().getSendgrid());
        map1.put(i -> nonNull(i.getPostgresIntegration()), i -> i.getPostgresIntegration().getPostgres());
        map1.put(i -> nonNull(i.getRabbitmqIntegration()), i -> i.getRabbitmqIntegration().getRabbitmq());
        map1.put(i -> nonNull(i.getClickhouseIntegration()), i -> i.getClickhouseIntegration().getClickhouse());
        map1.put(i -> nonNull(i.getElasticsearchIntegration()), i -> i.getElasticsearchIntegration().getElasticsearch());
        INTEGRATIONS_TO_LISTS_MAP = Collections.unmodifiableMap(map1);

        final Map<IntegrationPredicate, Function<Integration, ?>> map2 = new HashMap<>();
        map2.put(i -> i instanceof DatabaseConfig, integration -> integration);
        map2.put(i -> i instanceof Clickhouse, integration -> integration);
        map2.put(i -> i instanceof Redis, integration -> integration);
        map2.put(i -> i instanceof Mongo, integration -> integration);
        map2.put(i -> i instanceof S3, integration -> integration);
        map2.put(i -> i instanceof Sqs, integration -> integration);
        map2.put(i -> i instanceof Kafka, integration -> integration);
        map2.put(i -> i instanceof Rabbitmq, integration -> integration);
        map2.put(i -> i instanceof Dynamo, integration -> integration);
        map2.put(i -> i instanceof Elasticsearch, integration -> integration);
        map2.put(i -> i instanceof Lambda, integration -> integration);
        map2.put(i -> i instanceof Ses, integration -> integration);
        map2.put(i -> i instanceof Sendgrid, integration -> integration);
        INTEGRATIONS_LISTS = Collections.unmodifiableMap(map2);
    }

    public void validateIntegrations(final Map<String, Integrations> integrationsMap) {
        for (Map.Entry<IntegrationsPredicate, IntegrationListMethod> entry : INTEGRATIONS_TO_LISTS_MAP.entrySet()) {
            List<? extends List<? extends Integration>> integrations = getAllEnvsIntegrations(
                    new ArrayList<>(integrationsMap.values()), entry.getKey(), entry.getValue());
            if (!integrations.isEmpty() && integrations.size() != integrationsMap.keySet().size()) {
                throw new DefaultFrameworkException(INTEGRATIONS_MISMATCH_ENVS,
                        integrations.get(0).get(0).getClass().getSimpleName());
            } else if (!integrations.isEmpty()) {
                List<String> defaultAliases = getDefaultAliases(integrations);
                checkIfAliasesDifferAndMatch(new ArrayList<>(integrationsMap.keySet()), defaultAliases, integrations);
                checkApiAuth(integrations);
                validateUrlsAndPorts(integrations);
            }
        }
    }

    private List<List<? extends Integration>> getAllEnvsIntegrations(final List<Integrations> integrationsList,
                                                                     final IntegrationsPredicate notNullPredicate,
                                                                     final IntegrationListMethod integrationExtractor) {
        return integrationsList.stream()
                .filter(notNullPredicate)
                .map(integrationExtractor)
                .map(integrations -> integrations.stream()
                        .filter(Integration::isEnabled)
                        .collect(Collectors.toList()))
                .filter(integrations -> !integrations.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> getDefaultAliases(final List<? extends List<? extends Integration>> integrationsList) {
        return integrationsList.stream()
                .min(Comparator.comparingInt((List<? extends Integration> integrationList) -> integrationList.size()))
                .map(integrationList -> integrationList.stream()
                        .map(Integration::getAlias)
                        .collect(Collectors.toList())).get();
    }

    private void checkIfAliasesDifferAndMatch(final List<String> envsList,
                                              final List<String> defaultAliases,
                                              final List<? extends List<? extends Integration>> integrationList) {
        for (int envNum = 0; envNum < envsList.size(); envNum++) {
            Set<String> aliasSet = new HashSet<>();
            for (Integration integration : integrationList.get(envNum)) {
                if (!aliasSet.add(integration.getAlias())) {
                    throw new DefaultFrameworkException(SAME_INTEGRATION_ALIAS, integration.getClass().getSimpleName(),
                            integration.getAlias(), FileSearcher.searchFileFromEnvFolder(envsList.get(envNum),
                            INTEGRATION_CONFIG_FILENAME).get().getPath());
                } else if (!defaultAliases.contains(integration.getAlias())) {
                    throw new DefaultFrameworkException(INTEGRATION_ALIAS_NOT_MATCH,
                            integration.getClass().getSimpleName(), integration.getAlias());
                }
            }
        }
    }

    private void checkApiAuth(final List<? extends List<? extends Integration>> integrations) {
        if (integrations.get(0).get(0) instanceof Api) {
            List<Map<String, Auth>> authMaps = getAuthMaps((List<List<Api>>) integrations);
            if (authMaps.size() > 1) {
                Map<String, Auth> defaultAuthMap = authMaps.stream().min(Comparator.comparingInt(Map::size)).get();
                authMaps.stream()
                        .flatMap(authMap -> authMap.entrySet().stream())
                        .forEach(entry -> checkAuth(entry.getValue(), entry.getKey(), defaultAuthMap));
            }
        }
    }

    private List<Map<String, Auth>> getAuthMaps(final List<List<Api>> integrations) {
        return integrations.stream()
                .map(apis -> apis.stream()
                        .filter(api -> nonNull(api.getAuth()))
                        .collect(Collectors.toMap(Api::getAlias, Api::getAuth)))
                .collect(Collectors.toList());
    }

    private void checkAuth(final Auth auth,
                           final String alias,
                           final Map<String, Auth> defaultAuthMap) {
        if (!defaultAuthMap.containsKey(alias)) {
            throw new DefaultFrameworkException(AUTH_NOT_PRESENT_IN_ALL_ENVS, alias);
        }
        if (!defaultAuthMap.get(alias).isAutoLogout() == auth.isAutoLogout()) {
            throw new DefaultFrameworkException(AUTH_LOGOUT_NOT_MATCH, alias);
        }
        if (!Objects.equals(defaultAuthMap.get(alias).getAuthStrategy(), auth.getAuthStrategy())) {
            throw new DefaultFrameworkException(AUTH_STRATEGY_NOT_MATCH, alias);
        }
        if (!Objects.equals(defaultAuthMap.get(alias).getAuthCustomClassName(), auth.getAuthCustomClassName())) {
            throw new DefaultFrameworkException(AUTH_CUSTOM_CLASS_NAME_NOT_MATCH, alias);
        }
    }

    private void validateUrlsAndPorts(List<? extends List<? extends Integration>> integrationsList) {
        for (Map.Entry<IntegrationPredicate, Function<Integration, ?>> entry : INTEGRATIONS_LISTS.entrySet()) {
            List<List<?>> integrations = getIntegrations(integrationsList, entry.getKey(), entry.getValue());
            checkDbConfigUrls(integrations);
            checkClickhouseUrls(integrations);
            checkRedisUrls(integrations);
            checkMongoUrls(integrations);
            checkS3Endpoints(integrations);
            checkSqsEndpoints(integrations);
            checkKafkaEndpoints(integrations);
            checkRabbitmqEndpoints(integrations);
            checkDynamoEndpoints(integrations);
            checkElasticsearchEndpoints(integrations);
            checkLambdaEndpoints(integrations);
            checkSesEndpoints(integrations);
            checkSendgridEndpoints(integrations);
        }
    }

    private List<List<?>> getIntegrations(final List<? extends List<? extends Integration>> integrationsList,
                                          final IntegrationPredicate instanceOfPredicate,
                                          final Function<Integration,?> getIntegrationsMethod) {
        return integrationsList.stream()
                .map(integrationList -> integrationList.stream()
                        .filter(instanceOfPredicate)
                        .map(getIntegrationsMethod)
                        .collect(Collectors.toList()))
                .filter(integrationList -> !integrationList.isEmpty())
                .collect(Collectors.toList());
    }

    private void checkDbConfigUrls(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof DatabaseConfig) {
            Set<String> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                DatabaseConfig dbConfig = (DatabaseConfig) databaseConfig;
                if (!urlsSet.add(dbConfig.getConnectionUrl())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, dbConfig.getConnectionUrl(), URL);
                }
            }));
        }
    }

    private void checkClickhouseUrls(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Clickhouse) {
            Set<String> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Clickhouse clickhouse = (Clickhouse) databaseConfig;
                if (!urlsSet.add(clickhouse.getConnectionUrl())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, clickhouse.getConnectionUrl(), URL);
                }
            }));
        }
    }

    private void checkRedisUrls(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Redis) {
            Set<Integer> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Redis redis = (Redis) databaseConfig;
                if (!urlsSet.add(redis.getPort())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, redis.getPort(), PORT);
                }
            }));
        }
    }

    private void checkMongoUrls(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Mongo) {
            Set<Integer> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Mongo mongo = (Mongo) databaseConfig;
                if (!urlsSet.add(mongo.getPort())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, mongo.getPort(), PORT);
                }
            }));
        }
    }

    private void checkS3Endpoints(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof S3) {
            Set<String> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                S3 s3 = (S3) databaseConfig;
                if (!urlsSet.add(s3.getEndpoint())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, s3.getEndpoint(), ENDPOINT);
                }
            }));
        }
    }

    private void checkSqsEndpoints(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Sqs) {
            Set<String> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Sqs sqs = (Sqs) databaseConfig;
                if (!urlsSet.add(sqs.getEndpoint())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, sqs.getEndpoint(), ENDPOINT);
                }
            }));
        }
    }

    private void checkKafkaEndpoints(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Kafka) {
            Set<String> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Kafka kafka = (Kafka) databaseConfig;
                if (!urlsSet.add(kafka.getBootstrapAddress())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, kafka.getBootstrapAddress(), ENDPOINT);
                }
            }));
        }
    }

    private void checkRabbitmqEndpoints(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Rabbitmq) {
            Set<Integer> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Rabbitmq rabbitmq = (Rabbitmq) databaseConfig;
                if (!urlsSet.add(rabbitmq.getPort())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, rabbitmq.getPort(), PORT);
                }
            }));
        }
    }

    private void checkDynamoEndpoints(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Dynamo) {
            Set<String> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Dynamo dynamo = (Dynamo) databaseConfig;
                if (!urlsSet.add(dynamo.getEndpoint())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, dynamo.getEndpoint(), ENDPOINT);
                }
            }));
        }
    }

    private void checkElasticsearchEndpoints(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Elasticsearch) {
            Set<Integer> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Elasticsearch eSearch = (Elasticsearch) databaseConfig;
                if (!urlsSet.add(eSearch.getPort())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, eSearch.getPort(), PORT);
                }
            }));
        }
    }

    private void checkLambdaEndpoints(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Lambda) {
            Set<String> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Lambda lambda = (Lambda) databaseConfig;
                if (!urlsSet.add(lambda.getEndpoint())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, lambda.getEndpoint(), ENDPOINT);
                }
            }));
        }
    }

    private void checkSesEndpoints(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Ses) {
            Set<String> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Ses ses = (Ses) databaseConfig;
                if (!urlsSet.add(ses.getEndpoint())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, ses.getEndpoint(), ENDPOINT);
                }
            }));
        }
    }

    private void checkSendgridEndpoints(final List<List<?>> dbConfigs) {
        if (!dbConfigs.isEmpty() && dbConfigs.get(0).get(0) instanceof Sendgrid) {
            Set<String> urlsSet = new HashSet<>();
            dbConfigs.forEach(dbConfigList -> dbConfigList.forEach(databaseConfig -> {
                Sendgrid ses = (Sendgrid) databaseConfig;
                if (!urlsSet.add(ses.getApiKey())) {
                    throw new DefaultFrameworkException(SAME_PORT_OR_ENDPOINT, ses.getApiKey(), API_KEY);
                }
            }));
        }
    }

    private interface IntegrationPredicate extends Predicate<Integration> { }
    private interface IntegrationsPredicate extends Predicate<Integrations> { }
    private interface IntegrationListMethod extends Function<Integrations, List<? extends Integration>> { }
}
