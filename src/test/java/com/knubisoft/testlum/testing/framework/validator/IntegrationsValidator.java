package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Auth;
import com.knubisoft.testlum.testing.model.global_config.AuthStrategies;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;

import java.util.ArrayList;
import java.util.Collections;
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
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.AUTH_NUM_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.AUTH_STRATEGY_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INTEGRATION_ALIAS_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INTEGRATION_NOT_ENABLED_IN_ALL_ENVS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NUM_OF_ENABLED_INTEGRATIONS_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_INTEGRATION_ALIAS;
import static java.util.Objects.nonNull;

public class IntegrationsValidator {

    private static final Map<IntegrationsPredicate, IntegrationListMethod> INTEGRATIONS_TO_LISTS_MAP;

    static {
        final Map<IntegrationsPredicate, IntegrationListMethod> map = new HashMap<>(20);
        map.put(i -> nonNull(i.getApis()), i -> i.getApis().getApi());
        map.put(i -> nonNull(i.getWebsockets()), i -> i.getWebsockets().getApi());
        map.put(i -> nonNull(i.getS3Integration()), i -> i.getS3Integration().getS3());
        map.put(i -> nonNull(i.getSesIntegration()), i -> i.getSesIntegration().getSes());
        map.put(i -> nonNull(i.getSqsIntegration()), i -> i.getSqsIntegration().getSqs());
        map.put(i -> nonNull(i.getSmtpIntegration()), i -> i.getSmtpIntegration().getSmtp());
        map.put(i -> nonNull(i.getRedisIntegration()), i -> i.getRedisIntegration().getRedis());
        map.put(i -> nonNull(i.getMongoIntegration()), i -> i.getMongoIntegration().getMongo());
        map.put(i -> nonNull(i.getMysqlIntegration()), i -> i.getMysqlIntegration().getMysql());
        map.put(i -> nonNull(i.getKafkaIntegration()), i -> i.getKafkaIntegration().getKafka());
        map.put(i -> nonNull(i.getGraphqlIntegration()), i -> i.getGraphqlIntegration().getApi());
        map.put(i -> nonNull(i.getTwilioIntegration()), i -> i.getTwilioIntegration().getTwilio());
        map.put(i -> nonNull(i.getOracleIntegration()), i -> i.getOracleIntegration().getOracle());
        map.put(i -> nonNull(i.getDynamoIntegration()), i -> i.getDynamoIntegration().getDynamo());
        map.put(i -> nonNull(i.getLambdaIntegration()), i -> i.getLambdaIntegration().getLambda());
        map.put(i -> nonNull(i.getSendgridIntegration()), i -> i.getSendgridIntegration().getSendgrid());
        map.put(i -> nonNull(i.getPostgresIntegration()), i -> i.getPostgresIntegration().getPostgres());
        map.put(i -> nonNull(i.getRabbitmqIntegration()), i -> i.getRabbitmqIntegration().getRabbitmq());
        map.put(i -> nonNull(i.getClickhouseIntegration()), i -> i.getClickhouseIntegration().getClickhouse());
        map.put(i -> nonNull(i.getElasticsearchIntegration()), i -> i.getElasticsearchIntegration().getElasticsearch());
        INTEGRATIONS_TO_LISTS_MAP = Collections.unmodifiableMap(map);
    }

    public void validateIntegrations(final Map<String, Integrations> integrationsMap) {
        List<Integrations> integrationsList = new ArrayList<>(integrationsMap.values());
        List<String> envsList = new ArrayList<>(integrationsMap.keySet());
        INTEGRATIONS_TO_LISTS_MAP.forEach((notNullPredicate, getIntegrationList) -> {
            List<? extends List<? extends Integration>> integrations =
                    getIntegrationsFromAllEnvs(integrationsList, notNullPredicate, getIntegrationList);
            if (!integrations.isEmpty() && integrations.size() != envsList.size()) {
                String integrationName = getIntegrationName(integrations);
                throw new DefaultFrameworkException(INTEGRATION_NOT_ENABLED_IN_ALL_ENVS, integrationName);
            } else if (!integrations.isEmpty()) {
                validate(envsList, integrations);
            }
        });
    }

