package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Apis;
import com.knubisoft.testlum.testing.model.global_config.Auth;
import com.knubisoft.testlum.testing.model.global_config.AuthStrategies;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.ClickhouseIntegration;
import com.knubisoft.testlum.testing.model.global_config.Dynamo;
import com.knubisoft.testlum.testing.model.global_config.DynamoIntegration;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import com.knubisoft.testlum.testing.model.global_config.ElasticsearchIntegration;
import com.knubisoft.testlum.testing.model.global_config.GraphqlApi;
import com.knubisoft.testlum.testing.model.global_config.GraphqlIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import com.knubisoft.testlum.testing.model.global_config.KafkaIntegration;
import com.knubisoft.testlum.testing.model.global_config.Lambda;
import com.knubisoft.testlum.testing.model.global_config.LambdaIntegration;
import com.knubisoft.testlum.testing.model.global_config.Mongo;
import com.knubisoft.testlum.testing.model.global_config.MongoIntegration;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
import com.knubisoft.testlum.testing.model.global_config.MysqlIntegration;
import com.knubisoft.testlum.testing.model.global_config.Oracle;
import com.knubisoft.testlum.testing.model.global_config.OracleIntegration;
import com.knubisoft.testlum.testing.model.global_config.Postgres;
import com.knubisoft.testlum.testing.model.global_config.PostgresIntegration;
import com.knubisoft.testlum.testing.model.global_config.Rabbitmq;
import com.knubisoft.testlum.testing.model.global_config.RabbitmqIntegration;
import com.knubisoft.testlum.testing.model.global_config.Redis;
import com.knubisoft.testlum.testing.model.global_config.RedisIntegration;
import com.knubisoft.testlum.testing.model.global_config.S3;
import com.knubisoft.testlum.testing.model.global_config.S3Integration;
import com.knubisoft.testlum.testing.model.global_config.Sendgrid;
import com.knubisoft.testlum.testing.model.global_config.SendgridIntegration;
import com.knubisoft.testlum.testing.model.global_config.Ses;
import com.knubisoft.testlum.testing.model.global_config.SesIntegration;
import com.knubisoft.testlum.testing.model.global_config.Smtp;
import com.knubisoft.testlum.testing.model.global_config.SmtpIntegration;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import com.knubisoft.testlum.testing.model.global_config.SqsIntegration;
import com.knubisoft.testlum.testing.model.global_config.Twilio;
import com.knubisoft.testlum.testing.model.global_config.TwilioIntegration;
import com.knubisoft.testlum.testing.model.global_config.WebsocketApi;
import com.knubisoft.testlum.testing.model.global_config.Websockets;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_INTEGRATION_ALIASES;

@Slf4j
public class IntegrationsValidator {

    // CHECKSTYLE:OFF
    public void validateIntegrations(final Map<String, Integrations> integrations) {
        List<Integrations> integrationsList = new ArrayList<>(integrations.values());
        List<String> envsList = new ArrayList<>(integrations.keySet());
        validateApis(envsList, integrationsList);
        validateWebsockets(envsList, integrationsList);
        validateGraphql(envsList, integrationsList);
        validateSendGrid(envsList, integrationsList);
        validateElasticsearch(envsList, integrationsList);
        validateSmtp(envsList, integrationsList);
        validateTwilio(envsList, integrationsList);
        validateKafka(envsList, integrationsList);
        validateRabbitmq(envsList, integrationsList);
        validatePostgres(envsList, integrationsList);
        validateMysql(envsList, integrationsList);
        validateOracle(envsList, integrationsList);
        validateMongo(envsList, integrationsList);
        validateRedis(envsList, integrationsList);
        validateDynamo(envsList, integrationsList);
        validateClickhouse(envsList, integrationsList);
        validateS3(envsList, integrationsList);
        validateSqs(envsList, integrationsList);
        validateSes(envsList, integrationsList);
        validateLambda(envsList, integrationsList);
    }
    // CHECKSTYLE:ON

    private void validateApis(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Api>> apis = integrationsList.stream()
                .map(Integrations::getApis)
                .filter(Objects::nonNull)
                .map(Apis::getApi)
                .collect(Collectors.toList());
        if (apis.size() != 0) {
            checkAliasesDifference(envsList, apis);
            checkNumberOfIntegrations(envsList, apis, getIntegrationName(apis));
            checkAliasesSimilarity(envsList, apis, getIntegrationName(apis));
            checkApiAuth(envsList, apis, getIntegrationName(apis));
        }
    }

