package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.SendGridUtil;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.util.HttpUtil;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.framework.xml.XMLValidator;
import com.knubisoft.testlum.testing.model.global_config.*;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import com.knubisoft.testlum.testing.model.scenario.Clickhouse;
import com.knubisoft.testlum.testing.model.scenario.Dynamo;
import com.knubisoft.testlum.testing.model.scenario.Elasticsearch;
import com.knubisoft.testlum.testing.model.scenario.Kafka;
import com.knubisoft.testlum.testing.model.scenario.Lambda;
import com.knubisoft.testlum.testing.model.scenario.Mobilebrowser;
import com.knubisoft.testlum.testing.model.scenario.Mongo;
import com.knubisoft.testlum.testing.model.scenario.Mysql;
import com.knubisoft.testlum.testing.model.scenario.Native;
import com.knubisoft.testlum.testing.model.scenario.Oracle;
import com.knubisoft.testlum.testing.model.scenario.Postgres;
import com.knubisoft.testlum.testing.model.scenario.Redis;
import com.knubisoft.testlum.testing.model.scenario.S3;
import com.knubisoft.testlum.testing.model.scenario.Sendgrid;
import com.knubisoft.testlum.testing.model.scenario.Ses;
import com.knubisoft.testlum.testing.model.scenario.Smtp;
import com.knubisoft.testlum.testing.model.scenario.Sqs;
import com.knubisoft.testlum.testing.model.scenario.Twilio;
import com.knubisoft.testlum.testing.model.scenario.Web;
import com.knubisoft.testlum.testing.model.scenario.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class ScenarioValidator implements XMLValidator<Scenario> {

    private static final String NO_VALUE_FOUND_FOR_KEY = "Unable to find value for key <%s>.";
    private static final Pattern VARIATION_VARIABLE_PATTERN = Pattern.compile("\\{\\{(.*?)}}");

    private final Integrations integrations;
    private final MobileUtil mobileUtil;
    private final IntegrationsUtil integrationsUtil;
    private final TestResourceSettings testResourceSettings;
    private final HttpUtil httpUtil;
    private final FileSearcher fileSearcher;
    private final SendGridUtil sendGridUtil;
    private final BrowserUtil browserUtil;
    private final DatasetValidator datasetValidator;
    private final GlobalVariationsProvider globalVariationsProvider;
    private final UiConfig uiConfig;
    private final GlobalTestConfigurationProvider.UIConfiguration uiConfigs;

    private Map<AbstractCommandPredicate, AbstractCommandValidator> abstractCommandValidatorsMap;
    private Map<AbstractCommandPredicate, AbstractCommandValidator> uiCommandValidatorsMap;

    private List<Map<String, String>> variationList = new ArrayList<>();

    @PostConstruct
    public void init() {
        this.abstractCommandValidatorsMap = createCommandsValidatorsMap(integrations);
        this.uiCommandValidatorsMap = createUICommandsValidatorMap();
    }

    //CHECKSTYLE:OFF
    private @NotNull Map<AbstractCommandPredicate, AbstractCommandValidator>
    createCommandsValidatorsMap(final Integrations integrations) {
        return Map.ofEntries(
                Map.entry(o -> o instanceof Auth, (xmlFile, command) -> {
                    checkIntegrationExistence(integrations.getApis(), Apis.class);
                    Auth auth = (Auth) command;
                    validateFileExistenceInDataFolder(auth.getCredentials());
                    validateAuthCommand(auth);
                }),
                Map.entry(o -> o instanceof Http, (xmlFile, command) -> {
                    checkIntegrationExistence(integrations.getApis(), Apis.class);
                    Http http = (Http) command;
                    validateHttpCommand(xmlFile, http);
                }),
                Map.entry(o -> o instanceof Migrate, (xmlFile, command) -> {
                    Migrate migrate = (Migrate) command;
                    validateExistsDatasets(migrate);
                }),
                Map.entry(o -> o instanceof Postgres, (xmlFile, command) -> {
                    PostgresIntegration postgresIntegration = integrations.getPostgresIntegration();
                    checkIntegrationExistence(postgresIntegration, PostgresIntegration.class);
                    Postgres postgres = (Postgres) command;
                    validateAlias(postgresIntegration.getPostgres(), postgres.getAlias());
                    validateFileIfExist(xmlFile, postgres.getFile());
                }),
                Map.entry(o -> o instanceof Mysql, (xmlFile, command) -> {
                    MysqlIntegration mysqlIntegration = integrations.getMysqlIntegration();
                    checkIntegrationExistence(mysqlIntegration, MysqlIntegration.class);
                    Mysql mysql = (Mysql) command;
                    validateAlias(mysqlIntegration.getMysql(), mysql.getAlias());
                    validateFileIfExist(xmlFile, mysql.getFile());
                }),
                Map.entry(o -> o instanceof Oracle, (xmlFile, command) -> {
                    OracleIntegration oracleIntegration = integrations.getOracleIntegration();
                    checkIntegrationExistence(oracleIntegration, OracleIntegration.class);
                    Oracle oracle = (Oracle) command;
                    validateAlias(oracleIntegration.getOracle(), oracle.getAlias());
                    validateFileIfExist(xmlFile, oracle.getFile());
                }),
                Map.entry(o -> o instanceof Clickhouse, (xmlFile, command) -> {
                    ClickhouseIntegration clickhouseIntegration = integrations.getClickhouseIntegration();
                    checkIntegrationExistence(clickhouseIntegration, ClickhouseIntegration.class);
                    Clickhouse clickhouse = (Clickhouse) command;
                    validateAlias(clickhouseIntegration.getClickhouse(), clickhouse.getAlias());
                    validateFileIfExist(xmlFile, clickhouse.getFile());
                }),
                Map.entry(o -> o instanceof Redis, (xmlFile, command) -> {
                    RedisIntegration redisIntegration = integrations.getRedisIntegration();
                    checkIntegrationExistence(redisIntegration, RedisIntegration.class);
                    Redis redis = (Redis) command;
                    validateAlias(redisIntegration.getRedis(), redis.getAlias());
                    validateFileIfExist(xmlFile, redis.getFile());
                }),
                Map.entry(o -> o instanceof Mongo, (xmlFile, command) -> {
                    MongoIntegration mongoIntegration = integrations.getMongoIntegration();
                    checkIntegrationExistence(mongoIntegration, MongoIntegration.class);
                    Mongo mongo = (Mongo) command;
                    validateAlias(mongoIntegration.getMongo(), mongo.getAlias());
                    validateFileIfExist(xmlFile, mongo.getFile());
                }),
                Map.entry(o -> o instanceof Dynamo, (xmlFile, command) -> {
                    DynamoIntegration dynamoIntegration = integrations.getDynamoIntegration();
                    checkIntegrationExistence(dynamoIntegration, DynamoIntegration.class);
                    Dynamo dynamo = (Dynamo) command;
                    validateAlias(dynamoIntegration.getDynamo(), dynamo.getAlias());
                    validateFileIfExist(xmlFile, dynamo.getFile());
                }),
                Map.entry(o -> o instanceof Rabbit, (xmlFile, command) -> {
                    RabbitmqIntegration rabbitmqIntegration = integrations.getRabbitmqIntegration();
                    checkIntegrationExistence(rabbitmqIntegration, RabbitmqIntegration.class);
                    Rabbit rabbit = (Rabbit) command;
                    validateAlias(rabbitmqIntegration.getRabbitmq(), rabbit.getAlias());
                    validateRabbitCommand(xmlFile, rabbit);
                }),
                Map.entry(o -> o instanceof Kafka, (xmlFile, command) -> {
                    KafkaIntegration kafkaIntegration = integrations.getKafkaIntegration();
                    checkIntegrationExistence(kafkaIntegration, KafkaIntegration.class);
                    Kafka kafka = (Kafka) command;
                    validateAlias(kafkaIntegration.getKafka(), kafka.getAlias());
                    validateKafkaCommand(xmlFile, kafka);
                }),
                Map.entry(o -> o instanceof Sqs, (xmlFile, command) -> {
                    SqsIntegration sqsIntegration = integrations.getSqsIntegration();
                    checkIntegrationExistence(sqsIntegration, SqsIntegration.class);
                    Sqs sqs = (Sqs) command;
                    validateAlias(sqsIntegration.getSqs(), sqs.getAlias());
                    validateSqsCommand(xmlFile, sqs);
                }),
                Map.entry(o -> o instanceof Ses, (xmlFile, command) -> {
                    SesIntegration sesIntegration = integrations.getSesIntegration();
                    checkIntegrationExistence(sesIntegration, SesIntegration.class);
                    Ses ses = (Ses) command;
                    validateAlias(sesIntegration.getSes(), ses.getAlias());
                }),
                Map.entry(o -> o instanceof S3, (xmlFile, command) -> {
                    S3Integration s3Integration = integrations.getS3Integration();
                    checkIntegrationExistence(s3Integration, S3Integration.class);
                    S3 s3 = (S3) command;
                    validateAlias(s3Integration.getS3(), s3.getAlias());
                    validateS3Command(xmlFile, s3);
                }),
                Map.entry(o -> o instanceof Elasticsearch, (xmlFile, command) -> {
                    ElasticsearchIntegration elasticsearchIntegration = integrations.getElasticsearchIntegration();
                    checkIntegrationExistence(elasticsearchIntegration, ElasticsearchIntegration.class);
                    Elasticsearch elasticsearch = (Elasticsearch) command;
                    validateAlias(elasticsearchIntegration.getElasticsearch(), elasticsearch.getAlias());
                    validateElasticsearchCommand(xmlFile, elasticsearch);
                }),
                Map.entry(o -> o instanceof Sendgrid, (xmlFile, command) -> {
                    SendgridIntegration sendgridIntegration = integrations.getSendgridIntegration();
                    checkIntegrationExistence(sendgridIntegration, SendgridIntegration.class);
                    Sendgrid sendgrid = (Sendgrid) command;
                    validateAlias(sendgridIntegration.getSendgrid(), sendgrid.getAlias());
                    validateSendgridCommand(xmlFile, sendgrid);
                }),
                Map.entry(o -> o instanceof Smtp, (xmlFile, command) -> {
                    SmtpIntegration smtpIntegration = integrations.getSmtpIntegration();
                    checkIntegrationExistence(smtpIntegration, SmtpIntegration.class);
                    Smtp smtp = (Smtp) command;
                    validateAlias(smtpIntegration.getSmtp(), smtp.getAlias());
                }),
                Map.entry(o -> o instanceof Twilio, (xmlFile, command) -> {
                    TwilioIntegration twilioIntegration = integrations.getTwilioIntegration();
                    checkIntegrationExistence(twilioIntegration, TwilioIntegration.class);
                    Twilio twilio = (Twilio) command;
                    validateAlias(twilioIntegration.getTwilio(), twilio.getAlias());
                }),
                Map.entry(o -> o instanceof Graphql, (xmlFile, command) -> {
                    GraphqlIntegration graphqlIntegration = integrations.getGraphqlIntegration();
                    checkIntegrationExistence(graphqlIntegration, GraphqlIntegration.class);
                    Graphql graphql = (Graphql) command;
                    validateAlias(graphqlIntegration.getApi(), graphql.getAlias());
                    validateGraphqlCommand(xmlFile, graphql);
                }),
                Map.entry(o -> o instanceof Websocket, (xmlFile, command) -> {
                    Websockets wsIntegration = integrations.getWebsockets();
                    checkIntegrationExistence(wsIntegration, Websockets.class);
                    Websocket websocket = (Websocket) command;
                    validateAlias(wsIntegration.getApi(), websocket.getAlias());
                    validateWebsocketCommand(xmlFile, websocket);
                }),
                Map.entry(o -> o instanceof Lambda, (xmlFile, command) -> {
                    LambdaIntegration lambdaIntegration = integrations.getLambdaIntegration();
                    checkIntegrationExistence(lambdaIntegration, LambdaIntegration.class);
                    Lambda lambda = (Lambda) command;
                    validateAlias(lambdaIntegration.getLambda(), lambda.getAlias());
                    validateLambdaCommand(xmlFile, lambda);
                }),
                Map.entry(o -> o instanceof Shell, (xmlFile, command) -> {
                    Shell shell = (Shell) command;
                    validateShellCommand(xmlFile, shell);
                }),
                Map.entry(o -> o instanceof Include, (xmlFile, command) -> {
                    Include include = (Include) command;
                    validateIncludeAction(include, xmlFile);
                }),
                Map.entry(o -> o instanceof Var, (xmlFile, command) -> {
                    Var var = (Var) command;
                    validateVarCommand(xmlFile, var.getFile(), var.getSql());
                }),
                Map.entry(o -> o instanceof Repeat, (xmlFile, command) ->
                        validateRepeatCommand(((Repeat) command).getVariations())),
                Map.entry(o -> o instanceof Web, (xmlFile, command) ->
                        validateWebCommands((Web) command, xmlFile)),
                Map.entry(o -> o instanceof Mobilebrowser, (xmlFile, command) ->
                        validateMobileBrowserCommands((Mobilebrowser) command, xmlFile)),
                Map.entry(o -> o instanceof Native, (xmlFile, command) ->
                        validateNativeCommands((Native) command, xmlFile)));
    }
    //CHECKSTYLE:ON

    //CHECKSTYLE:OFF
    private @NotNull Map<AbstractCommandPredicate, AbstractCommandValidator> createUICommandsValidatorMap() {
        return Map.of(
                o -> o instanceof WebVar, (xmlFile, command) -> {
                    WebVar var = (WebVar) command;
                    validateVarCommand(xmlFile, var.getFile(), var.getSql());
                },
                o -> o instanceof WebRepeat, (xmlFile, command) ->
                        validateRepeatCommand(((WebRepeat) command).getVariations()),
                o -> o instanceof NativeRepeat, (xmlFile, command) ->
                        validateRepeatCommand(((NativeRepeat) command).getVariations()),
                o -> o instanceof MobilebrowserRepeat, (xmlFile, command) ->
                        validateRepeatCommand(((MobilebrowserRepeat) command).getVariations()),
                o -> o instanceof Scroll && ScrollType.INNER == ((Scroll) o).getType(), (xmlFile, command) ->
                        validateLocator((Scroll) command),
                o -> o instanceof Image, (xmlFile, command) ->
                        validateFileIfExist(xmlFile, ((Image) command).getFile()),
                o -> o instanceof DragAndDrop, (xmlFile, command) ->
                        validateFileIfExist(xmlFile, ((DragAndDrop) command).getFileName()),
                o -> o instanceof Javascript, (xmlFile, command) ->
                        validateFileExistenceInDataFolder(((Javascript) command).getFile()),
                o -> o instanceof NativeVar, (xmlFile, command) -> {
                    NativeVar var = (NativeVar) command;
                    validateVarCommand(xmlFile, var.getFile(), var.getSql());
                });
    }
    //CHECKSTYLE:ON

    @Override
    public void validate(final Scenario scenario, final File xmlFile) {
        if (scenario.getSettings().isActive()) {
            validateVariationsIfExist(scenario, xmlFile);
            validateIfContainsNativeAndMobileCommands(scenario.getCommands());
            scenario.getCommands().forEach(command -> validateCommand(command, xmlFile));
        }
    }

    private void validateVariationsIfExist(final Scenario scenario, final File xmlFile) {
        if (StringUtils.isNotBlank(scenario.getSettings().getVariations())) {
            globalVariationsProvider.process(scenario, xmlFile);
            variationList.addAll(globalVariationsProvider.getVariations(scenario.getSettings().getVariations()));
        }
    }

    private void validateIfContainsNativeAndMobileCommands(final List<AbstractCommand> commands) {
        boolean containsNativeAndMobileCommands =
                commands.stream().anyMatch(command -> command instanceof Native)
                        && commands.stream().anyMatch(command -> command instanceof Mobilebrowser);
        if (containsNativeAndMobileCommands && mobileUtil.isNativeAndMobileBrowserConfigEnabled()) {
            validateNativeAndMobileAppiumConfig();
        }
    }

    private void validateNativeAndMobileAppiumConfig() {
        if (isSameUrl()) {
            throw new DefaultFrameworkException(ExceptionMessage.SAME_APPIUM_URL);
        }
        if (isSameNativeAndMobileDevices()) {
            throw new DefaultFrameworkException(ExceptionMessage.SAME_MOBILE_DEVICES);
        }
    }

    private boolean isSameUrl() {
        return uiConfigs.values().stream()
                .filter(uiConfig -> ObjectUtils.allNotNull(uiConfig.getMobilebrowser(), uiConfig.getNative()))
                .filter(uiConfig -> ObjectUtils.allNotNull(uiConfig.getNative().getConnection().getAppiumServer(),
                        uiConfig.getMobilebrowser().getConnection().getAppiumServer()))
                .anyMatch(uiConfig -> Objects.equals(
                        uiConfig.getMobilebrowser().getConnection().getAppiumServer().getServerUrl(),
                        uiConfig.getNative().getConnection().getAppiumServer().getServerUrl()));
    }

    private boolean isSameNativeAndMobileDevices() {
        return uiConfigs.values().stream()
                .filter(uiConfig -> ObjectUtils.allNotNull(uiConfig.getMobilebrowser(), uiConfig.getNative()))
                .anyMatch(this::isSameNativeAndMobileDevices);
    }

    private boolean isSameNativeAndMobileDevices(final UiConfig uiConfig) {
        List<String> nativeUdids = uiConfig.getNative().getDevices().getDevice().stream()
                .map(NativeDevice::getAppiumCapabilities)
                .filter(Objects::nonNull)
                .map(AppiumCapabilities::getUdid)
                .toList();

        return uiConfig.getMobilebrowser().getDevices().getDevice().stream()
                .map(MobilebrowserDevice::getAppiumCapabilities)
                .filter(Objects::nonNull)
                .map(AppiumCapabilities::getUdid)
                .anyMatch(nativeUdids::contains);
    }

    private String validateFileExistenceInDataFolder(final String fileName) {
        if (StringUtils.isNotBlank(fileName) && fileName.trim().contains(DelimiterConstant.DOUBLE_OPEN_BRACE)
                && fileName.trim().contains(DelimiterConstant.DOUBLE_CLOSE_BRACE) && !variationList.isEmpty()) {
            return validateFileNamesIfVariations(null, fileName).getName();
        }
        if (StringUtils.isNotBlank(fileName) && !fileName.trim().contains(DelimiterConstant.DOUBLE_OPEN_BRACE)
                && !fileName.trim().contains(DelimiterConstant.DOUBLE_CLOSE_BRACE)) {
            return fileSearcher.searchFileFromDataFolder(fileName).getName();
        }
        return fileName;
    }

    private void validateFileIfExist(final File xmlFile, final String fileName) {
        if (StringUtils.isNotBlank(fileName) && fileName.trim().contains(DelimiterConstant.DOUBLE_OPEN_BRACE)
                && fileName.trim().contains(DelimiterConstant.DOUBLE_CLOSE_BRACE) && !variationList.isEmpty()) {
            validateFileNamesIfVariations(xmlFile, fileName);
        } else if (StringUtils.isNotBlank(fileName) && !fileName.trim().contains(DelimiterConstant.DOUBLE_OPEN_BRACE)
                && !fileName.trim().contains(DelimiterConstant.DOUBLE_CLOSE_BRACE)) {
            fileSearcher.searchFileFromDir(xmlFile, fileName);
        }
    }

    private File validateFileNamesIfVariations(final File xmlFile, final String fileName) {
        String fileNameKey = this.extractFileNameFromVariationVariable(fileName);
        String variationValue = variationList.stream()
                .filter(variatonsMap -> variatonsMap.containsKey(fileNameKey))
                .map(variationsMap -> globalVariationsProvider.getValue(fileName, variationsMap))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(NO_VALUE_FOUND_FOR_KEY, fileName)));
        return Objects.nonNull(xmlFile) ? fileSearcher.searchFileFromDir(xmlFile, variationValue)
                : fileSearcher.searchFileFromDataFolder(variationValue);
    }

    private void checkIntegrationExistence(final Object integration, final Class<?> name) {
        if (Objects.isNull(integration)) {
            throw new DefaultFrameworkException(ExceptionMessage.INTEGRATION_NOT_FOUND, name.getSimpleName());
        }
    }

    private void validateAlias(final List<? extends Integration> integrationList, final String alias) {
        integrationsUtil.findForAlias(integrationList, alias);
    }

    //CHECKSTYLE:OFF
    private void validateVarCommand(final File xmlFile, final FromFile fromFile, final FromSQL fromSQL) {
        if (Objects.nonNull(fromFile)) {
            validateFileIfExist(xmlFile, fromFile.getFileName());
        }
        if (Objects.nonNull(fromSQL)) {
            List<? extends Integration> integrationList = switch (fromSQL.getDbType()) {
                case POSTGRES -> {
                    checkIntegrationExistence(integrations.getPostgresIntegration(), PostgresIntegration.class);
                    yield integrations.getPostgresIntegration().getPostgres();
                }
                case MYSQL -> {
                    checkIntegrationExistence(integrations.getMysqlIntegration(), MysqlIntegration.class);
                    yield integrations.getMysqlIntegration().getMysql();
                }
                case ORACLE -> {
                    checkIntegrationExistence(integrations.getOracleIntegration(), OracleIntegration.class);
                    yield integrations.getOracleIntegration().getOracle();
                }
                case CLICKHOUSE -> {
                    checkIntegrationExistence(integrations.getClickhouseIntegration(), ClickhouseIntegration.class);
                    yield integrations.getClickhouseIntegration().getClickhouse();
                }
            };
            validateAlias(integrationList, fromSQL.getAlias());
        }
    }
    //CHECKSTYLE:ON

    private void validateAuthCommand(final Auth auth) {
        Api apiIntegration = integrationsUtil.findApiForAlias(integrations.getApis().getApi(), auth.getApiAlias());
        if (Objects.isNull(apiIntegration.getAuth())) {
            throw new DefaultFrameworkException(ExceptionMessage.AUTH_NOT_FOUND, apiIntegration.getAlias());
        }
        boolean authCmdAliasNotMatch = auth.getCommands().stream()
                .anyMatch(command -> command instanceof Http
                        && !((Http) command).getAlias().equalsIgnoreCase(auth.getApiAlias()));
        if (authCmdAliasNotMatch) {
            throw new DefaultFrameworkException(ExceptionMessage.AUTH_ALIASES_DOESNT_MATCH);
        }
    }

    private void validateHttpCommand(final File xmlFile, final Http http) {
        integrationsUtil.findApiForAlias(integrations.getApis().getApi(), http.getAlias());

        HttpInfo httpInfo = httpUtil.getHttpMethodMetadata(http).getHttpInfo();
        Response response = httpInfo.getResponse();
        if (Objects.nonNull(response) && StringUtils.isNotBlank(response.getFile())) {
            validateFileIfExist(xmlFile, response.getFile());
        }
    }

    private void validateGraphqlCommand(final File xmlFile, final Graphql graphql) {
        Stream.of(graphql.getPost(), graphql.getGet())
                .filter(Objects::nonNull)
                .filter(v -> StringUtils.isNotBlank(v.getResponse().getFile()))
                .forEach(v -> validateFileIfExist(xmlFile, v.getResponse().getFile()));
    }

    private void validateWebsocketCommand(final File xmlFile, final Websocket websocket) {
        List<Object> commands = new ArrayList<>();
        if (Objects.nonNull(websocket.getStomp())) {
            addWebsocketCommandsToCheck(websocket.getStomp().getSubscribeOrSendOrReceive(), commands);
        } else {
            addWebsocketCommandsToCheck(websocket.getSendOrReceive(), commands);
        }
        commands.stream()
                .map(this::getWebsocketFilename)
                .filter(StringUtils::isNotBlank)
                .forEach(filename -> validateFileIfExist(xmlFile, filename));
    }

    private void addWebsocketCommandsToCheck(final List<Object> commandList, final List<Object> commands) {
        commandList.stream()
                .peek(commands::add)
                .filter(ws -> ws instanceof WebsocketSend)
                .forEach(commands::add);
    }

    private String getWebsocketFilename(final Object command) {
        if (command instanceof WebsocketSend) {
            return ((WebsocketSend) command).getFile();
        } else if (command instanceof WebsocketReceive) {
            return ((WebsocketReceive) command).getFile();
        }
        return DelimiterConstant.EMPTY;
    }

    private void validateLambdaCommand(final File xmlFile, final Lambda lambda) {
        Response response = lambda.getResponse();
        if (Objects.nonNull(response) && StringUtils.isNotBlank(response.getFile())) {
            validateFileIfExist(xmlFile, response.getFile());
        }
        LambdaBody body = lambda.getBody();
        if (Objects.nonNull(body) && Objects.nonNull(body.getFrom())) {
            validateFileIfExist(xmlFile, body.getFrom().getFile());
        }
    }

    private void validateExistsDatasets(final Migrate migrate) {
        List<String> datasets = migrate.getDataset();
        StorageName storageName = migrate.getName();
        datasets.stream()
                .map(this::validateFileExistenceInDataFolder)
                .forEach(dataset -> datasetValidator.validateDatasetByExtension(dataset, storageName));
    }

    private void validateElasticsearchCommand(final File xmlFile, final Elasticsearch elasticsearch) {
        ElasticSearchResponse elasticSearchResponse = httpUtil.getESHttpMethodMetadata(elasticsearch)
                .getElasticSearchRequest().getResponse();
        if (Objects.nonNull(elasticSearchResponse) && StringUtils.isNotBlank(elasticSearchResponse.getFile())) {
            validateFileIfExist(xmlFile, elasticSearchResponse.getFile());
        }
    }

    private void validateRabbitCommand(final File xmlFile, final Rabbit rabbit) {
        rabbit.getSendOrReceive().stream()
                .map(this::getRabbitFilename)
                .filter(StringUtils::isNotBlank)
                .forEach(filename -> validateFileIfExist(xmlFile, filename));
    }

    private String getRabbitFilename(final Object rabbitCommand) {
        if (rabbitCommand instanceof SendRmqMessage) {
            return ((SendRmqMessage) rabbitCommand).getFile();
        } else {
            return ((ReceiveRmqMessage) rabbitCommand).getFile();
        }
    }

    private void validateKafkaCommand(final File xmlFile, final Kafka kafka) {
        kafka.getSendOrReceive().stream()
                .map(this::getKafkaFilename)
                .filter(StringUtils::isNotBlank)
                .forEach(filename -> validateFileIfExist(xmlFile, filename));
    }

    private String getKafkaFilename(final Object kafkaCommand) {
        if (kafkaCommand instanceof SendKafkaMessage) {
            return ((SendKafkaMessage) kafkaCommand).getFile();
        } else {
            return ((ReceiveKafkaMessage) kafkaCommand).getFile();
        }
    }

    private void validateSqsCommand(final File xmlFile, final Sqs sqs) {
        sqs.getSendOrReceive().stream()
                .map(this::getSqsFilename)
                .filter(StringUtils::isNotBlank)
                .forEach(filename -> validateFileIfExist(xmlFile, filename));
    }

    private String getSqsFilename(final Object sqsCommand) {
        if (sqsCommand instanceof SendSqsMessage) {
            return ((SendSqsMessage) sqsCommand).getFile();
        } else {
            return ((ReceiveSqsMessage) sqsCommand).getFile();
        }
    }

    private void validateS3Command(final File xmlFile, final S3 s3) {
        s3.getFileOrBucket().stream()
                .filter(command -> command instanceof S3File)
                .map(command -> (S3File) command)
                .forEach(s3File -> validateS3FileCommand(xmlFile, s3File));
    }

    private void validateS3FileCommand(final File xmlFile, final S3File s3File) {
        if (StringUtils.isNotBlank(s3File.getUpload())) {
            validateFileIfExist(xmlFile, s3File.getUpload());
        }
    }

    private void validateSendgridCommand(final File xmlFile, final Sendgrid sendgrid) {
        SendgridInfo sendgridInfo = sendGridUtil.getSendgridMethodMetadata(sendgrid).getHttpInfo();
        Response response = sendgridInfo.getResponse();
        if (Objects.nonNull(response) && StringUtils.isNotBlank(response.getFile())) {
            validateFileIfExist(xmlFile, response.getFile());
        }

        if (sendgridInfo instanceof SendgridWithBody commandWithBody) {
            Body body = commandWithBody.getBody();
            if (Objects.nonNull(body) && Objects.nonNull(body.getFrom())) {
                validateFileIfExist(xmlFile, body.getFrom().getFile());
            }
        }
    }

    private void validateShellCommand(final File xmlFile, final Shell shell) {
        validateFileIfExist(xmlFile, shell.getFile());
        List<String> shellFiles = shell.getShellFile();
        if (!shellFiles.isEmpty()) {
            shellFiles.forEach(this::validateFileExistenceInDataFolder);
        }
    }

    private void validateRepeatCommand(final String variationsFileName) {
        if (StringUtils.isNotBlank(variationsFileName)) {
            globalVariationsProvider.process(variationsFileName);
            variationList.addAll(globalVariationsProvider.getVariations(variationsFileName));
        }
    }

    private void validateIncludeAction(final Include include, final File xmlFile) {
        if (StringUtils.isNotBlank(include.getScenario())) {
            File includedScenarioFolder = new File(testResourceSettings.getScenariosFolder(),
                    include.getScenario());
            File includedFile = fileSearcher.searchFileFromDir(includedScenarioFolder,
                    TestResourceSettings.SCENARIO_FILENAME);
            if (includedFile.equals(xmlFile)) {
                throw new DefaultFrameworkException(ExceptionMessage.SCENARIO_CANNOT_BE_INCLUDED_TO_ITSELF);
            }
        }
    }

    private void validateWebCommands(final Web command, final File xmlFile) {
        if (browserUtil.filterDefaultEnabledBrowsers().isEmpty()
                || !uiConfig.getWeb().isEnabled()) {
            throw new DefaultFrameworkException(ExceptionMessage.NOT_ENABLED_BROWSERS);
        }
        validateSubCommands(command.getClickOrInputOrAssert(), xmlFile);
    }

    private void validateMobileBrowserCommands(final Mobilebrowser command, final File xmlFile) {
        if (mobileUtil.filterDefaultEnabledMobileBrowserDevices().isEmpty()
                || !uiConfig.getMobilebrowser().isEnabled()) {
            throw new DefaultFrameworkException(ExceptionMessage.NOT_ENABLED_MOBILEBROWSER_DEVICE);
        }
        validateSubCommands(command.getClickOrInputOrAssert(), xmlFile);
    }

    private void validateNativeCommands(final Native command, final File xmlFile) {
        if (mobileUtil.filterDefaultEnabledNativeDevices().isEmpty()
                || !uiConfig.getNative().isEnabled()) {
            throw new DefaultFrameworkException(ExceptionMessage.NOT_ENABLED_NATIVE_DEVICE);
        }
        validateSubCommands(command.getClickOrInputOrAssert(), xmlFile);
    }

    private void validateLocator(final CommandWithOptionalLocator command) {
        if (!StringUtils.isNotBlank(command.getLocator())) {
            throw new DefaultFrameworkException(ExceptionMessage.NO_LOCATOR_FOUND_FOR_INNER_SCROLL);
        }
    }

    private void validateSubCommands(final List<AbstractUiCommand> subCommands, final File xmlFile) {
        subCommands.forEach(command -> uiCommandValidatorsMap.entrySet().stream()
                .filter(validator -> validator.getKey().test(command))
                .findFirst()
                .ifPresent(validator -> validator.getValue().accept(xmlFile, command))
        );
    }

    private void validateCommand(final AbstractCommand command, final File xmlFile) {
        abstractCommandValidatorsMap.entrySet().stream()
                .filter(validator -> validator.getKey().test(command))
                .findFirst()
                .ifPresent(validator -> validator.getValue().accept(xmlFile, command));
    }

    private String extractFileNameFromVariationVariable(final String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return fileName;
        }
        Matcher matcher = VARIATION_VARIABLE_PATTERN.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return fileName;
    }

    private interface AbstractCommandPredicate extends Predicate<AbstractCommand> {
    }

    private interface AbstractCommandValidator extends BiConsumer<File, AbstractCommand> {
    }
}
