package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.config.IntegrationConfig;
import com.testlum.reporting.sdk.model.config.IntegrationKind;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.model.global_config.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

import static java.util.Map.entry;

@Component
@RequiredArgsConstructor
public class IntegrationConfigMapper {

    private static final Map<IntegrationKind, Function<Integrations, List<? extends Integration>>> SOURCES =
            new EnumMap<>(Map.ofEntries(
                    entry(IntegrationKind.AI, items(Integrations::getAiIntegration, AiIntegration::getAi)),
                    entry(IntegrationKind.API, items(Integrations::getApis, Apis::getApi)),
                    entry(IntegrationKind.WEBSOCKET, items(Integrations::getWebsockets, Websockets::getApi)),
                    entry(IntegrationKind.GRAPHQL,
                            items(Integrations::getGraphqlIntegration, GraphqlIntegration::getApi)),
                    entry(IntegrationKind.POSTGRES,
                            items(Integrations::getPostgresIntegration, PostgresIntegration::getPostgres)),
                    entry(IntegrationKind.CLICKHOUSE,
                            items(Integrations::getClickhouseIntegration, ClickhouseIntegration::getClickhouse)),
                    entry(IntegrationKind.MYSQL, items(Integrations::getMysqlIntegration, MysqlIntegration::getMysql)),
                    entry(IntegrationKind.ORACLE,
                            items(Integrations::getOracleIntegration, OracleIntegration::getOracle)),
                    entry(IntegrationKind.REDIS, items(Integrations::getRedisIntegration, RedisIntegration::getRedis)),
                    entry(IntegrationKind.MONGO, items(Integrations::getMongoIntegration, MongoIntegration::getMongo)),
                    entry(IntegrationKind.S3, items(Integrations::getS3Integration, S3Integration::getS3)),
                    entry(IntegrationKind.SQS, items(Integrations::getSqsIntegration, SqsIntegration::getSqs)),
                    entry(IntegrationKind.KAFKA, items(Integrations::getKafkaIntegration, KafkaIntegration::getKafka)),
                    entry(IntegrationKind.RABBITMQ,
                            items(Integrations::getRabbitmqIntegration, RabbitmqIntegration::getRabbitmq)),
                    entry(IntegrationKind.DYNAMO,
                            items(Integrations::getDynamoIntegration, DynamoIntegration::getDynamo)),
                    entry(IntegrationKind.ELASTICSEARCH, items(Integrations::getElasticsearchIntegration,
                            ElasticsearchIntegration::getElasticsearch)),
                    entry(IntegrationKind.LAMBDA,
                            items(Integrations::getLambdaIntegration, LambdaIntegration::getLambda)),
                    entry(IntegrationKind.SENDGRID,
                            items(Integrations::getSendgridIntegration, SendgridIntegration::getSendgrid)),
                    entry(IntegrationKind.SES, items(Integrations::getSesIntegration, SesIntegration::getSes)),
                    entry(IntegrationKind.SMTP, items(Integrations::getSmtpIntegration, SmtpIntegration::getSmtp)),
                    entry(IntegrationKind.TWILIO,
                            items(Integrations::getTwilioIntegration, TwilioIntegration::getTwilio)),
                    entry(IntegrationKind.SQL_DATABASE,
                            items(Integrations::getSqlDatabaseIntegration, SqlDatabaseIntegration::getSqlDatabase))));

    private final JacksonService jacksonService;

    private static <W> Function<Integrations, List<? extends Integration>> items(
            final Function<Integrations, W> wrapper, final Function<W, List<? extends Integration>> list) {
        return integrations -> Optional.ofNullable(wrapper.apply(integrations)).map(list).orElse(List.of());
    }

    public List<IntegrationConfig> map(final Integrations integrations) {
        List<IntegrationConfig> result = new ArrayList<>();
        if (integrations != null) {
            SOURCES.forEach((kind, source) -> source.apply(integrations)
                    .forEach(integration -> result.add(map(kind, integration))));
        }
        return result;
    }

    private IntegrationConfig map(final IntegrationKind kind, final Integration integration) {
        return IntegrationConfig.builder()
                .kind(kind)
                .alias(integration.getAlias())
                .enabled(integration.isEnabled())
                .body(jacksonService.toMapByFields(integration))
                .build();
    }
}