    private void validateWebsockets(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<WebsocketApi>> websockets = integrationsList.stream()
                .map(Integrations::getWebsockets)
                .filter(Objects::nonNull)
                .map(Websockets::getApi)
                .collect(Collectors.toList());
        if (websockets.size() != 0) {
            checkAliasesDifference(envsList, websockets);
            checkNumberOfIntegrations(envsList, websockets, getIntegrationName(websockets));
            checkAliasesSimilarity(envsList, websockets, getIntegrationName(websockets));
        }
    }

    private void validateGraphql(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<GraphqlApi>> graphqlIntegrations = integrationsList.stream()
                .map(Integrations::getGraphqlIntegration)
                .filter(Objects::nonNull)
                .map(GraphqlIntegration::getApi)
                .collect(Collectors.toList());
        if (graphqlIntegrations.size() != 0) {
            checkAliasesDifference(envsList, graphqlIntegrations);
            checkNumberOfIntegrations(envsList, graphqlIntegrations, getIntegrationName(graphqlIntegrations));
            checkAliasesSimilarity(envsList, graphqlIntegrations, getIntegrationName(graphqlIntegrations));
        }
    }

    private void validatePostgres(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Postgres>> postgresIntegrations = integrationsList.stream()
                .map(Integrations::getPostgresIntegration)
                .filter(Objects::nonNull)
                .map(PostgresIntegration::getPostgres)
                .collect(Collectors.toList());
        if (postgresIntegrations.size() != 0) {
            checkAliasesDifference(envsList, postgresIntegrations);
            checkNumberOfIntegrations(envsList, postgresIntegrations, getIntegrationName(postgresIntegrations));
            checkAliasesSimilarity(envsList, postgresIntegrations, getIntegrationName(postgresIntegrations));
        }
    }

    private void validateMysql(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Mysql>> mysqlIntegrations = integrationsList.stream()
                .map(Integrations::getMysqlIntegration)
                .filter(Objects::nonNull)
                .map(MysqlIntegration::getMysql)
                .collect(Collectors.toList());
        if (mysqlIntegrations.size() != 0) {
            checkAliasesDifference(envsList, mysqlIntegrations);
            checkNumberOfIntegrations(envsList, mysqlIntegrations, getIntegrationName(mysqlIntegrations));
            checkAliasesSimilarity(envsList, mysqlIntegrations, getIntegrationName(mysqlIntegrations));
        }
    }

    private void validateOracle(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Oracle>> oracleIntegrations = integrationsList.stream()
                .map(Integrations::getOracleIntegration)
                .filter(Objects::nonNull)
                .map(OracleIntegration::getOracle)
                .collect(Collectors.toList());
        if (oracleIntegrations.size() != 0) {
            checkAliasesDifference(envsList, oracleIntegrations);
            checkNumberOfIntegrations(envsList, oracleIntegrations, getIntegrationName(oracleIntegrations));
            checkAliasesSimilarity(envsList, oracleIntegrations, getIntegrationName(oracleIntegrations));
        }
    }

    private void validateMongo(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Mongo>> mongoIntegrations = integrationsList.stream()
                .map(Integrations::getMongoIntegration)
                .filter(Objects::nonNull)
                .map(MongoIntegration::getMongo)
                .collect(Collectors.toList());
        if (mongoIntegrations.size() != 0) {
            checkAliasesDifference(envsList, mongoIntegrations);
            checkNumberOfIntegrations(envsList, mongoIntegrations, getIntegrationName(mongoIntegrations));
            checkAliasesSimilarity(envsList, mongoIntegrations, getIntegrationName(mongoIntegrations));
        }
    }

    private void validateKafka(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Kafka>> kafkaIntegrations = integrationsList.stream()
                .map(Integrations::getKafkaIntegration)
                .filter(Objects::nonNull)
                .map(KafkaIntegration::getKafka)
                .collect(Collectors.toList());
        if (kafkaIntegrations.size() != 0) {
            checkAliasesDifference(envsList, kafkaIntegrations);
            checkNumberOfIntegrations(envsList, kafkaIntegrations, getIntegrationName(kafkaIntegrations));
            checkAliasesSimilarity(envsList, kafkaIntegrations, getIntegrationName(kafkaIntegrations));
        }
    }

