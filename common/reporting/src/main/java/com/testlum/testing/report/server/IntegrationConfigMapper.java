package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.config.AiIntegrationConfig;
import com.testlum.reporting.sdk.model.config.ApiAuthConfig;
import com.testlum.reporting.sdk.model.config.ApiAuthStrategy;
import com.testlum.reporting.sdk.model.config.ApiIntegrationConfig;
import com.testlum.reporting.sdk.model.config.AwsConnectionConfig;
import com.testlum.reporting.sdk.model.config.ClickhouseIntegrationConfig;
import com.testlum.reporting.sdk.model.config.DatabaseIntegrationConfig;
import com.testlum.reporting.sdk.model.config.DynamoIntegrationConfig;
import com.testlum.reporting.sdk.model.config.ElasticsearchIntegrationConfig;
import com.testlum.reporting.sdk.model.config.GraphqlIntegrationConfig;
import com.testlum.reporting.sdk.model.config.HikariConfig;
import com.testlum.reporting.sdk.model.config.IntegrationConfig;
import com.testlum.reporting.sdk.model.config.IntegrationKind;
import com.testlum.reporting.sdk.model.config.KafkaIntegrationConfig;
import com.testlum.reporting.sdk.model.config.LambdaIntegrationConfig;
import com.testlum.reporting.sdk.model.config.MongoIntegrationConfig;
import com.testlum.reporting.sdk.model.config.MysqlIntegrationConfig;
import com.testlum.reporting.sdk.model.config.OracleIntegrationConfig;
import com.testlum.reporting.sdk.model.config.PostgresIntegrationConfig;
import com.testlum.reporting.sdk.model.config.RabbitmqIntegrationConfig;
import com.testlum.reporting.sdk.model.config.RedisIntegrationConfig;
import com.testlum.reporting.sdk.model.config.S3IntegrationConfig;
import com.testlum.reporting.sdk.model.config.SendgridIntegrationConfig;
import com.testlum.reporting.sdk.model.config.SesIntegrationConfig;
import com.testlum.reporting.sdk.model.config.SmtpIntegrationConfig;
import com.testlum.reporting.sdk.model.config.SqlDatabaseIntegrationConfig;
import com.testlum.reporting.sdk.model.config.SqsIntegrationConfig;
import com.testlum.reporting.sdk.model.config.StorageIntegrationConfig;
import com.testlum.reporting.sdk.model.config.TwilioIntegrationConfig;
import com.testlum.reporting.sdk.model.config.WebsocketIntegrationConfig;
import com.testlum.testing.model.global_config.Ai;
import com.testlum.testing.model.global_config.AiIntegration;
import com.testlum.testing.model.global_config.Api;
import com.testlum.testing.model.global_config.Apis;
import com.testlum.testing.model.global_config.Auth;
import com.testlum.testing.model.global_config.Clickhouse;
import com.testlum.testing.model.global_config.ClickhouseIntegration;
import com.testlum.testing.model.global_config.DatabaseConfig;
import com.testlum.testing.model.global_config.Dynamo;
import com.testlum.testing.model.global_config.DynamoIntegration;
import com.testlum.testing.model.global_config.Elasticsearch;
import com.testlum.testing.model.global_config.ElasticsearchIntegration;
import com.testlum.testing.model.global_config.GraphqlApi;
import com.testlum.testing.model.global_config.GraphqlIntegration;
import com.testlum.testing.model.global_config.Hikari;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Kafka;
import com.testlum.testing.model.global_config.KafkaIntegration;
import com.testlum.testing.model.global_config.Lambda;
import com.testlum.testing.model.global_config.LambdaIntegration;
import com.testlum.testing.model.global_config.Mongo;
import com.testlum.testing.model.global_config.MongoIntegration;
import com.testlum.testing.model.global_config.Mysql;
import com.testlum.testing.model.global_config.MysqlIntegration;
import com.testlum.testing.model.global_config.Oracle;
import com.testlum.testing.model.global_config.OracleIntegration;
import com.testlum.testing.model.global_config.Postgres;
import com.testlum.testing.model.global_config.PostgresIntegration;
import com.testlum.testing.model.global_config.Rabbitmq;
import com.testlum.testing.model.global_config.RabbitmqIntegration;
import com.testlum.testing.model.global_config.Redis;
import com.testlum.testing.model.global_config.RedisIntegration;
import com.testlum.testing.model.global_config.S3;
import com.testlum.testing.model.global_config.S3Integration;
import com.testlum.testing.model.global_config.Sendgrid;
import com.testlum.testing.model.global_config.SendgridIntegration;
import com.testlum.testing.model.global_config.Ses;
import com.testlum.testing.model.global_config.SesIntegration;
import com.testlum.testing.model.global_config.Smtp;
import com.testlum.testing.model.global_config.SmtpIntegration;
import com.testlum.testing.model.global_config.SqlDatabase;
import com.testlum.testing.model.global_config.SqlDatabaseIntegration;
import com.testlum.testing.model.global_config.Sqs;
import com.testlum.testing.model.global_config.SqsIntegration;
import com.testlum.testing.model.global_config.StorageIntegration;
import com.testlum.testing.model.global_config.Twilio;
import com.testlum.testing.model.global_config.TwilioIntegration;
import com.testlum.testing.model.global_config.WebsocketApi;
import com.testlum.testing.model.global_config.Websockets;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Map.entry;