    private List<List<? extends Integration>> getIntegrationsFromAllEnvs(
            final List<Integrations> integrationsList,
            final IntegrationsPredicate notNullPredicate,
            final IntegrationListMethod integrationExtractor) {
        return integrationsList.stream()
                .filter(notNullPredicate)
                .map(integrationExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void validate(final List<String> envsList,
                          final List<? extends List<? extends Integration>> integrations) {
        checkAliasesDifference(envsList, integrations);
        if (integrations.size() > 1) {
            String integrationName = getIntegrationName(integrations);
            int numOfEnabledIntegrations = getNumOfEnabledIntegrations(integrations.stream().findFirst().get());
            checkNumberOfEnabledIntegrations(envsList, numOfEnabledIntegrations, integrations, integrationName);
            checkAliasesSimilarity(integrations, numOfEnabledIntegrations, integrationName);
            if (isApiIntegration(integrations)) {
                checkApiAuth(envsList, (List<List<Api>>) integrations, integrationName);
            }
        }
    }

    private String getIntegrationName(final List<? extends List<? extends Integration>> integrationsList) {
        List<? extends Integration> integrations = integrationsList.stream().findFirst().get();
        Integration integration = integrations.stream().findFirst().get();
        return integration.getClass().getSimpleName();
    }

    private void checkAliasesDifference(final List<String> envsList,
                                        final List<? extends List<? extends Integration>> integrationList) {
        for (int envNum = 0; envNum < integrationList.size(); envNum++) {
            Set<String> aliasSet = new HashSet<>();
            for (Integration integration : integrationList.get(envNum)) {
                if (!aliasSet.add(integration.getAlias())) {
                    throw new DefaultFrameworkException(SAME_INTEGRATION_ALIAS, integration.getClass().getSimpleName(),
                            integration.getAlias(), FileSearcher.searchFileFromEnvFolder(envsList.get(envNum),
                            INTEGRATION_CONFIG_FILENAME).get().getPath());
                }
            }
        }
    }

    private void checkNumberOfEnabledIntegrations(final List<String> envsList,
                                                  final int firstEnvEnabledIntegrations,
                                                  final List<? extends List<? extends Integration>> integrationsList,
                                                  final String integrationName) {
        for (int envNum = 0; envNum < envsList.size(); envNum++) {
            int nextEnvEnabledIntegrations = getNumOfEnabledIntegrations(integrationsList.get(envNum));
            if (nextEnvEnabledIntegrations != firstEnvEnabledIntegrations) {
                throw new DefaultFrameworkException(NUM_OF_ENABLED_INTEGRATIONS_NOT_MATCH, integrationName,
                        FileSearcher.searchFileFromEnvFolder(envsList.get(envNum),
                                INTEGRATION_CONFIG_FILENAME).get().getPath());
            }
        }
    }

    private int getNumOfEnabledIntegrations(final List<? extends Integration> integrationsList) {
        return integrationsList.stream()
                .filter(Integration::isEnabled)
                .mapToInt(e -> 1).sum();
    }

    private void checkAliasesSimilarity(final List<? extends List<? extends Integration>> integrationsList,
                                        final int numOfEnabledIntegrations,
                                        final String integrationName) {
        for (int integration = 0; integration < numOfEnabledIntegrations; integration++) {
            List<String> aliases = getAliases(integrationsList, integration);
            if (aliases.stream().distinct().count() > 1) {
                throw new DefaultFrameworkException(INTEGRATION_ALIAS_NOT_MATCH,
                        integrationName, String.join(", ", aliases));
            }
        }
    }

    private List<String> getAliases(final List<? extends List<? extends Integration>> integrations,
                                    final int integrationNum) {
        return integrations.stream()
                .map(integrationList -> integrationList.get(integrationNum))
                .map(Integration::getAlias)
                .collect(Collectors.toList());
    }

    private boolean isApiIntegration(final List<? extends List<? extends Integration>> integrationsList) {
        List<? extends Integration> integrations = integrationsList.stream().findFirst().get();
        Integration integration = integrations.stream().findFirst().get();
        return integration instanceof Api;
    }

    private void checkApiAuth(final List<String> envsList,
                              final List<List<Api>> apiList,
                              final String integrationName) {
        List<Api> apis = apiList.stream().findFirst().get();
        for (int apiNum = 0; apiNum < apis.size(); apiNum++) {
            List<Auth> authList = getAuthList(apiList, apiNum);
            if (checkAuthPresence(apiList, integrationName, authList)) {
                checkAutoLogout(envsList, authList, integrationName);
                checkAuthStrategy(envsList, authList, integrationName);
                checkAuthCustomClassName(envsList, authList, integrationName);
            }
        }
    }

    private List<Auth> getAuthList(final List<List<Api>> apiList, final int apiNum) {
        return apiList.stream()
                .map(api -> api.get(apiNum))
                .map(Api::getAuth)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean checkAuthPresence(final List<List<Api>> apiList,
                                      final String integrationName,
                                      final List<Auth> authList) {
        if (authList.size() == 0) {
            return false;
        } else if (authList.size() != apiList.size()) {
            throw new DefaultFrameworkException(AUTH_NUM_NOT_MATCH, integrationName);
        }
        return true;
    }

    private void checkAutoLogout(final List<String> envsList,
                                 final List<Auth> authList,
                                 final String integrationName) {
        boolean autoLogout = authList.stream().findFirst().get().isAutoLogout();
        for (int envNum = 0; envNum < authList.size(); envNum++) {
            if (authList.get(envNum).isAutoLogout() != autoLogout) {
                throw new DefaultFrameworkException(AUTH_LOGOUT_NOT_MATCH, integrationName,
                        FileSearcher.searchFileFromEnvFolder(envsList.get(envNum),
                                INTEGRATION_CONFIG_FILENAME).get().getPath());
            }
        }
    }

    private void checkAuthStrategy(final List<String> envsList,
                                   final List<Auth> authList,
                                   final String integrationName) {
        AuthStrategies authStrategy = authList.stream().findFirst().get().getAuthStrategy();
        for (int envNum = 0; envNum < authList.size(); envNum++) {
            if (authList.get(envNum).getAuthStrategy() != authStrategy) {
                throw new DefaultFrameworkException(AUTH_STRATEGY_NOT_MATCH, integrationName,
                        FileSearcher.searchFileFromEnvFolder(envsList.get(envNum),
                                INTEGRATION_CONFIG_FILENAME).get().getPath());
            }
        }
    }

    private void checkAuthCustomClassName(final List<String> envsList,
                                          final List<Auth> authList,
                                          final String integrationName) {
        String authCustomClassName = authList.stream().findFirst().get().getAuthCustomClassName();
        for (int envNum = 0; envNum < authList.size(); envNum++) {
            if ((Objects.isNull(authList.get(envNum).getAuthCustomClassName()) && nonNull(authCustomClassName))
                    || !Objects.equals(authList.get(envNum).getAuthCustomClassName(), authCustomClassName)) {
                throw new DefaultFrameworkException(AUTH_CUSTOM_CLASS_NAME_NOT_MATCH, integrationName,
                        FileSearcher.searchFileFromEnvFolder(envsList.get(envNum),
                                INTEGRATION_CONFIG_FILENAME).get().getPath());
            }
        }
    }

    private interface IntegrationsPredicate extends Predicate<Integrations> { }
    private interface IntegrationListMethod extends Function<Integrations, List<? extends Integration>> { }
}