    private void validateS3(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<S3>> s3Integrations = integrationsList.stream()
                .map(Integrations::getS3Integration)
                .filter(Objects::nonNull)
                .map(S3Integration::getS3)
                .collect(Collectors.toList());
        if (s3Integrations.size() != 0) {
            checkAliasesDifference(envsList, s3Integrations);
            checkNumberOfIntegrations(envsList, s3Integrations, getIntegrationName(s3Integrations));
            checkAliasesSimilarity(envsList, s3Integrations, getIntegrationName(s3Integrations));
        }
    }

    private void validateSqs(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Sqs>> sqsIntegrations = integrationsList.stream()
                .map(Integrations::getSqsIntegration)
                .filter(Objects::nonNull)
                .map(SqsIntegration::getSqs)
                .collect(Collectors.toList());
        if (sqsIntegrations.size() != 0) {
            checkAliasesDifference(envsList, sqsIntegrations);
            checkNumberOfIntegrations(envsList, sqsIntegrations, getIntegrationName(sqsIntegrations));
            checkAliasesSimilarity(envsList, sqsIntegrations, getIntegrationName(sqsIntegrations));
        }
    }

    private void validateRabbitmq(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Rabbitmq>> rabbitmqIntegrations = integrationsList.stream()
                .map(Integrations::getRabbitmqIntegration)
                .filter(Objects::nonNull)
                .map(RabbitmqIntegration::getRabbitmq)
                .collect(Collectors.toList());
        if (rabbitmqIntegrations.size() != 0) {
            checkAliasesDifference(envsList, rabbitmqIntegrations);
            checkNumberOfIntegrations(envsList, rabbitmqIntegrations, getIntegrationName(rabbitmqIntegrations));
            checkAliasesSimilarity(envsList, rabbitmqIntegrations, getIntegrationName(rabbitmqIntegrations));
        }
    }

    private void validateRedis(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Redis>> redisIntegrations = integrationsList.stream()
                .map(Integrations::getRedisIntegration)
                .filter(Objects::nonNull)
                .map(RedisIntegration::getRedis)
                .collect(Collectors.toList());
        if (redisIntegrations.size() != 0) {
            checkAliasesDifference(envsList, redisIntegrations);
            checkNumberOfIntegrations(envsList, redisIntegrations, getIntegrationName(redisIntegrations));
            checkAliasesSimilarity(envsList, redisIntegrations, getIntegrationName(redisIntegrations));
        }
    }

    private void validateSes(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Ses>> sesIntegrations = integrationsList.stream()
                .map(Integrations::getSesIntegration)
                .filter(Objects::nonNull)
                .map(SesIntegration::getSes)
                .collect(Collectors.toList());
        if (sesIntegrations.size() != 0) {
            checkAliasesDifference(envsList, sesIntegrations);
            checkNumberOfIntegrations(envsList, sesIntegrations, getIntegrationName(sesIntegrations));
            checkAliasesSimilarity(envsList, sesIntegrations, getIntegrationName(sesIntegrations));
        }
    }

    private void validateSendGrid(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Sendgrid>> sendgridIntegrations = integrationsList.stream()
                .map(Integrations::getSendgridIntegration)
                .filter(Objects::nonNull)
                .map(SendgridIntegration::getSendgrid)
                .collect(Collectors.toList());
        if (sendgridIntegrations.size() != 0) {
            checkAliasesDifference(envsList, sendgridIntegrations);
            checkNumberOfIntegrations(envsList, sendgridIntegrations, getIntegrationName(sendgridIntegrations));
            checkAliasesSimilarity(envsList, sendgridIntegrations, getIntegrationName(sendgridIntegrations));
        }
    }

    private void validateDynamo(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Dynamo>> dynamoIntegrations = integrationsList.stream()
                .map(Integrations::getDynamoIntegration)
                .filter(Objects::nonNull)
                .map(DynamoIntegration::getDynamo)
                .collect(Collectors.toList());
        if (dynamoIntegrations.size() != 0) {
            checkAliasesDifference(envsList, dynamoIntegrations);
            checkNumberOfIntegrations(envsList, dynamoIntegrations, getIntegrationName(dynamoIntegrations));
            checkAliasesSimilarity(envsList, dynamoIntegrations, getIntegrationName(dynamoIntegrations));
        }
    }

    private void validateElasticsearch(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Elasticsearch>> elasticsearchIntegrations = integrationsList.stream()
                .map(Integrations::getElasticsearchIntegration)
                .filter(Objects::nonNull)
                .map(ElasticsearchIntegration::getElasticsearch)
                .collect(Collectors.toList());
        if (elasticsearchIntegrations.size() != 0) {
            checkAliasesDifference(envsList, elasticsearchIntegrations);
            checkNumberOfIntegrations(envsList, elasticsearchIntegrations,
                    getIntegrationName(elasticsearchIntegrations));
            checkAliasesSimilarity(envsList, elasticsearchIntegrations, getIntegrationName(elasticsearchIntegrations));
        }
    }