/**
 * Maps an environment's JAXB {@code integration.xml} config onto the typed reporting-SDK integrations: one
 * {@link IntegrationConfig} subclass per kind, ordered by kind and then by declaration order in the file.
 */
@Component
public class IntegrationConfigMapper {

    private static final Map<IntegrationKind, Function<Integrations, List<IntegrationConfig>>> SOURCES =
            new EnumMap<>(Map.ofEntries(
                    entry(IntegrationKind.AI,
                            items(Integrations::getAiIntegration, AiIntegration::getAi, IntegrationConfigMapper::ai)),
                    entry(IntegrationKind.API,
                            items(Integrations::getApis, Apis::getApi, IntegrationConfigMapper::api)),
                    entry(IntegrationKind.WEBSOCKET, items(Integrations::getWebsockets, Websockets::getApi,
                            IntegrationConfigMapper::websocket)),
                    entry(IntegrationKind.GRAPHQL, items(Integrations::getGraphqlIntegration,
                            GraphqlIntegration::getApi, IntegrationConfigMapper::graphql)),
                    entry(IntegrationKind.POSTGRES, items(Integrations::getPostgresIntegration,
                            PostgresIntegration::getPostgres, IntegrationConfigMapper::postgres)),
                    entry(IntegrationKind.CLICKHOUSE, items(Integrations::getClickhouseIntegration,
                            ClickhouseIntegration::getClickhouse, IntegrationConfigMapper::clickhouse)),
                    entry(IntegrationKind.MYSQL, items(Integrations::getMysqlIntegration,
                            MysqlIntegration::getMysql, IntegrationConfigMapper::mysql)),
                    entry(IntegrationKind.ORACLE, items(Integrations::getOracleIntegration,
                            OracleIntegration::getOracle, IntegrationConfigMapper::oracle)),
                    entry(IntegrationKind.REDIS, items(Integrations::getRedisIntegration,
                            RedisIntegration::getRedis, IntegrationConfigMapper::redis)),
                    entry(IntegrationKind.MONGO, items(Integrations::getMongoIntegration,
                            MongoIntegration::getMongo, IntegrationConfigMapper::mongo)),
                    entry(IntegrationKind.S3,
                            items(Integrations::getS3Integration, S3Integration::getS3, IntegrationConfigMapper::s3)),
                    entry(IntegrationKind.SQS, items(Integrations::getSqsIntegration,
                            SqsIntegration::getSqs, IntegrationConfigMapper::sqs)),
                    entry(IntegrationKind.KAFKA, items(Integrations::getKafkaIntegration,
                            KafkaIntegration::getKafka, IntegrationConfigMapper::kafka)),
                    entry(IntegrationKind.RABBITMQ, items(Integrations::getRabbitmqIntegration,
                            RabbitmqIntegration::getRabbitmq, IntegrationConfigMapper::rabbitmq)),
                    entry(IntegrationKind.DYNAMO, items(Integrations::getDynamoIntegration,
                            DynamoIntegration::getDynamo, IntegrationConfigMapper::dynamo)),
                    entry(IntegrationKind.ELASTICSEARCH, items(Integrations::getElasticsearchIntegration,
                            ElasticsearchIntegration::getElasticsearch, IntegrationConfigMapper::elasticsearch)),
                    entry(IntegrationKind.LAMBDA, items(Integrations::getLambdaIntegration,
                            LambdaIntegration::getLambda, IntegrationConfigMapper::lambda)),
                    entry(IntegrationKind.SENDGRID, items(Integrations::getSendgridIntegration,
                            SendgridIntegration::getSendgrid, IntegrationConfigMapper::sendgrid)),
                    entry(IntegrationKind.SES, items(Integrations::getSesIntegration,
                            SesIntegration::getSes, IntegrationConfigMapper::ses)),
                    entry(IntegrationKind.SMTP, items(Integrations::getSmtpIntegration,
                            SmtpIntegration::getSmtp, IntegrationConfigMapper::smtp)),
                    entry(IntegrationKind.TWILIO, items(Integrations::getTwilioIntegration,
                            TwilioIntegration::getTwilio, IntegrationConfigMapper::twilio)),
                    entry(IntegrationKind.SQL_DATABASE, items(Integrations::getSqlDatabaseIntegration,
                            SqlDatabaseIntegration::getSqlDatabase, IntegrationConfigMapper::sqlDatabase))));

