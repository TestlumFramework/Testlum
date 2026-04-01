package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.UIConfiguration;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.SendGridUtil;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.util.HttpUtil;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.DatasetValidator;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.framework.util.MobileUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.framework.xml.XMLValidator;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Apis;
import com.knubisoft.testlum.testing.model.global_config.ClickhouseIntegration;
import com.knubisoft.testlum.testing.model.global_config.DynamoIntegration;
import com.knubisoft.testlum.testing.model.global_config.ElasticsearchIntegration;
import com.knubisoft.testlum.testing.model.global_config.GraphqlIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.KafkaIntegration;
import com.knubisoft.testlum.testing.model.global_config.LambdaIntegration;
import com.knubisoft.testlum.testing.model.global_config.MongoIntegration;
import com.knubisoft.testlum.testing.model.global_config.MysqlIntegration;
import com.knubisoft.testlum.testing.model.global_config.OracleIntegration;
import com.knubisoft.testlum.testing.model.global_config.PostgresIntegration;
import com.knubisoft.testlum.testing.model.global_config.RabbitmqIntegration;
import com.knubisoft.testlum.testing.model.global_config.RedisIntegration;
import com.knubisoft.testlum.testing.model.global_config.S3Integration;
import com.knubisoft.testlum.testing.model.global_config.SendgridIntegration;
import com.knubisoft.testlum.testing.model.global_config.SesIntegration;
import com.knubisoft.testlum.testing.model.global_config.SmtpIntegration;
import com.knubisoft.testlum.testing.model.global_config.SqsIntegration;
import com.knubisoft.testlum.testing.model.global_config.TwilioIntegration;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Websockets;
import com.knubisoft.testlum.testing.model.scenario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ScenarioValidator} covering validation logic,
 * command dispatching, integration existence checks, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class ScenarioValidatorTest {

    @Mock
    private Integrations integrations;
    @Mock
    private MobileUtil mobileUtil;
    @Mock
    private IntegrationsUtil integrationsUtil;
    @Mock
    private TestResourceSettings testResourceSettings;
    @Mock
    private HttpUtil httpUtil;
    @Mock
    private FileSearcher fileSearcher;
    @Mock
    private SendGridUtil sendGridUtil;
    @Mock
    private BrowserUtil browserUtil;
    @Mock
    private DatasetValidator datasetValidator;
    @Mock
    private GlobalVariationsProvider globalVariationsProvider;
    @Mock
    private UiConfig uiConfig;
    @Mock
    private UIConfiguration uiConfigs;

    private ScenarioValidator validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new ScenarioValidator(
                integrations, mobileUtil, integrationsUtil, testResourceSettings,
                httpUtil, fileSearcher, sendGridUtil, browserUtil,
                datasetValidator, globalVariationsProvider, uiConfig, uiConfigs
        );
        validator.init();
    }

    private Scenario createScenario(final boolean active, final AbstractCommand... commands) {
        Scenario scenario = new Scenario();
        Settings settings = new Settings();
        settings.setActive(active);
        scenario.setSettings(settings);
        for (AbstractCommand cmd : commands) {
            scenario.getCommands().add(cmd);
        }
        return scenario;
    }

    private File xmlFile() {
        return new File("/tmp/test/scenario.xml");
    }

    @Nested
    class ClassStructure {

        @Test
        void implementsXMLValidator() {
            assertTrue(XMLValidator.class.isAssignableFrom(ScenarioValidator.class));
        }

        @Test
        void classIsAnnotatedWithComponent() {
            assertNotNull(ScenarioValidator.class.getAnnotation(
                    org.springframework.stereotype.Component.class));
        }

        @Test
        void initMethodExists() throws NoSuchMethodException {
            assertNotNull(ScenarioValidator.class.getDeclaredMethod("init"));
        }

        @Test
        void validateMethodExists() throws NoSuchMethodException {
            assertNotNull(ScenarioValidator.class.getDeclaredMethod("validate",
                    Scenario.class, java.io.File.class));
        }
    }

    @Nested
    class ValidateMethod {

        @Test
        void inactiveScenarioSkipsValidation() {
            Scenario scenario = createScenario(false, new Postgres());
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
            verifyNoInteractions(globalVariationsProvider, mobileUtil);
        }

        @Test
        void activeScenarioWithEmptyCommandsDoesNotThrow() {
            Scenario scenario = createScenario(true);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void activeScenarioWithVariationsCallsProvider() {
            Scenario scenario = createScenario(true);
            scenario.getSettings().setVariations("vars.csv");
            when(globalVariationsProvider.getVariations("vars.csv"))
                    .thenReturn(List.of(Map.of("key", "val")));

            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(globalVariationsProvider).process(scenario, xmlFile());
            verify(globalVariationsProvider).getVariations("vars.csv");
        }

        @Test
        void activeScenarioWithNullVariationsDoesNotCallProvider() {
            Scenario scenario = createScenario(true);
            scenario.getSettings().setVariations(null);

            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(globalVariationsProvider, never()).process(any(Scenario.class), any(File.class));
        }

        @Test
        void activeScenarioWithBlankVariationsDoesNotCallProvider() {
            Scenario scenario = createScenario(true);
            scenario.getSettings().setVariations("   ");

            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(globalVariationsProvider, never()).process(any(Scenario.class), any(File.class));
        }
    }

    @Nested
    class CheckIntegrationExistence {

        @Test
        void throwsWhenIntegrationIsNull() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "checkIntegrationExistence", Object.class, Class.class);
            method.setAccessible(true);

            Exception ex = assertThrows(Exception.class,
                    () -> method.invoke(validator, null, Apis.class));
            assertTrue(ex.getCause() instanceof DefaultFrameworkException);
            assertTrue(ex.getCause().getMessage().contains("Apis"));
        }

        @Test
        void doesNotThrowWhenIntegrationIsPresent() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "checkIntegrationExistence", Object.class, Class.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> method.invoke(validator, new Apis(), Apis.class));
        }
    }

    @Nested
    class ValidatePostgresCommand {

        @Test
        void validatesPostgresIntegrationAndAlias() {
            PostgresIntegration pgInt = new PostgresIntegration();
            when(integrations.getPostgresIntegration()).thenReturn(pgInt);

            Postgres pg = new Postgres();
            pg.setAlias("pgAlias");
            pg.setFile("result.json");

            Scenario scenario = createScenario(true, pg);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(pgInt.getPostgres(), "pgAlias");
        }

        @Test
        void throwsWhenPostgresIntegrationIsNull() {
            when(integrations.getPostgresIntegration()).thenReturn(null);

            Postgres pg = new Postgres();
            pg.setAlias("pgAlias");
            pg.setFile("result.json");

            Scenario scenario = createScenario(true, pg);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateMysqlCommand {

        @Test
        void validatesMysqlIntegrationAndAlias() {
            MysqlIntegration mysqlInt = new MysqlIntegration();
            when(integrations.getMysqlIntegration()).thenReturn(mysqlInt);

            Mysql mysql = new Mysql();
            mysql.setAlias("mysqlAlias");
            mysql.setFile("result.json");

            Scenario scenario = createScenario(true, mysql);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(mysqlInt.getMysql(), "mysqlAlias");
        }

        @Test
        void throwsWhenMysqlIntegrationIsNull() {
            when(integrations.getMysqlIntegration()).thenReturn(null);

            Mysql mysql = new Mysql();
            mysql.setAlias("alias");
            mysql.setFile("file.json");

            Scenario scenario = createScenario(true, mysql);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateOracleCommand {

        @Test
        void validatesOracleIntegration() {
            OracleIntegration oracleInt = new OracleIntegration();
            when(integrations.getOracleIntegration()).thenReturn(oracleInt);

            Oracle oracle = new Oracle();
            oracle.setAlias("oracleAlias");
            oracle.setFile("result.json");

            Scenario scenario = createScenario(true, oracle);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(oracleInt.getOracle(), "oracleAlias");
        }

        @Test
        void throwsWhenOracleIntegrationIsNull() {
            when(integrations.getOracleIntegration()).thenReturn(null);

            Oracle oracle = new Oracle();
            oracle.setAlias("alias");
            oracle.setFile("file.json");

            Scenario scenario = createScenario(true, oracle);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateClickhouseCommand {

        @Test
        void validatesClickhouseIntegration() {
            ClickhouseIntegration chInt = new ClickhouseIntegration();
            when(integrations.getClickhouseIntegration()).thenReturn(chInt);

            Clickhouse ch = new Clickhouse();
            ch.setAlias("chAlias");
            ch.setFile("result.json");

            Scenario scenario = createScenario(true, ch);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(chInt.getClickhouse(), "chAlias");
        }

        @Test
        void throwsWhenClickhouseIntegrationIsNull() {
            when(integrations.getClickhouseIntegration()).thenReturn(null);

            Clickhouse ch = new Clickhouse();
            ch.setAlias("alias");
            ch.setFile("file.json");

            Scenario scenario = createScenario(true, ch);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateRedisCommand {

        @Test
        void validatesRedisIntegration() {
            RedisIntegration redisInt = new RedisIntegration();
            when(integrations.getRedisIntegration()).thenReturn(redisInt);

            Redis redis = new Redis();
            redis.setAlias("redisAlias");
            redis.setFile("result.json");

            Scenario scenario = createScenario(true, redis);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(redisInt.getRedis(), "redisAlias");
        }

        @Test
        void throwsWhenRedisIntegrationIsNull() {
            when(integrations.getRedisIntegration()).thenReturn(null);

            Redis redis = new Redis();
            redis.setAlias("alias");
            redis.setFile("file.json");

            Scenario scenario = createScenario(true, redis);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateMongoCommand {

        @Test
        void validatesMongoIntegration() {
            MongoIntegration mongoInt = new MongoIntegration();
            when(integrations.getMongoIntegration()).thenReturn(mongoInt);

            Mongo mongo = new Mongo();
            mongo.setAlias("mongoAlias");
            mongo.setFile("result.json");

            Scenario scenario = createScenario(true, mongo);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(mongoInt.getMongo(), "mongoAlias");
        }

        @Test
        void throwsWhenMongoIntegrationIsNull() {
            when(integrations.getMongoIntegration()).thenReturn(null);

            Mongo mongo = new Mongo();
            mongo.setAlias("alias");
            mongo.setFile("file.json");

            Scenario scenario = createScenario(true, mongo);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateDynamoCommand {

        @Test
        void validatesDynamoIntegration() {
            DynamoIntegration dynamoInt = new DynamoIntegration();
            when(integrations.getDynamoIntegration()).thenReturn(dynamoInt);

            Dynamo dynamo = new Dynamo();
            dynamo.setAlias("dynamoAlias");
            dynamo.setFile("result.json");

            Scenario scenario = createScenario(true, dynamo);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(dynamoInt.getDynamo(), "dynamoAlias");
        }

        @Test
        void throwsWhenDynamoIntegrationIsNull() {
            when(integrations.getDynamoIntegration()).thenReturn(null);

            Dynamo dynamo = new Dynamo();
            dynamo.setAlias("alias");
            dynamo.setFile("file.json");

            Scenario scenario = createScenario(true, dynamo);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateHttpCommand {

        @Test
        void validatesHttpIntegrationExistence() {
            Apis apis = new Apis();
            when(integrations.getApis()).thenReturn(apis);

            Http http = new Http();
            http.setAlias("apiAlias");
            Get getMethod = new Get();
            http.setGet(getMethod);

            HttpInfo httpInfo = mock(HttpInfo.class);
            when(httpInfo.getResponse()).thenReturn(null);
            HttpUtil.HttpMethodMetadata metadata = new HttpUtil.HttpMethodMetadata(httpInfo, null);
            when(httpUtil.getHttpMethodMetadata(http)).thenReturn(metadata);

            Scenario scenario = createScenario(true, http);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findApiForAlias(apis.getApi(), "apiAlias");
        }

        @Test
        void throwsWhenApisIsNull() {
            when(integrations.getApis()).thenReturn(null);

            Http http = new Http();
            http.setAlias("apiAlias");

            Scenario scenario = createScenario(true, http);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void validatesResponseFileIfPresent() {
            Apis apis = new Apis();
            when(integrations.getApis()).thenReturn(apis);

            Http http = new Http();
            http.setAlias("apiAlias");

            Response response = new Response();
            response.setFile("expected.json");
            HttpInfo httpInfo = mock(HttpInfo.class);
            when(httpInfo.getResponse()).thenReturn(response);
            HttpUtil.HttpMethodMetadata metadata = new HttpUtil.HttpMethodMetadata(httpInfo, null);
            when(httpUtil.getHttpMethodMetadata(http)).thenReturn(metadata);

            Scenario scenario = createScenario(true, http);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateAuthCommand {

        @Test
        void throwsWhenApisIsNullForAuth() {
            when(integrations.getApis()).thenReturn(null);

            Auth auth = new Auth();
            auth.setApiAlias("api1");
            auth.setCredentials("creds.json");

            Scenario scenario = createScenario(true, auth);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void throwsWhenApiAuthConfigIsNull() {
            Apis apis = new Apis();
            when(integrations.getApis()).thenReturn(apis);

            Api apiObj = mock(Api.class);
            when(apiObj.getAuth()).thenReturn(null);
            when(apiObj.getAlias()).thenReturn("api1");
            when(integrationsUtil.findApiForAlias(apis.getApi(), "api1")).thenReturn(apiObj);
            when(fileSearcher.searchFileFromDataFolder("creds.json"))
                    .thenReturn(new File("creds.json"));

            Auth auth = new Auth();
            auth.setApiAlias("api1");
            auth.setCredentials("creds.json");

            Scenario scenario = createScenario(true, auth);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateGraphqlCommand {

        @Test
        void validatesGraphqlIntegration() {
            GraphqlIntegration gqlInt = new GraphqlIntegration();
            when(integrations.getGraphqlIntegration()).thenReturn(gqlInt);

            Graphql gql = new Graphql();
            gql.setAlias("gqlAlias");

            Scenario scenario = createScenario(true, gql);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(gqlInt.getApi(), "gqlAlias");
        }

        @Test
        void throwsWhenGraphqlIntegrationIsNull() {
            when(integrations.getGraphqlIntegration()).thenReturn(null);

            Graphql gql = new Graphql();
            gql.setAlias("alias");

            Scenario scenario = createScenario(true, gql);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateWebsocketCommand {

        @Test
        void validatesWebsocketIntegration() {
            Websockets wsInt = new Websockets();
            when(integrations.getWebsockets()).thenReturn(wsInt);

            Websocket ws = new Websocket();
            ws.setAlias("wsAlias");

            Scenario scenario = createScenario(true, ws);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(wsInt.getApi(), "wsAlias");
        }

        @Test
        void throwsWhenWebsocketIntegrationIsNull() {
            when(integrations.getWebsockets()).thenReturn(null);

            Websocket ws = new Websocket();
            ws.setAlias("alias");

            Scenario scenario = createScenario(true, ws);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateElasticsearchCommand {

        @Test
        void validatesElasticsearchIntegration() {
            ElasticsearchIntegration esInt = new ElasticsearchIntegration();
            when(integrations.getElasticsearchIntegration()).thenReturn(esInt);

            Elasticsearch es = new Elasticsearch();
            es.setAlias("esAlias");

            ElasticSearchRequest esReq = mock(ElasticSearchRequest.class);
            when(esReq.getResponse()).thenReturn(null);
            HttpUtil.ESHttpMethodMetadata esMeta = new HttpUtil.ESHttpMethodMetadata(esReq, null);
            when(httpUtil.getESHttpMethodMetadata(es)).thenReturn(esMeta);

            Scenario scenario = createScenario(true, es);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(esInt.getElasticsearch(), "esAlias");
        }

        @Test
        void throwsWhenElasticsearchIntegrationIsNull() {
            when(integrations.getElasticsearchIntegration()).thenReturn(null);

            Elasticsearch es = new Elasticsearch();
            es.setAlias("alias");

            Scenario scenario = createScenario(true, es);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateRabbitCommand {

        @Test
        void validatesRabbitIntegration() {
            RabbitmqIntegration rmqInt = new RabbitmqIntegration();
            when(integrations.getRabbitmqIntegration()).thenReturn(rmqInt);

            Rabbit rabbit = new Rabbit();
            rabbit.setAlias("rmqAlias");

            Scenario scenario = createScenario(true, rabbit);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(rmqInt.getRabbitmq(), "rmqAlias");
        }

        @Test
        void throwsWhenRabbitIntegrationIsNull() {
            when(integrations.getRabbitmqIntegration()).thenReturn(null);

            Rabbit rabbit = new Rabbit();
            rabbit.setAlias("alias");

            Scenario scenario = createScenario(true, rabbit);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateKafkaCommand {

        @Test
        void validatesKafkaIntegration() {
            KafkaIntegration kafkaInt = new KafkaIntegration();
            when(integrations.getKafkaIntegration()).thenReturn(kafkaInt);

            Kafka kafka = new Kafka();
            kafka.setAlias("kafkaAlias");

            Scenario scenario = createScenario(true, kafka);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(kafkaInt.getKafka(), "kafkaAlias");
        }

        @Test
        void throwsWhenKafkaIntegrationIsNull() {
            when(integrations.getKafkaIntegration()).thenReturn(null);

            Kafka kafka = new Kafka();
            kafka.setAlias("alias");

            Scenario scenario = createScenario(true, kafka);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateSqsCommand {

        @Test
        void validatesSqsIntegration() {
            SqsIntegration sqsInt = new SqsIntegration();
            when(integrations.getSqsIntegration()).thenReturn(sqsInt);

            Sqs sqs = new Sqs();
            sqs.setAlias("sqsAlias");

            Scenario scenario = createScenario(true, sqs);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(sqsInt.getSqs(), "sqsAlias");
        }

        @Test
        void throwsWhenSqsIntegrationIsNull() {
            when(integrations.getSqsIntegration()).thenReturn(null);

            Sqs sqs = new Sqs();
            sqs.setAlias("alias");

            Scenario scenario = createScenario(true, sqs);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateS3Command {

        @Test
        void validatesS3Integration() {
            S3Integration s3Int = new S3Integration();
            when(integrations.getS3Integration()).thenReturn(s3Int);

            S3 s3 = new S3();
            s3.setAlias("s3Alias");

            Scenario scenario = createScenario(true, s3);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(s3Int.getS3(), "s3Alias");
        }

        @Test
        void throwsWhenS3IntegrationIsNull() {
            when(integrations.getS3Integration()).thenReturn(null);

            S3 s3 = new S3();
            s3.setAlias("alias");

            Scenario scenario = createScenario(true, s3);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateLambdaCommand {

        @Test
        void validatesLambdaIntegration() {
            LambdaIntegration lambdaInt = new LambdaIntegration();
            when(integrations.getLambdaIntegration()).thenReturn(lambdaInt);

            Lambda lambda = new Lambda();
            lambda.setAlias("lambdaAlias");

            Scenario scenario = createScenario(true, lambda);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(lambdaInt.getLambda(), "lambdaAlias");
        }

        @Test
        void throwsWhenLambdaIntegrationIsNull() {
            when(integrations.getLambdaIntegration()).thenReturn(null);

            Lambda lambda = new Lambda();
            lambda.setAlias("alias");

            Scenario scenario = createScenario(true, lambda);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateSesCommand {

        @Test
        void validatesSesIntegration() {
            SesIntegration sesInt = new SesIntegration();
            when(integrations.getSesIntegration()).thenReturn(sesInt);

            Ses ses = new Ses();
            ses.setAlias("sesAlias");

            Scenario scenario = createScenario(true, ses);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(sesInt.getSes(), "sesAlias");
        }

        @Test
        void throwsWhenSesIntegrationIsNull() {
            when(integrations.getSesIntegration()).thenReturn(null);

            Ses ses = new Ses();
            ses.setAlias("alias");

            Scenario scenario = createScenario(true, ses);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateSmtpCommand {

        @Test
        void validatesSmtpIntegration() {
            SmtpIntegration smtpInt = new SmtpIntegration();
            when(integrations.getSmtpIntegration()).thenReturn(smtpInt);

            Smtp smtp = new Smtp();
            smtp.setAlias("smtpAlias");

            Scenario scenario = createScenario(true, smtp);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(smtpInt.getSmtp(), "smtpAlias");
        }

        @Test
        void throwsWhenSmtpIntegrationIsNull() {
            when(integrations.getSmtpIntegration()).thenReturn(null);

            Smtp smtp = new Smtp();
            smtp.setAlias("alias");

            Scenario scenario = createScenario(true, smtp);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateSendgridCommand {

        @Test
        void validatesSendgridIntegration() {
            SendgridIntegration sgInt = new SendgridIntegration();
            when(integrations.getSendgridIntegration()).thenReturn(sgInt);

            Sendgrid sg = new Sendgrid();
            sg.setAlias("sgAlias");

            SendgridInfo sgInfo = mock(SendgridInfo.class);
            when(sgInfo.getResponse()).thenReturn(null);
            SendGridUtil.SendGridMethodMetadata sgMeta =
                    new SendGridUtil.SendGridMethodMetadata(sgInfo, null);
            when(sendGridUtil.getSendgridMethodMetadata(sg)).thenReturn(sgMeta);

            Scenario scenario = createScenario(true, sg);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(sgInt.getSendgrid(), "sgAlias");
        }

        @Test
        void throwsWhenSendgridIntegrationIsNull() {
            when(integrations.getSendgridIntegration()).thenReturn(null);

            Sendgrid sg = new Sendgrid();
            sg.setAlias("alias");

            Scenario scenario = createScenario(true, sg);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateTwilioCommand {

        @Test
        void validatesTwilioIntegration() {
            TwilioIntegration twilioInt = new TwilioIntegration();
            when(integrations.getTwilioIntegration()).thenReturn(twilioInt);

            Twilio twilio = new Twilio();
            twilio.setAlias("twilioAlias");

            Scenario scenario = createScenario(true, twilio);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(twilioInt.getTwilio(), "twilioAlias");
        }

        @Test
        void throwsWhenTwilioIntegrationIsNull() {
            when(integrations.getTwilioIntegration()).thenReturn(null);

            Twilio twilio = new Twilio();
            twilio.setAlias("alias");

            Scenario scenario = createScenario(true, twilio);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateShellCommand {

        @Test
        void validatesShellFileIfExist() {
            Shell shell = new Shell();
            shell.setFile("script.sh");

            Scenario scenario = createScenario(true, shell);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void validatesShellFilesFromDataFolder() {
            when(fileSearcher.searchFileFromDataFolder("setup.sh"))
                    .thenReturn(new File("setup.sh"));
            when(fileSearcher.searchFileFromDataFolder("teardown.sh"))
                    .thenReturn(new File("teardown.sh"));

            Shell shell = new Shell();
            shell.setFile(null);
            shell.getShellFile().add("setup.sh");
            shell.getShellFile().add("teardown.sh");

            Scenario scenario = createScenario(true, shell);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateIncludeCommand {

        @Test
        void validatesIncludeScenario() {
            File scenariosFolder = new File("/tmp/scenarios");
            when(testResourceSettings.getScenariosFolder()).thenReturn(scenariosFolder);

            File includedFile = new File("/tmp/scenarios/included/scenario.xml");
            when(fileSearcher.searchFileFromDir(any(File.class), eq(TestResourceSettings.SCENARIO_FILENAME)))
                    .thenReturn(includedFile);

            Include include = new Include();
            include.setScenario("included");

            Scenario scenario = createScenario(true, include);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void throwsWhenIncludingSelf() {
            File xml = new File("/tmp/scenarios/self/scenario.xml");
            File scenariosFolder = new File("/tmp/scenarios");
            when(testResourceSettings.getScenariosFolder()).thenReturn(scenariosFolder);
            when(fileSearcher.searchFileFromDir(any(File.class), eq(TestResourceSettings.SCENARIO_FILENAME)))
                    .thenReturn(xml);

            Include include = new Include();
            include.setScenario("self");

            Scenario scenario = createScenario(true, include);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xml));
        }

        @Test
        void blankScenarioNameDoesNotValidate() {
            Include include = new Include();
            include.setScenario("");

            Scenario scenario = createScenario(true, include);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verifyNoInteractions(testResourceSettings, fileSearcher);
        }

        @Test
        void nullScenarioNameDoesNotValidate() {
            Include include = new Include();
            include.setScenario(null);

            Scenario scenario = createScenario(true, include);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verifyNoInteractions(testResourceSettings, fileSearcher);
        }
    }

    @Nested
    class ValidateVarCommand {

        @Test
        void varWithNullFileAndNullSqlDoesNotThrow() {
            Var var = new Var();
            var.setName("myVar");
            var.setFile(null);
            var.setSql(null);

            Scenario scenario = createScenario(true, var);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void varWithFileValidatesFileExistence() {
            FromFile fromFile = new FromFile();
            fromFile.setFileName("data.json");

            Var var = new Var();
            var.setName("myVar");
            var.setFile(fromFile);

            Scenario scenario = createScenario(true, var);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void varWithSqlValidatesAlias() {
            PostgresIntegration pgInt = new PostgresIntegration();
            when(integrations.getPostgresIntegration()).thenReturn(pgInt);

            FromSQL fromSQL = new FromSQL();
            fromSQL.setDbType(RelationalDB.POSTGRES);
            fromSQL.setAlias("pgAlias");

            Var var = new Var();
            var.setName("myVar");
            var.setSql(fromSQL);

            Scenario scenario = createScenario(true, var);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateRepeatCommand {

        @Test
        void repeatWithBlankVariationsDoesNotCallProvider() {
            Repeat repeat = new Repeat();
            repeat.setVariations("");

            Scenario scenario = createScenario(true, repeat);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(globalVariationsProvider, never()).process(anyString());
        }

        @Test
        void repeatWithNullVariationsDoesNotCallProvider() {
            Repeat repeat = new Repeat();
            repeat.setVariations(null);

            Scenario scenario = createScenario(true, repeat);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(globalVariationsProvider, never()).process(anyString());
        }

        @Test
        void repeatWithVariationsProcessesThem() {
            when(globalVariationsProvider.getVariations("repeat_vars.csv"))
                    .thenReturn(List.of(Map.of("k", "v")));

            Repeat repeat = new Repeat();
            repeat.setVariations("repeat_vars.csv");

            Scenario scenario = createScenario(true, repeat);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(globalVariationsProvider).process("repeat_vars.csv");
            verify(globalVariationsProvider).getVariations("repeat_vars.csv");
        }
    }

    @Nested
    class ValidateMigrateCommand {

        @Test
        void validatesDatasetFiles() {
            when(fileSearcher.searchFileFromDataFolder("data.json"))
                    .thenReturn(new File("data.json"));

            Migrate migrate = new Migrate();
            migrate.setName(StorageName.POSTGRES);
            migrate.getDataset().add("data.json");

            Scenario scenario = createScenario(true, migrate);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(datasetValidator).validateDatasetByExtension("data.json", StorageName.POSTGRES);
        }
    }

    @Nested
    class ValidateWebCommands {

        @Test
        void throwsWhenNoBrowsersEnabled() {
            when(browserUtil.filterDefaultEnabledBrowsers()).thenReturn(Collections.emptyList());

            Web web = new Web();

            Scenario scenario = createScenario(true, web);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void throwsWhenWebNotEnabled() {
            com.knubisoft.testlum.testing.model.global_config.Web webConfig =
                    mock(com.knubisoft.testlum.testing.model.global_config.Web.class);
            when(webConfig.isEnabled()).thenReturn(false);
            when(uiConfig.getWeb()).thenReturn(webConfig);
            when(browserUtil.filterDefaultEnabledBrowsers()).thenReturn(List.of(mock(
                    com.knubisoft.testlum.testing.model.global_config.AbstractBrowser.class)));

            Web web = new Web();

            Scenario scenario = createScenario(true, web);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void validWebCommandWithEnabledBrowsers() {
            com.knubisoft.testlum.testing.model.global_config.Web webConfig =
                    mock(com.knubisoft.testlum.testing.model.global_config.Web.class);
            when(webConfig.isEnabled()).thenReturn(true);
            when(uiConfig.getWeb()).thenReturn(webConfig);
            when(browserUtil.filterDefaultEnabledBrowsers()).thenReturn(List.of(mock(
                    com.knubisoft.testlum.testing.model.global_config.AbstractBrowser.class)));

            Web web = new Web();

            Scenario scenario = createScenario(true, web);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateMobileBrowserCommands {

        @Test
        void throwsWhenNoMobileBrowserDevicesEnabled() {
            when(mobileUtil.filterDefaultEnabledMobileBrowserDevices())
                    .thenReturn(Collections.emptyList());

            Mobilebrowser mb = new Mobilebrowser();

            Scenario scenario = createScenario(true, mb);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void throwsWhenMobileBrowserNotEnabled() {
            com.knubisoft.testlum.testing.model.global_config.Mobilebrowser mbConfig =
                    mock(com.knubisoft.testlum.testing.model.global_config.Mobilebrowser.class);
            when(mbConfig.isEnabled()).thenReturn(false);
            when(uiConfig.getMobilebrowser()).thenReturn(mbConfig);
            when(mobileUtil.filterDefaultEnabledMobileBrowserDevices())
                    .thenReturn(List.of(mock(
                            com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice.class)));

            Mobilebrowser mb = new Mobilebrowser();

            Scenario scenario = createScenario(true, mb);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void validMobileBrowserWithEnabledDevices() {
            com.knubisoft.testlum.testing.model.global_config.Mobilebrowser mbConfig =
                    mock(com.knubisoft.testlum.testing.model.global_config.Mobilebrowser.class);
            when(mbConfig.isEnabled()).thenReturn(true);
            when(uiConfig.getMobilebrowser()).thenReturn(mbConfig);
            when(mobileUtil.filterDefaultEnabledMobileBrowserDevices())
                    .thenReturn(List.of(mock(
                            com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice.class)));

            Mobilebrowser mb = new Mobilebrowser();

            Scenario scenario = createScenario(true, mb);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateNativeCommands {

        @Test
        void throwsWhenNoNativeDevicesEnabled() {
            when(mobileUtil.filterDefaultEnabledNativeDevices())
                    .thenReturn(Collections.emptyList());

            Native nativeCmd = new Native();

            Scenario scenario = createScenario(true, nativeCmd);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void throwsWhenNativeNotEnabled() {
            com.knubisoft.testlum.testing.model.global_config.Native nativeConfig =
                    mock(com.knubisoft.testlum.testing.model.global_config.Native.class);
            when(nativeConfig.isEnabled()).thenReturn(false);
            when(uiConfig.getNative()).thenReturn(nativeConfig);
            when(mobileUtil.filterDefaultEnabledNativeDevices())
                    .thenReturn(List.of(mock(
                            com.knubisoft.testlum.testing.model.global_config.NativeDevice.class)));

            Native nativeCmd = new Native();

            Scenario scenario = createScenario(true, nativeCmd);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void validNativeWithEnabledDevices() {
            com.knubisoft.testlum.testing.model.global_config.Native nativeConfig =
                    mock(com.knubisoft.testlum.testing.model.global_config.Native.class);
            when(nativeConfig.isEnabled()).thenReturn(true);
            when(uiConfig.getNative()).thenReturn(nativeConfig);
            when(mobileUtil.filterDefaultEnabledNativeDevices())
                    .thenReturn(List.of(mock(
                            com.knubisoft.testlum.testing.model.global_config.NativeDevice.class)));

            Native nativeCmd = new Native();

            Scenario scenario = createScenario(true, nativeCmd);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateIfContainsNativeAndMobileCommands {

        @Test
        void noNativeOrMobileDoesNothing() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateIfContainsNativeAndMobileCommands", List.class);
            method.setAccessible(true);

            List<AbstractCommand> commands = List.of(new Postgres());
            assertDoesNotThrow(() -> method.invoke(validator, commands));

            verify(mobileUtil, never()).isNativeAndMobileBrowserConfigEnabled();
        }

        @Test
        void onlyNativeDoesNotTriggerCheck() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateIfContainsNativeAndMobileCommands", List.class);
            method.setAccessible(true);

            List<AbstractCommand> commands = List.of(new Native());
            assertDoesNotThrow(() -> method.invoke(validator, commands));

            verify(mobileUtil, never()).isNativeAndMobileBrowserConfigEnabled();
        }

        @Test
        void onlyMobileBrowserDoesNotTriggerCheck() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateIfContainsNativeAndMobileCommands", List.class);
            method.setAccessible(true);

            List<AbstractCommand> commands = List.of(new Mobilebrowser());
            assertDoesNotThrow(() -> method.invoke(validator, commands));

            verify(mobileUtil, never()).isNativeAndMobileBrowserConfigEnabled();
        }

        @Test
        void bothNativeAndMobileTriggersConfigCheck() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateIfContainsNativeAndMobileCommands", List.class);
            method.setAccessible(true);

            when(mobileUtil.isNativeAndMobileBrowserConfigEnabled()).thenReturn(false);

            List<AbstractCommand> commands = new ArrayList<>();
            commands.add(new Native());
            commands.add(new Mobilebrowser());

            assertDoesNotThrow(() -> method.invoke(validator, commands));

            verify(mobileUtil).isNativeAndMobileBrowserConfigEnabled();
        }
    }

    @Nested
    class ExtractFileNameFromVariationVariable {

        @Test
        void extractsVariableNameFromDoubleBraces() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "extractFileNameFromVariationVariable", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(validator, "{{fileName}}");
            assertEquals("fileName", result);
        }

        @Test
        void returnsOriginalWhenNoDoubleBraces() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "extractFileNameFromVariationVariable", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(validator, "plainFile.json");
            assertEquals("plainFile.json", result);
        }

        @Test
        void returnsNullForNullInput() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "extractFileNameFromVariationVariable", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(validator, (Object) null);
            assertNull(result);
        }

        @Test
        void returnsBlankForBlankInput() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "extractFileNameFromVariationVariable", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(validator, "   ");
            assertEquals("   ", result);
        }

        @Test
        void returnsEmptyForEmptyInput() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "extractFileNameFromVariationVariable", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(validator, "");
            assertEquals("", result);
        }

        @Test
        void extractsFirstVariableWhenMultiplePresent() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "extractFileNameFromVariationVariable", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(validator, "{{first}}_{{second}}");
            assertEquals("first", result);
        }

        @Test
        void extractsVariableWithDots() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "extractFileNameFromVariationVariable", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(validator, "{{file.name}}");
            assertEquals("file.name", result);
        }
    }

    @Nested
    class ValidateFileIfExist {

        @Test
        void blankFileNameDoesNothing() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateFileIfExist", File.class, String.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> method.invoke(validator, xmlFile(), ""));
            verifyNoInteractions(fileSearcher);
        }

        @Test
        void nullFileNameDoesNothing() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateFileIfExist", File.class, String.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> method.invoke(validator, xmlFile(), (String) null));
            verifyNoInteractions(fileSearcher);
        }

        @Test
        void plainFileNameSearchesFromDir() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateFileIfExist", File.class, String.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> method.invoke(validator, xmlFile(), "result.json"));

            verify(fileSearcher).searchFileFromDir(xmlFile(), "result.json");
        }

        @Test
        void variationFileNameWithoutVariationsDoesNothing() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateFileIfExist", File.class, String.class);
            method.setAccessible(true);

            // variationList is empty by default, so variation pattern is skipped
            assertDoesNotThrow(() -> method.invoke(validator, xmlFile(), "{{varFile}}"));
            verifyNoInteractions(fileSearcher);
        }
    }

    @Nested
    class ValidateFileExistenceInDataFolder {

        @Test
        void blankFileNameReturnsAsIs() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateFileExistenceInDataFolder", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(validator, "");
            assertEquals("", result);
            verifyNoInteractions(fileSearcher);
        }

        @Test
        void nullFileNameReturnsNull() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateFileExistenceInDataFolder", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(validator, (Object) null);
            assertNull(result);
            verifyNoInteractions(fileSearcher);
        }

        @Test
        void plainFileNameSearchesFromDataFolder() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateFileExistenceInDataFolder", String.class);
            method.setAccessible(true);

            when(fileSearcher.searchFileFromDataFolder("creds.json"))
                    .thenReturn(new File("creds.json"));

            String result = (String) method.invoke(validator, "creds.json");
            assertEquals("creds.json", result);
        }

        @Test
        void variationFileNameWithoutVariationsReturnsAsIs() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateFileExistenceInDataFolder", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(validator, "{{varFile}}");
            assertEquals("{{varFile}}", result);
            verifyNoInteractions(fileSearcher);
        }
    }

    @Nested
    class MultipleCommandsInScenario {

        @Test
        void validatesMultipleCommandsSequentially() {
            PostgresIntegration pgInt = new PostgresIntegration();
            when(integrations.getPostgresIntegration()).thenReturn(pgInt);

            MysqlIntegration mysqlInt = new MysqlIntegration();
            when(integrations.getMysqlIntegration()).thenReturn(mysqlInt);

            Postgres pg = new Postgres();
            pg.setAlias("pgAlias");
            pg.setFile("pg_result.json");

            Mysql mysql = new Mysql();
            mysql.setAlias("mysqlAlias");
            mysql.setFile("mysql_result.json");

            Scenario scenario = createScenario(true, pg, mysql);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));

            verify(integrationsUtil).findForAlias(pgInt.getPostgres(), "pgAlias");
            verify(integrationsUtil).findForAlias(mysqlInt.getMysql(), "mysqlAlias");
        }
    }

    @Nested
    class UnknownCommandType {

        @Test
        void unrecognizedCommandDoesNotThrow() {
            // Wait command has no validator registered, should be silently ignored
            Wait wait = new Wait();
            Scenario scenario = createScenario(true, wait);
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }
    }

    @Nested
    class ValidateLocator {

        @Test
        void throwsWhenLocatorIsBlank() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateLocator", CommandWithOptionalLocator.class);
            method.setAccessible(true);

            Scroll scroll = new Scroll();
            scroll.setLocator("");

            Exception ex = assertThrows(Exception.class,
                    () -> method.invoke(validator, scroll));
            assertTrue(ex.getCause() instanceof DefaultFrameworkException);
        }

        @Test
        void doesNotThrowWhenLocatorIsPresent() throws Exception {
            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "validateLocator", CommandWithOptionalLocator.class);
            method.setAccessible(true);

            Scroll scroll = new Scroll();
            scroll.setLocator("page.element");

            assertDoesNotThrow(() -> method.invoke(validator, scroll));
        }
    }

    @Nested
    class InitMethod {

        @Test
        void initPopulatesValidatorMaps() throws Exception {
            Field abstractField = ScenarioValidator.class.getDeclaredField("abstractCommandValidatorsMap");
            abstractField.setAccessible(true);
            assertNotNull(abstractField.get(validator));

            Field uiField = ScenarioValidator.class.getDeclaredField("uiCommandValidatorsMap");
            uiField.setAccessible(true);
            assertNotNull(uiField.get(validator));
        }

        @Test
        void abstractCommandValidatorsMapIsNotEmpty() throws Exception {
            Field field = ScenarioValidator.class.getDeclaredField("abstractCommandValidatorsMap");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<?, ?> map = (Map<?, ?>) field.get(validator);
            assertFalse(map.isEmpty());
        }

        @Test
        void uiCommandValidatorsMapIsNotEmpty() throws Exception {
            Field field = ScenarioValidator.class.getDeclaredField("uiCommandValidatorsMap");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<?, ?> map = (Map<?, ?>) field.get(validator);
            assertFalse(map.isEmpty());
        }
    }

    @Nested
    class VariationListThreadLocal {

        @Test
        void variationListIsClearedOnValidate() {
            Scenario scenario = createScenario(true);
            validator.validate(scenario, xmlFile());
            // After validate completes, the ThreadLocal should be removed (cleaned up)
            // This test just verifies no exception on second call
            assertDoesNotThrow(() -> validator.validate(scenario, xmlFile()));
        }

        @Test
        void variationListIsRemovedEvenOnException() {
            when(integrations.getPostgresIntegration()).thenReturn(null);

            Postgres pg = new Postgres();
            pg.setAlias("alias");
            pg.setFile("file.json");
            Scenario scenario = createScenario(true, pg);

            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(scenario, xmlFile()));

            // Subsequent call should not carry over stale state
            Scenario emptyScenario = createScenario(true);
            assertDoesNotThrow(() -> validator.validate(emptyScenario, xmlFile()));
        }
    }

    @Nested
    class GetDbIntegrationList {

        @Test
        void returnsPostgresListForPostgresType() throws Exception {
            PostgresIntegration pgInt = new PostgresIntegration();
            when(integrations.getPostgresIntegration()).thenReturn(pgInt);

            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "getDbIntegrationList", RelationalDB.class);
            method.setAccessible(true);

            Object result = method.invoke(validator, RelationalDB.POSTGRES);
            assertNotNull(result);
        }

        @Test
        void returnsMysqlListForMysqlType() throws Exception {
            MysqlIntegration mysqlInt = new MysqlIntegration();
            when(integrations.getMysqlIntegration()).thenReturn(mysqlInt);

            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "getDbIntegrationList", RelationalDB.class);
            method.setAccessible(true);

            Object result = method.invoke(validator, RelationalDB.MYSQL);
            assertNotNull(result);
        }

        @Test
        void returnsOracleListForOracleType() throws Exception {
            OracleIntegration oracleInt = new OracleIntegration();
            when(integrations.getOracleIntegration()).thenReturn(oracleInt);

            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "getDbIntegrationList", RelationalDB.class);
            method.setAccessible(true);

            Object result = method.invoke(validator, RelationalDB.ORACLE);
            assertNotNull(result);
        }

        @Test
        void returnsClickhouseListForClickhouseType() throws Exception {
            ClickhouseIntegration chInt = new ClickhouseIntegration();
            when(integrations.getClickhouseIntegration()).thenReturn(chInt);

            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "getDbIntegrationList", RelationalDB.class);
            method.setAccessible(true);

            Object result = method.invoke(validator, RelationalDB.CLICKHOUSE);
            assertNotNull(result);
        }

        @Test
        void throwsWhenDbIntegrationIsNull() throws Exception {
            when(integrations.getPostgresIntegration()).thenReturn(null);

            Method method = ScenarioValidator.class.getDeclaredMethod(
                    "getDbIntegrationList", RelationalDB.class);
            method.setAccessible(true);

            Exception ex = assertThrows(Exception.class,
                    () -> method.invoke(validator, RelationalDB.POSTGRES));
            assertTrue(ex.getCause() instanceof DefaultFrameworkException);
        }
    }
}
