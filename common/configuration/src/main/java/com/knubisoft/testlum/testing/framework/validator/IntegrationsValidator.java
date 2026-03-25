package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Auth;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class IntegrationsValidator implements ConfigurationValidator<Map<String, Integrations>> {

    private final Map<IntegrationsPredicate, IntegrationListMethod> configToIntegrationListMap;

    private final FileSearcher fileSearcher;
    private final TestResourceSettings testResourceSettings;

    public IntegrationsValidator(final FileSearcher fileSearcher,
                                 final TestResourceSettings testResourceSettings) {
        this.fileSearcher = fileSearcher;
        this.testResourceSettings = testResourceSettings;
        final Map<IntegrationsPredicate, IntegrationListMethod> map = new HashMap<>(20);
        map.put(i -> Objects.nonNull(i.getApis()), i -> i.getApis().getApi());
        map.put(i -> Objects.nonNull(i.getWebsockets()), i -> i.getWebsockets().getApi());
        map.put(i -> Objects.nonNull(i.getS3Integration()), i -> i.getS3Integration().getS3());
        map.put(i -> Objects.nonNull(i.getSesIntegration()), i -> i.getSesIntegration().getSes());
        map.put(i -> Objects.nonNull(i.getSqsIntegration()), i -> i.getSqsIntegration().getSqs());
        map.put(i -> Objects.nonNull(i.getSmtpIntegration()), i -> i.getSmtpIntegration().getSmtp());
        map.put(i -> Objects.nonNull(i.getRedisIntegration()), i -> i.getRedisIntegration().getRedis());
        map.put(i -> Objects.nonNull(i.getMongoIntegration()), i -> i.getMongoIntegration().getMongo());
        map.put(i -> Objects.nonNull(i.getMysqlIntegration()), i -> i.getMysqlIntegration().getMysql());
        map.put(i -> Objects.nonNull(i.getKafkaIntegration()), i -> i.getKafkaIntegration().getKafka());
        map.put(i -> Objects.nonNull(i.getGraphqlIntegration()), i -> i.getGraphqlIntegration().getApi());
        map.put(i -> Objects.nonNull(i.getTwilioIntegration()), i -> i.getTwilioIntegration().getTwilio());
        map.put(i -> Objects.nonNull(i.getOracleIntegration()), i -> i.getOracleIntegration().getOracle());
        map.put(i -> Objects.nonNull(i.getDynamoIntegration()), i -> i.getDynamoIntegration().getDynamo());
        map.put(i -> Objects.nonNull(i.getLambdaIntegration()), i -> i.getLambdaIntegration().getLambda());
        map.put(i -> Objects.nonNull(i.getSendgridIntegration()), i -> i.getSendgridIntegration().getSendgrid());
        map.put(i -> Objects.nonNull(i.getPostgresIntegration()), i -> i.getPostgresIntegration().getPostgres());
        map.put(i -> Objects.nonNull(i.getRabbitmqIntegration()), i -> i.getRabbitmqIntegration().getRabbitmq());
        map.put(i -> Objects.nonNull(i.getClickhouseIntegration()), i -> i.getClickhouseIntegration().getClickhouse());
        map.put(i -> Objects.nonNull(i.getElasticsearchIntegration()),
                i -> i.getElasticsearchIntegration().getElasticsearch());
        this.configToIntegrationListMap = Collections.unmodifiableMap(map);
    }

    @Override
    public void validate(final Map<String, Integrations> integrationsMap) {
        configToIntegrationListMap.forEach((nonNullCheck, toIntegrationList) -> {
            List<List<? extends Integration>> integrationLists = getAllEnvsIntegrations(
                    new ArrayList<>(integrationsMap.values()), nonNullCheck, toIntegrationList);
            if (!integrationLists.isEmpty() && integrationLists.size() != integrationsMap.keySet().size()) {
                throw new DefaultFrameworkException(ExceptionMessage.INTEGRATIONS_MISMATCH_ENVS,
                        integrationLists.get(0).get(0).getClass().getSimpleName());
            } else if (!integrationLists.isEmpty()) {
                List<String> defaultAliases = getDefaultAliases(integrationLists);
                checkAliasesDifferAndMatch(new ArrayList<>(integrationsMap.keySet()), defaultAliases, integrationLists);
                checkApiAuth(integrationLists);
            }
        });
    }

    private List<List<? extends Integration>> getAllEnvsIntegrations(final List<Integrations> integrationsList,
                                                                     final IntegrationsPredicate nonNullCheck,
                                                                     final IntegrationListMethod toIntegrationList) {
        return integrationsList.stream()
                .filter(nonNullCheck)
                .map(toIntegrationList)
                .map(integrationList -> integrationList.stream()
                        .filter(Integration::isEnabled)
                        .toList())
                .filter(integrations -> !integrations.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> getDefaultAliases(final List<List<? extends Integration>> integrationLists) {
        return integrationLists.stream()
                .min(Comparator.comparingInt(List::size))
                .map(integrationList -> integrationList.stream()
                        .map(Integration::getAlias)
                        .toList())
                .orElse(Collections.emptyList());
    }

    private void checkAliasesDifferAndMatch(final List<String> envsList,
                                            final List<String> defaultAliases,
                                            final List<List<? extends Integration>> integrationLists) {
        IntStream.range(0, envsList.size()).forEach(envNum -> {
            Set<String> aliasSet = new HashSet<>();
            integrationLists.get(envNum).forEach(integration ->
                    checkIntegrationAliasDifferAndMatch(integration, envsList.get(envNum), defaultAliases, aliasSet));
        });
    }

    private void checkIntegrationAliasDifferAndMatch(final Integration integration,
                                                     final String envFolder,
                                                     final List<String> defaultAliases,
                                                     final Set<String> aliasSet) {
        if (!aliasSet.add(integration.getAlias())) {
            String path = fileSearcher.searchFileFromEnvFolder(envFolder,
                            TestResourceSettings.INTEGRATION_CONFIG_FILENAME)
                    .map(File::getPath).orElse(testResourceSettings.getEnvConfigFolder().getPath());
            throw new DefaultFrameworkException(ExceptionMessage.SAME_INTEGRATION_ALIAS,
                    integration.getClass().getSimpleName(),
                    integration.getAlias(), path);
        } else if (!defaultAliases.contains(integration.getAlias())) {
            throw new DefaultFrameworkException(ExceptionMessage.INTEGRATION_ALIAS_NOT_MATCH,
                    integration.getClass().getSimpleName(), integration.getAlias());
        }
    }

    private void checkApiAuth(final List<List<? extends Integration>> integrationLists) {
        List<Map<String, Auth>> authMaps = getAuthMaps(integrationLists);
        if (authMaps.size() > 1) {
            Map<String, Auth> defaultAuthMap = authMaps.stream().min(Comparator.comparingInt(Map::size)).get();
            authMaps.stream()
                    .flatMap(authMap -> authMap.entrySet().stream())
                    .forEach(entry -> checkAuth(entry.getValue(), entry.getKey(), defaultAuthMap));
        }
    }

    private List<Map<String, Auth>> getAuthMaps(final List<List<? extends Integration>> integrationLists) {
        return integrationLists.stream()
                .map(integrations -> integrations.stream()
                        .filter(integration -> integration instanceof Api)
                        .map(Api.class::cast)
                        .filter(api -> Objects.nonNull(api.getAuth()))
                        .collect(Collectors.toMap(Api::getAlias, Api::getAuth)))
                .toList();
    }

    private void checkAuth(final Auth auth, final String alias, final Map<String, Auth> defaultAuthMap) {
        if (!defaultAuthMap.containsKey(alias)) {
            throw new DefaultFrameworkException(ExceptionMessage.AUTH_NOT_PRESENT_IN_ALL_ENVS, alias);
        }
        if (!defaultAuthMap.get(alias).isAutoLogout() == auth.isAutoLogout()) {
            throw new DefaultFrameworkException(ExceptionMessage.AUTH_LOGOUT_NOT_MATCH, alias);
        }
        if (!Objects.equals(defaultAuthMap.get(alias).getAuthStrategy(), auth.getAuthStrategy())) {
            throw new DefaultFrameworkException(ExceptionMessage.AUTH_STRATEGY_NOT_MATCH, alias);
        }
        if (!Objects.equals(defaultAuthMap.get(alias).getAuthCustomClassName(), auth.getAuthCustomClassName())) {
            throw new DefaultFrameworkException(ExceptionMessage.AUTH_CUSTOM_CLASS_NAME_NOT_MATCH, alias);
        }
    }

    private interface IntegrationsPredicate extends Predicate<Integrations> { }
    private interface IntegrationListMethod extends Function<Integrations, List<? extends Integration>> { }
}