    private static <W, T> Function<Integrations, List<IntegrationConfig>> items(
            final Function<Integrations, W> wrapper, final Function<W, List<T>> list,
            final Function<T, ? extends IntegrationConfig> mapper) {
        return integrations -> Optional.ofNullable(wrapper.apply(integrations)).map(list).orElse(List.of())
                .stream()
                .<IntegrationConfig>map(mapper::apply)
                .toList();
    }

    public List<IntegrationConfig> map(final Integrations integrations) {
        List<IntegrationConfig> result = new ArrayList<>();
        if (integrations != null) {
            SOURCES.values().forEach(source -> result.addAll(source.apply(integrations)));
        }
        return result;
    }

    private static PostgresIntegrationConfig postgres(final Postgres postgres) {
        return database(PostgresIntegrationConfig.builder(), postgres).build();
    }

    private static MysqlIntegrationConfig mysql(final Mysql mysql) {
        return database(MysqlIntegrationConfig.builder(), mysql).build();
    }

    private static OracleIntegrationConfig oracle(final Oracle oracle) {
        return database(OracleIntegrationConfig.builder(), oracle).build();
    }

    private static SqlDatabaseIntegrationConfig sqlDatabase(final SqlDatabase sqlDatabase) {
        return database(SqlDatabaseIntegrationConfig.builder(), sqlDatabase)
                .customTruncateFile(sqlDatabase.getCustomTruncate() == null
                        ? null : sqlDatabase.getCustomTruncate().getTruncateFile())
                .build();
    }

    private static ClickhouseIntegrationConfig clickhouse(final Clickhouse clickhouse) {
        return storage(ClickhouseIntegrationConfig.builder(), clickhouse)
                .jdbcDriver(clickhouse.getJdbcDriver())
                .username(clickhouse.getUsername())
                .password(clickhouse.getPassword())
                .connectionUrl(clickhouse.getConnectionUrl())
                .build();
    }

    private static MongoIntegrationConfig mongo(final Mongo mongo) {
        return storage(MongoIntegrationConfig.builder(), mongo)
                .database(mongo.getDatabase())
                .host(mongo.getHost())
                .port(mongo.getPort())
                .username(mongo.getUsername())
                .password(mongo.getPassword())
                .build();
    }

