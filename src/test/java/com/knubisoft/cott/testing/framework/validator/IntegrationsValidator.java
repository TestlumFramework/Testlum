package com.knubisoft.cott.testing.framework.validator;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.global_config.Environment;
import com.knubisoft.cott.testing.model.global_config.Integration;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SAME_INTEGRATION_ALIASES;
import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
public class IntegrationsValidator implements XMLValidator<Integrations> {

    private static final Map<IntegrationsPredicate, IntegrationListMethod> INTEGRATIONS_TO_LISTS_MAP;
    private final Environment env;

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

    @Override
    public void validate(final Integrations integrations, final File xmlFile) {
        checkEveryIntegration(integrations);
    }

    private void checkEveryIntegration(final Integrations integrations) {
        for (Map.Entry<IntegrationsPredicate, IntegrationListMethod> entry : INTEGRATIONS_TO_LISTS_MAP.entrySet()) {
            if (entry.getKey().test(integrations)) {
                List<? extends Integration> integrationsList = entry.getValue().apply(integrations);
                checkForSameAliases(integrationsList);
            }
        }
    }

    private void checkForSameAliases(final List<? extends Integration> integrationsList) {
        for (Integration integration : integrationsList) {
            if (getNumOfSameAliases(integrationsList, integration.getAlias()) > 1) {
                throw new DefaultFrameworkException(SAME_INTEGRATION_ALIASES,
                        integration.getClass().getSimpleName(), integration.getAlias(), env.getFolder());
            }
        }
    }

    private long getNumOfSameAliases(final List<? extends Integration> integrationsList,
                                     final String currentAlias) {
        return integrationsList.stream()
                .filter(Integration::isEnabled)
                .filter(integration -> integration.getAlias().equalsIgnoreCase(currentAlias))
                .count();
    }

    private interface IntegrationsPredicate extends Predicate<Integrations> { }
    private interface IntegrationListMethod extends Function<Integrations, List<? extends Integration>> { }
}
