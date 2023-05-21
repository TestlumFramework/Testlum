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
            List<? extends List<? extends Integration>> integrations = getIntegrationsFromAllEnvs(
                    integrationsList, notNullPredicate, getIntegrationList);
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
            checkNumberOfEnabledIntegrations(envsList.size(), integrationName, integrations);
            checkAliasesSimilarity(integrationName, integrations);
            if (isApiIntegration(integrations)) {
                checkApiAuth((List<List<Api>>) integrations, integrationName);
            }
        }
    }

    private String getIntegrationName(final List<? extends List<? extends Integration>> integrationsList) {
        List<? extends Integration> integrations = integrationsList.get(0);
        Integration integration = integrations.get(0);
        return integration.getClass().getSimpleName();
    }

    private void checkAliasesDifference(final List<String> envsList,
                                        final List<? extends List<? extends Integration>> integrationList) {
        for (int envNum = 0; envNum < envsList.size(); envNum++) {
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

    private void checkNumberOfEnabledIntegrations(final int envListSize,
                                                  final String integrationName,
                                                  final List<? extends List<? extends Integration>> integrationsList) {
        int defaultNumOfEnabledIntegrations = -1;
        for (int envNum = 0; envNum < envListSize; envNum++) {
            if (defaultNumOfEnabledIntegrations == -1) {
                defaultNumOfEnabledIntegrations = getNumOfEnabledIntegrations(integrationsList.get(envNum));
            }
            if (defaultNumOfEnabledIntegrations != getNumOfEnabledIntegrations(integrationsList.get(envNum))) {
                throw new DefaultFrameworkException(NUM_OF_ENABLED_INTEGRATIONS_NOT_MATCH,
                        integrationName, INTEGRATION_CONFIG_FILENAME);
            }
        }
    }

    private int getNumOfEnabledIntegrations(final List<? extends Integration> integrationsList) {
        return integrationsList.stream()
                .filter(Integration::isEnabled)
                .mapToInt(e -> 1).sum();
    }

    private void checkAliasesSimilarity(final String integrationName,
                                        final List<? extends List<? extends Integration>> integrationsList) {
        for (int integration = 0; integration < getNumOfEnabledIntegrations(integrationsList.get(0)); integration++) {
            int integrationNum = integration;
            List<String> aliases = integrationsList.stream()
                    .map(integrationList -> integrationList.get(integrationNum))
                    .map(Integration::getAlias)
                    .collect(Collectors.toList());

            if (aliases.stream().distinct().count() > 1) {
                throw new DefaultFrameworkException(INTEGRATION_ALIAS_NOT_MATCH,
                        integrationName, String.join(", ", aliases));
            }
        }
    }

    private boolean isApiIntegration(final List<? extends List<? extends Integration>> integrationsList) {
        List<? extends Integration> integrations = integrationsList.get(0);
        Integration integration = integrations.get(0);
        return integration instanceof Api;
    }

    private void checkApiAuth(final List<List<Api>> apiList,
                              final String integrationName) {
        for (int apiNum = 0; apiNum < apiList.get(0).size(); apiNum++) {
            int finalApiNum = apiNum;
            List<Auth> authList = apiList.stream()
                    .map(api -> api.get(finalApiNum).getAuth())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (checkAuthPresence(apiList.size(), authList.size(), integrationName)) {
                checkAutoLogout(authList, integrationName);
                checkAuthStrategy(authList, integrationName);
                checkAuthCustomClassName(authList, integrationName);
            }
        }
    }

    private boolean checkAuthPresence(final int apiListSize,
                                      final int authListSize,
                                      final String integrationName) {
        if (authListSize == 0) {
            return false;
        } else if (authListSize != apiListSize) {
            throw new DefaultFrameworkException(AUTH_NUM_NOT_MATCH, integrationName);
        }
        return true;
    }

    private void checkAutoLogout(final List<Auth> authList,
                                 final String integrationName) {
        boolean defaultAutoLogout = authList.get(0).isAutoLogout();
        for (Auth auth : authList) {
            if (auth.isAutoLogout() != defaultAutoLogout) {
                throw new DefaultFrameworkException(AUTH_LOGOUT_NOT_MATCH,
                        integrationName, INTEGRATION_CONFIG_FILENAME);
            }
        }
    }

    private void checkAuthStrategy(final List<Auth> authList,
                                   final String integrationName) {
        AuthStrategies defaultAuthStrategy = authList.get(0).getAuthStrategy();
        for (Auth auth : authList) {
            if (auth.getAuthStrategy() != defaultAuthStrategy) {
                throw new DefaultFrameworkException(AUTH_STRATEGY_NOT_MATCH,
                        integrationName, INTEGRATION_CONFIG_FILENAME);
            }
        }
    }

    private void checkAuthCustomClassName(final List<Auth> authList,
                                          final String integrationName) {
        String defaultCustomClassName = authList.get(0).getAuthCustomClassName();
        for (Auth auth : authList) {
            if ((Objects.isNull(auth.getAuthCustomClassName()) && nonNull(defaultCustomClassName))
                    || !Objects.equals(auth.getAuthCustomClassName(), defaultCustomClassName)) {
                throw new DefaultFrameworkException(AUTH_CUSTOM_CLASS_NAME_NOT_MATCH,
                        integrationName, INTEGRATION_CONFIG_FILENAME);
            }
        }
    }

    private interface IntegrationsPredicate extends Predicate<Integrations> { }
    private interface IntegrationListMethod extends Function<Integrations, List<? extends Integration>> { }
}