    private static RedisIntegrationConfig redis(final Redis redis) {
        return storage(RedisIntegrationConfig.builder(), redis)
                .host(redis.getHost())
                .port(redis.getPort())
                .build();
    }

    private static ElasticsearchIntegrationConfig elasticsearch(final Elasticsearch elasticsearch) {
        return storage(ElasticsearchIntegrationConfig.builder(), elasticsearch)
                .host(elasticsearch.getHost())
                .port(elasticsearch.getPort())
                .scheme(elasticsearch.getScheme())
                .connectionTimeout(elasticsearch.getConnectionTimeout())
                .socketTimeout(elasticsearch.getSocketTimeout())
                .signer(elasticsearch.isSigner())
                .serviceName(elasticsearch.getServiceName())
                .region(elasticsearch.getRegion())
                .username(elasticsearch.getUsername())
                .password(elasticsearch.getPassword())
                .build();
    }

    private static KafkaIntegrationConfig kafka(final Kafka kafka) {
        return storage(KafkaIntegrationConfig.builder(), kafka)
                .bootstrapAddress(kafka.getBootstrapAddress())
                .autoOffsetReset(kafka.getAutoOffsetReset())
                .maxPollRecords(kafka.getMaxPollRecords())
                .maxPollIntervalMs(kafka.getMaxPollIntervalMs())
                .clientId(kafka.getClientId())
                .groupId(kafka.getGroupId())
                .autoCommitTimeout(kafka.getAutoCommitTimeout())
                .build();
    }

    private static RabbitmqIntegrationConfig rabbitmq(final Rabbitmq rabbitmq) {
        return storage(RabbitmqIntegrationConfig.builder(), rabbitmq)
                .host(rabbitmq.getHost())
                .port(rabbitmq.getPort())
                .username(rabbitmq.getUsername())
                .password(rabbitmq.getPassword())
                .apiPort(rabbitmq.getApiPort())
                .virtualHost(rabbitmq.getVirtualHost())
                .enabledMetrics(rabbitmq.isEnabledMetrics())
                .build();
    }

    private static S3IntegrationConfig s3(final S3 s3) {
        return storage(S3IntegrationConfig.builder(), s3)
                .aws(aws(s3.getRegion(), s3.getEndpoint(), s3.getAccessKeyId(), s3.getSecretAccessKey()))
                .build();
    }

    private static SqsIntegrationConfig sqs(final Sqs sqs) {
        return storage(SqsIntegrationConfig.builder(), sqs)
                .aws(aws(sqs.getRegion(), sqs.getEndpoint(), sqs.getAccessKeyId(), sqs.getSecretAccessKey()))
                .build();
    }

    private static DynamoIntegrationConfig dynamo(final Dynamo dynamo) {
        return storage(DynamoIntegrationConfig.builder(), dynamo)
                .aws(aws(dynamo.getRegion(), dynamo.getEndpoint(), dynamo.getAccessKeyId(),
                        dynamo.getSecretAccessKey()))
                .sessionToken(dynamo.getSessionToken())
                .build();
    }

    private static LambdaIntegrationConfig lambda(final Lambda lambda) {
        return base(LambdaIntegrationConfig.builder(), lambda)
                .aws(aws(lambda.getRegion(), lambda.getEndpoint(), lambda.getAccessKeyId(),
                        lambda.getSecretAccessKey()))
                .build();
    }

    private static SesIntegrationConfig ses(final Ses ses) {
        return base(SesIntegrationConfig.builder(), ses)
                .aws(aws(ses.getRegion(), ses.getEndpoint(), ses.getAccessKeyId(), ses.getSecretAccessKey()))
                .build();
    }

    private static SendgridIntegrationConfig sendgrid(final Sendgrid sendgrid) {
        return base(SendgridIntegrationConfig.builder(), sendgrid)
                .apiKey(sendgrid.getApiKey())
                .build();
    }

    private static SmtpIntegrationConfig smtp(final Smtp smtp) {
        return base(SmtpIntegrationConfig.builder(), smtp)
                .host(smtp.getHost())
                .port(smtp.getPort() == null ? null : smtp.getPort().intValue())
                .username(smtp.getUsername())
                .password(smtp.getPassword())
                .smtpAuth(smtp.isSmtpAuth())
                .smtpStarttlsEnable(smtp.isSmtpStarttlsEnable())
                .build();
    }

