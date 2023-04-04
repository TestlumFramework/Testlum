package com.knubisoft.cott.testing.framework.validator;

import com.knubisoft.cott.testing.framework.constant.LogMessage;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.global_config.Environment;
import com.knubisoft.cott.testing.model.global_config.Integration;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SAME_INTEGRATION_ALIASES;
import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class IntegrationsValidator implements XMLValidator<Integrations> {

    private static final Map<IntegrationsPredicate, IntegrationsFunction> INTEGRATIONS_MAP;
    private final Environment env;

    static {
        final Map<IntegrationsPredicate, IntegrationsFunction> map = new HashMap<>(20);
        map.put(i -> nonNull(i.getApis()), integrations -> integrations.getApis().getApi());
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
        INTEGRATIONS_MAP = Collections.unmodifiableMap(map);
    }

    @Override
    public void validate(final Integrations integrations, final File xmlFile) {
        List<List<? extends Integration>> integrationsLists = getDeclaredIntegrations(integrations);
        if (integrationsLists.size() == 0) {
            log.warn(LogMessage.INTEGRATIONS_NOT_FOUND, env.getFolder());
        } else {
            checkIntegrationsAliases(integrationsLists);
        }
    }

    private void checkIntegrationsAliases(final List<List<? extends Integration>> integrationsLists) {
        for (List<? extends Integration> integrations : integrationsLists) {
            integrations.forEach(integration -> {
                long numOfSameAliases = integrations.stream()
                        .filter(Integration::isEnabled)
                        .map(Integration::getAlias)
                        .filter(alias -> alias.equalsIgnoreCase(integration.getAlias())).count();
                if (numOfSameAliases > 1) {
                    throw new DefaultFrameworkException(SAME_INTEGRATION_ALIASES,
                            integration.getClass().getSimpleName(), integration.getAlias(), env.getFolder());
                }
            });
        }
    }

    private List<List<? extends Integration>> getDeclaredIntegrations(final Integrations integrations) {
        return INTEGRATIONS_MAP.keySet().stream()
                .filter(key -> key.test(integrations))
                .map(INTEGRATIONS_MAP::get)
                .map(value -> value.apply(integrations))
                .collect(Collectors.toList());
    }


    private interface IntegrationsPredicate extends Predicate<Integrations> {

    }

    private interface IntegrationsFunction extends Function<Integrations, List<? extends Integration>> {

    }
}