    private void validateClickhouse(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Clickhouse>> clickhouseIntegrations = integrationsList.stream()
                .map(Integrations::getClickhouseIntegration)
                .filter(Objects::nonNull)
                .map(ClickhouseIntegration::getClickhouse)
                .collect(Collectors.toList());
        if (clickhouseIntegrations.size() != 0) {
            checkAliasesDifference(envsList, clickhouseIntegrations);
            checkNumberOfIntegrations(envsList, clickhouseIntegrations, getIntegrationName(clickhouseIntegrations));
            checkAliasesSimilarity(envsList, clickhouseIntegrations, getIntegrationName(clickhouseIntegrations));
        }
    }

    private void validateSmtp(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Smtp>> smtpIntegrations = integrationsList.stream()
                .map(Integrations::getSmtpIntegration)
                .filter(Objects::nonNull)
                .map(SmtpIntegration::getSmtp)
                .collect(Collectors.toList());
        if (smtpIntegrations.size() != 0) {
            checkAliasesDifference(envsList, smtpIntegrations);
            checkNumberOfIntegrations(envsList, smtpIntegrations, getIntegrationName(smtpIntegrations));
            checkAliasesSimilarity(envsList, smtpIntegrations, getIntegrationName(smtpIntegrations));
        }
    }

    private void validateTwilio(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Twilio>> twilioIntegrations = integrationsList.stream()
                .map(Integrations::getTwilioIntegration)
                .filter(Objects::nonNull)
                .map(TwilioIntegration::getTwilio)
                .collect(Collectors.toList());
        if (twilioIntegrations.size() != 0) {
            checkAliasesDifference(envsList, twilioIntegrations);
            checkNumberOfIntegrations(envsList, twilioIntegrations, getIntegrationName(twilioIntegrations));
            checkAliasesSimilarity(envsList, twilioIntegrations, getIntegrationName(twilioIntegrations));
        }
    }

    private void validateLambda(final List<String> envsList, final List<Integrations> integrationsList) {
        List<List<Lambda>> lambdaIntegrations = integrationsList.stream()
                .map(Integrations::getLambdaIntegration)
                .filter(Objects::nonNull)
                .map(LambdaIntegration::getLambda)
                .collect(Collectors.toList());
        if (lambdaIntegrations.size() != 0) {
            checkAliasesDifference(envsList, lambdaIntegrations);
            checkNumberOfIntegrations(envsList, lambdaIntegrations, getIntegrationName(lambdaIntegrations));
            checkAliasesSimilarity(envsList, lambdaIntegrations, getIntegrationName(lambdaIntegrations));
        }
    }

    private void checkApiAuth(final List<String> envsList,
                              final List<List<Api>> apiList,
                              final String integrationName) {
        List<Api> apis = apiList.stream().findFirst().get();
        for (int envNum = 0; envNum < apis.size(); envNum++) {
            List<Auth> authList = getAuthList(apiList, envNum);
            if (checkAuthPresence(apiList, integrationName, authList)) {
                checkAutoLogout(envsList, authList, integrationName);
                checkAuthStrategy(envsList, authList, integrationName);
                checkAuthCustomClassName(envsList, authList, integrationName);
            }
        }
    }