    private static TwilioIntegrationConfig twilio(final Twilio twilio) {
        return base(TwilioIntegrationConfig.builder(), twilio)
                .accountSid(twilio.getAccountSid())
                .authToken(twilio.getAuthToken())
                .twilioNumber(twilio.getTwilioNumber())
                .build();
    }

    private static AiIntegrationConfig ai(final Ai ai) {
        return base(AiIntegrationConfig.builder(), ai)
                .apiKey(ai.getApiKey())
                .modelName(ai.getModelName())
                .baseUrl(ai.getBaseUrl())
                .build();
    }

    private static ApiIntegrationConfig api(final Api api) {
        return base(ApiIntegrationConfig.builder(), api)
                .url(api.getUrl())
                .auth(auth(api.getAuth()))
                .build();
    }

    private static ApiAuthConfig auth(final Auth auth) {
        if (auth == null) {
            return null;
        }
        return ApiAuthConfig.builder()
                .autoLogout(auth.isAutoLogout())
                .authStrategy(auth.getAuthStrategy() == null
                        ? null : ApiAuthStrategy.valueOf(auth.getAuthStrategy().name()))
                .tokenName(auth.getTokenName())
                .authCustomClassName(auth.getAuthCustomClassName())
                .build();
    }

    private static GraphqlIntegrationConfig graphql(final GraphqlApi graphql) {
        return base(GraphqlIntegrationConfig.builder(), graphql)
                .url(graphql.getUrl())
                .build();
    }

    private static WebsocketIntegrationConfig websocket(final WebsocketApi websocket) {
        return base(WebsocketIntegrationConfig.builder(), websocket)
                .url(websocket.getUrl())
                .protocol(websocket.getProtocol() == null ? null
                        : com.testlum.reporting.sdk.model.config.WebsocketProtocol.valueOf(
                                websocket.getProtocol().name()))
                .build();
    }

    private static <B extends DatabaseIntegrationConfig.DatabaseIntegrationConfigBuilder<?, ?>> B database(
            final B builder, final DatabaseConfig database) {
        storage(builder, database)
                .jdbcDriver(database.getJdbcDriver())
                .username(database.getUsername())
                .password(database.getPassword())
                .connectionUrl(database.getConnectionUrl())
                .schema(database.getSchema())
                .hikari(hikari(database.getHikari()));
        return builder;
    }

    private static HikariConfig hikari(final Hikari hikari) {
        return hikari == null ? null : HikariConfig.builder()
                .connectionTimeout(hikari.getConnectionTimeout())
                .idleTimeout(hikari.getIdleTimeout())
                .maxLifetime(hikari.getMaxLifetime())
                .maximumPoolSize(hikari.getMaximumPoolSize())
                .minimumIdle(hikari.getMinimumIdle())
                .connectionInitSql(hikari.getConnectionInitSql())
                .connectionTestQuery(hikari.getConnectionTestQuery())
                .poolName(hikari.getPoolName())
                .autoCommit(hikari.isAutoCommit())
                .build();
    }

    private static <B extends StorageIntegrationConfig.StorageIntegrationConfigBuilder<?, ?>> B storage(
            final B builder, final StorageIntegration storage) {
        base(builder, storage).truncate(storage.isTruncate());
        return builder;
    }

    private static <B extends IntegrationConfig.IntegrationConfigBuilder<?, ?>> B base(
            final B builder, final Integration integration) {
        builder.alias(integration.getAlias()).enabled(integration.isEnabled());
        return builder;
    }

    private static AwsConnectionConfig aws(final String region, final String endpoint,
                                           final String accessKeyId, final String secretAccessKey) {
        return AwsConnectionConfig.builder()
                .region(region)
                .endpoint(endpoint)
                .accessKeyId(accessKeyId)
                .secretAccessKey(secretAccessKey)
                .build();
    }
}
