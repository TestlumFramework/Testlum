package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Auth;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;

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

    private interface IntegrationsPredicate extends Predicate<Integrations> { }
    private interface IntegrationListMethod extends Function<Integrations, List<? extends Integration>> { }
}