    private List<Auth> getAuthList(final List<List<Api>> apiList, final int envNum) {
        return apiList.stream()
                .map(api -> api.get(envNum))
                .filter(Integration::isEnabled)
                .map(Api::getAuth)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void checkAuthStrategy(final List<String> envsList,
                                   final List<Auth> authList,
                                   final String integrationName) {
        AuthStrategies authStrategy = authList.stream().findFirst().get().getAuthStrategy();
        for (int envNum = 0; envNum < authList.size(); envNum++) {
            if (authList.get(envNum).getAuthStrategy() != authStrategy) {
                throw new DefaultFrameworkException(
                        "Every single <" + integrationName + "> integration must have the same setup for <authStrategy>"
                                + " argument inside <auth> command in all configs"
                                + "Invalid config: " + envsList.get(envNum) + "\n");
            }
        }
    }

    private void checkAutoLogout(final List<String> envsList,
                                 final List<Auth> authList,
                                 final String integrationName) {
        boolean autoLogout = authList.stream().findFirst().get().isAutoLogout();
        for (int envNum = 0; envNum < authList.size(); envNum++) {
            if (authList.get(envNum).isAutoLogout() != autoLogout) {
                throw new DefaultFrameworkException("Every single <" + integrationName + "> integration must have the"
                        + " same setup for <autoLogout> argument inside <auth> command in all configs"
                        + "Invalid config: " + envsList.get(envNum) + "\n");
            }
        }
    }

    private void checkAuthCustomClassName(final List<String> envsList,
                                          final List<Auth> authList,
                                          final String integrationName) {
        String authCustomClassName = authList.stream().findFirst().get().getAuthCustomClassName();
        for (int envNum = 0; envNum < authList.size(); envNum++) {
            if ((Objects.isNull(authList.get(envNum).getAuthCustomClassName()) && Objects.nonNull(authCustomClassName))
                    || !Objects.equals(authList.get(envNum).getAuthCustomClassName(), authCustomClassName)) {
                throw new DefaultFrameworkException("Every single <" + integrationName + "> integration must have the"
                        + " same setup for <authCustomClassName> argument inside <auth> command in all configs"
                        + "Invalid config: " + envsList.get(envNum) + "\n");
            }
        }
    }

    private boolean checkAuthPresence(final List<List<Api>> apiList,
                                      final String integrationName,
                                      final List<Auth> authList) {
        if (authList.size() == 0) {
            return false;
        } else if (authList.size() != apiList.size()) {
            throw new DefaultFrameworkException("Every single <" + integrationName + "> integration must have the same"
                    + " setup for <auth> command in all configs");
        }
        return true;
    }

    private String getIntegrationName(final List<? extends List<? extends Integration>> integrationsList) {
        List<? extends Integration> integrations = integrationsList.stream().findFirst().get();
        Integration integration = integrations.stream().findFirst().get();
        return integration.getClass().getSimpleName();
    }

    private void checkNumberOfIntegrations(final List<String> envsList,
                                           final List<? extends List<? extends Integration>> integrationsList,
                                           final String integrationName) {
        long numEnabledIntegrations = -1;
        boolean isFirstEnvIntegration = true;
        for (int envNum = 0; envNum < integrationsList.size(); envNum++) {
            long numOfIntegrations = getNumOfIntegrations(integrationsList.get(envNum));
            if (isFirstEnvIntegration) {
                numEnabledIntegrations = numOfIntegrations;
                isFirstEnvIntegration = false;
            } else if (numOfIntegrations != numEnabledIntegrations) {
                throw new DefaultFrameworkException(
                        "Every single <" + integrationName + "> integration must be either enabled or disabled in all"
                                + " configs\n" + "Invalid config: " + envsList.get(envNum) + "\n");
            }
        }
    }

    private long getNumOfIntegrations(final List<? extends Integration> integrationsList) {
        return integrationsList.stream()
                .filter(Integration::isEnabled)
                .count();
    }

    private void checkAliasesSimilarity(final List<String> envsList,
                                        final List<? extends List<? extends Integration>> integrationsList,
                                        final String integrationName) {
        List<? extends Integration> integrations = integrationsList.stream().findFirst().get();
        for (int envNum = 0; envNum < integrations.size(); envNum++) {
            List<String> aliases = getAliases(integrationsList, envNum);
            if (aliases.stream().distinct().count() > 1) {
                throw new DefaultFrameworkException("Every single <" + integrationName + "> integration must have the"
                        + " same <alias> argument in all configs.\n"
                        + "Invalid config: " + envsList.get(envNum) + "\n"
                        + "Invalid aliases: " + String.join(", ", aliases));
            }
        }
    }

    private List<String> getAliases(final List<? extends List<? extends Integration>> integrations,
                                    final int envNum) {
        return integrations.stream()
                .map(integration -> integration.get(envNum))
                .filter(Integration::isEnabled)
                .map(Integration::getAlias)
                .collect(Collectors.toList());
    }

    private void checkAliasesDifference(final List<String> envsList,
                                        final List<? extends List<? extends Integration>> integrationList) {
        for (int envNum = 0; envNum < integrationList.size(); envNum++) {
            Set<String> aliasSet = new HashSet<>();
            for (Integration integration : integrationList.get(envNum)) {
                if (!aliasSet.add(integration.getAlias())) {
                    throw new DefaultFrameworkException(SAME_INTEGRATION_ALIASES,
                            integration.getClass().getSimpleName(), integration.getAlias(), envsList.get(envNum));
                }
            }
        }
    }
}
