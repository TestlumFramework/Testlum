package com.knubisoft.cott.testing.framework.validator;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.cott.testing.model.global_config.Integration;
import com.knubisoft.cott.testing.model.global_config.Integrations;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SAME_INTEGRATION_ALIASES;
import static java.util.Objects.nonNull;

public class GlobalTestConfigValidator implements XMLValidator<GlobalTestConfiguration> {
    private static final Map<IntegrationsPredicate, IntegrationsFunction> INTEGRATIONS_MAP;
    private final SubscriptionValidator subscriptionValidator = new StripeValidationService();

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
    public void validate(final GlobalTestConfiguration globalTestConfig, final File xmlFile) {
        checkIsActiveSubscription(globalTestConfig);
        checkIntegrationsAliases(globalTestConfig.getIntegrations());
    }

    private void checkIsActiveSubscription(final GlobalTestConfiguration globalTestConfig) {
        if (Objects.isNull(globalTestConfig.getSubscription())) {
            throw new DefaultFrameworkException("Cannot find customer subscription configuration");
        }
        if ("free".equalsIgnoreCase(globalTestConfig.getSubscription().getType().value())) {
            return;
        }
        try {
            subscriptionValidator.checkSubscription(globalTestConfig);
        } catch (Exception e) {
            LogUtil.logException(e);
            throw e;
        }
    }

    public void checkIntegrationsAliases(final Integrations configIntegrations) {
        INTEGRATIONS_MAP.keySet().stream()
                .filter(key -> key.test(configIntegrations))
                .map(INTEGRATIONS_MAP::get)
                .map(value -> value.apply(configIntegrations))
                .forEach(integrations -> {
                    for (Integration integration : integrations) {
                        if (integrations.stream().map(Integration::getAlias)
                                .filter(a -> a.equalsIgnoreCase(integration.getAlias())).count() > 1) {
                            throw new DefaultFrameworkException(SAME_INTEGRATION_ALIASES,
                                    integration.getClass().getSimpleName(), integration.getAlias());
                        }
                    }
                });
    }

    private interface IntegrationsPredicate extends Predicate<Integrations> {

    }

    private interface IntegrationsFunction extends Function<Integrations, List<? extends Integration>> {

    }
}
