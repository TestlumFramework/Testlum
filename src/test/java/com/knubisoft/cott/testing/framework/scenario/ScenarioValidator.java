package com.knubisoft.cott.testing.framework.scenario;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.BrowserUtil;
import com.knubisoft.cott.testing.framework.util.DatasetValidator;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.HttpUtil;
import com.knubisoft.cott.testing.framework.util.IntegrationsUtil;
import com.knubisoft.cott.testing.framework.util.MobileUtil;
import com.knubisoft.cott.testing.framework.util.SendGridUtil;
import com.knubisoft.cott.testing.framework.validator.XMLValidator;
import com.knubisoft.cott.testing.framework.variations.GlobalVariations;
import com.knubisoft.cott.testing.model.global_config.Apis;
import com.knubisoft.cott.testing.model.global_config.AppiumCapabilities;
import com.knubisoft.cott.testing.model.global_config.ClickhouseIntegration;
import com.knubisoft.cott.testing.model.global_config.DynamoIntegration;
import com.knubisoft.cott.testing.model.global_config.ElasticsearchIntegration;
import com.knubisoft.cott.testing.model.global_config.Integration;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.KafkaIntegration;
import com.knubisoft.cott.testing.model.global_config.LambdaIntegration;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.MongoIntegration;
import com.knubisoft.cott.testing.model.global_config.MysqlIntegration;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.global_config.OracleIntegration;
import com.knubisoft.cott.testing.model.global_config.PostgresIntegration;
import com.knubisoft.cott.testing.model.global_config.RabbitmqIntegration;
import com.knubisoft.cott.testing.model.global_config.RedisIntegration;
import com.knubisoft.cott.testing.model.global_config.S3Integration;
import com.knubisoft.cott.testing.model.global_config.SendgridIntegration;
import com.knubisoft.cott.testing.model.global_config.SesIntegration;
import com.knubisoft.cott.testing.model.global_config.SmtpIntegration;
import com.knubisoft.cott.testing.model.global_config.SqsIntegration;
import com.knubisoft.cott.testing.model.global_config.TwilioIntegration;
import com.knubisoft.cott.testing.model.global_config.UiConfig;
import com.knubisoft.cott.testing.model.global_config.Websockets;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.cott.testing.model.scenario.Auth;
import com.knubisoft.cott.testing.model.scenario.Body;
import com.knubisoft.cott.testing.model.scenario.Clickhouse;
import com.knubisoft.cott.testing.model.scenario.CommandWithOptionalLocator;
import com.knubisoft.cott.testing.model.scenario.Dynamo;
import com.knubisoft.cott.testing.model.scenario.Elasticsearch;
import com.knubisoft.cott.testing.model.scenario.FromFile;
import com.knubisoft.cott.testing.model.scenario.FromSQL;
import com.knubisoft.cott.testing.model.scenario.Http;
import com.knubisoft.cott.testing.model.scenario.HttpInfo;
import com.knubisoft.cott.testing.model.scenario.Include;
import com.knubisoft.cott.testing.model.scenario.Javascript;
import com.knubisoft.cott.testing.model.scenario.Kafka;
import com.knubisoft.cott.testing.model.scenario.Lambda;
import com.knubisoft.cott.testing.model.scenario.LambdaBody;
import com.knubisoft.cott.testing.model.scenario.Migrate;
import com.knubisoft.cott.testing.model.scenario.Mobilebrowser;
import com.knubisoft.cott.testing.model.scenario.Mongo;
import com.knubisoft.cott.testing.model.scenario.Mysql;
import com.knubisoft.cott.testing.model.scenario.Native;
import com.knubisoft.cott.testing.model.scenario.Oracle;
import com.knubisoft.cott.testing.model.scenario.Postgres;
import com.knubisoft.cott.testing.model.scenario.Rabbit;
import com.knubisoft.cott.testing.model.scenario.ReceiveKafkaMessage;
import com.knubisoft.cott.testing.model.scenario.ReceiveRmqMessage;
import com.knubisoft.cott.testing.model.scenario.Redis;
import com.knubisoft.cott.testing.model.scenario.Response;
import com.knubisoft.cott.testing.model.scenario.S3;
import com.knubisoft.cott.testing.model.scenario.Scenario;
import com.knubisoft.cott.testing.model.scenario.Scroll;
import com.knubisoft.cott.testing.model.scenario.ScrollNative;
import com.knubisoft.cott.testing.model.scenario.ScrollType;
import com.knubisoft.cott.testing.model.scenario.SendKafkaMessage;
import com.knubisoft.cott.testing.model.scenario.SendRmqMessage;
import com.knubisoft.cott.testing.model.scenario.Sendgrid;
import com.knubisoft.cott.testing.model.scenario.SendgridInfo;
import com.knubisoft.cott.testing.model.scenario.SendgridWithBody;
import com.knubisoft.cott.testing.model.scenario.Ses;
import com.knubisoft.cott.testing.model.scenario.Shell;
import com.knubisoft.cott.testing.model.scenario.Smtp;
import com.knubisoft.cott.testing.model.scenario.Sqs;
import com.knubisoft.cott.testing.model.scenario.StorageName;
import com.knubisoft.cott.testing.model.scenario.SwipeNative;
import com.knubisoft.cott.testing.model.scenario.SwipeType;
import com.knubisoft.cott.testing.model.scenario.Twilio;
import com.knubisoft.cott.testing.model.scenario.Var;
import com.knubisoft.cott.testing.model.scenario.Web;
import com.knubisoft.cott.testing.model.scenario.WebVar;
import com.knubisoft.cott.testing.model.scenario.Websocket;
import com.knubisoft.cott.testing.model.scenario.WebsocketReceive;
import com.knubisoft.cott.testing.model.scenario.WebsocketSend;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.AUTH_ALIASES_DOESNT_MATCH;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.DB_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.INTEGRATION_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NOT_ENABLED_BROWSERS;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NOT_ENABLED_MOBILEBROWSER_DEVICE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NOT_ENABLED_NATIVE_DEVICE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NO_LOCATOR_FOUND_FOR_ELEMENT_SWIPE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NO_LOCATOR_FOUND_FOR_INNER_SCROLL;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SAME_APPIUM_URL;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SAME_MOBILE_DEVICES;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCENARIO_CANNOT_BE_INCLUDED_TO_ITSELF;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.JSON_EXTENSION;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.allNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ScenarioValidator implements XMLValidator<Scenario> {

    private final Map<AbstractCommandPredicate, AbstractCommandValidator> abstractCommandValidatorsMap;
    private final Integrations integrations = GlobalTestConfigurationProvider.getDefaultIntegrations();

    public ScenarioValidator() {

        Map<AbstractCommandPredicate, AbstractCommandValidator> validatorMap = new HashMap<>();

        validatorMap.put(o -> o instanceof Auth, (xmlFile, command) -> {
            Auth auth = (Auth) command;
            validateFileExistenceInDataFolder(auth.getCredentials());
            validateAlias(integrations.getApis().getApi(), auth.getApiAlias());
            validateAuthCommand(auth);
        });

        validatorMap.put(o -> o instanceof Http, (xmlFile, command) -> {
            checkIntegrationExistence(integrations.getApis(), Apis.class);
            Http http = (Http) command;
            validateHttpCommand(xmlFile, http);
        });

        validatorMap.put(o -> o instanceof Shell, (xmlFile, command) -> {
            Shell shell = (Shell) command;
            validateShellCommand(xmlFile, shell);
        });

        validatorMap.put(o -> o instanceof Migrate, (xmlFile, command) -> {
            Migrate migrate = (Migrate) command;
            validateExistsDatasets(migrate);
        });

        validatorMap.put(o -> o instanceof Elasticsearch, (xmlFile, command) -> {
            ElasticsearchIntegration elasticsearchIntegration = integrations.getElasticsearchIntegration();
            checkIntegrationExistence(elasticsearchIntegration, ElasticsearchIntegration.class);
            Elasticsearch elasticsearch = (Elasticsearch) command;
            validateAlias(elasticsearchIntegration.getElasticsearch(), elasticsearch.getAlias());
            validateElasticsearchCommand(xmlFile, elasticsearch);
        });

        validatorMap.put(o -> o instanceof S3, (xmlFile, command) -> {
            S3Integration s3Integration = integrations.getS3Integration();
            checkIntegrationExistence(s3Integration, S3Integration.class);
            S3 s3 = (S3) command;
            validateAlias(s3Integration.getS3(), s3.getAlias());
            validateS3Command(xmlFile, s3);
        });

        validatorMap.put(o -> o instanceof Postgres, (xmlFile, command) -> {
            PostgresIntegration postgresIntegration = integrations.getPostgresIntegration();
            checkIntegrationExistence(postgresIntegration, PostgresIntegration.class);
            Postgres postgres = (Postgres) command;
            validateAlias(postgresIntegration.getPostgres(), postgres.getAlias());
            validateFileIfExist(xmlFile, postgres.getFile());
        });

        validatorMap.put(o -> o instanceof Mysql, (xmlFile, command) -> {
            MysqlIntegration mysqlIntegration = integrations.getMysqlIntegration();
            checkIntegrationExistence(mysqlIntegration, MysqlIntegration.class);
            Mysql mysql = (Mysql) command;
            validateAlias(mysqlIntegration.getMysql(), mysql.getAlias());
            validateFileIfExist(xmlFile, mysql.getFile());
        });

        validatorMap.put(o -> o instanceof Oracle, (xmlFile, command) -> {
            OracleIntegration oracleIntegration = integrations.getOracleIntegration();
            checkIntegrationExistence(oracleIntegration, OracleIntegration.class);
            Oracle oracle = (Oracle) command;
            validateAlias(oracleIntegration.getOracle(), oracle.getAlias());
            validateFileIfExist(xmlFile, oracle.getFile());
        });

        validatorMap.put(o -> o instanceof Redis, (xmlFile, command) -> {
            RedisIntegration redisIntegration = integrations.getRedisIntegration();
            checkIntegrationExistence(redisIntegration, RedisIntegration.class);
            Redis redis = (Redis) command;
            validateAlias(redisIntegration.getRedis(), redis.getAlias());
            validateFileIfExist(xmlFile, redis.getFile());
        });

        validatorMap.put(o -> o instanceof Mongo, (xmlFile, command) -> {
            MongoIntegration mongoIntegration = integrations.getMongoIntegration();
            checkIntegrationExistence(mongoIntegration, MongoIntegration.class);
            Mongo mongo = (Mongo) command;
            validateAlias(mongoIntegration.getMongo(), mongo.getAlias());
            validateFileIfExist(xmlFile, mongo.getFile());
        });

        validatorMap.put(o -> o instanceof Dynamo, (xmlFile, command) -> {
            DynamoIntegration dynamoIntegration = integrations.getDynamoIntegration();
            checkIntegrationExistence(dynamoIntegration, DynamoIntegration.class);
            Dynamo dynamo = (Dynamo) command;
            validateAlias(dynamoIntegration.getDynamo(), dynamo.getAlias());
            validateFileIfExist(xmlFile, dynamo.getFile());
        });

        validatorMap.put(o -> o instanceof Clickhouse, (xmlFile, command) -> {
            ClickhouseIntegration clickhouseIntegration = integrations.getClickhouseIntegration();
            checkIntegrationExistence(clickhouseIntegration, ClickhouseIntegration.class);
            Clickhouse clickhouse = (Clickhouse) command;
            validateAlias(clickhouseIntegration.getClickhouse(), clickhouse.getAlias());
            validateFileIfExist(xmlFile, clickhouse.getFile());
        });

        validatorMap.put(o -> o instanceof Rabbit, (xmlFile, command) -> {
            RabbitmqIntegration rabbitmqIntegration = integrations.getRabbitmqIntegration();
            checkIntegrationExistence(rabbitmqIntegration, RabbitmqIntegration.class);
            Rabbit rabbit = (Rabbit) command;
            validateAlias(rabbitmqIntegration.getRabbitmq(), rabbit.getAlias());
            validateRabbitCommand(xmlFile, rabbit);
        });

        validatorMap.put(o -> o instanceof Kafka, (xmlFile, command) -> {
            KafkaIntegration kafkaIntegration = integrations.getKafkaIntegration();
            checkIntegrationExistence(kafkaIntegration, KafkaIntegration.class);
            Kafka kafka = (Kafka) command;
            validateAlias(kafkaIntegration.getKafka(), kafka.getAlias());
            validateKafkaCommand(xmlFile, kafka);
        });

        validatorMap.put(o -> o instanceof Sqs, (xmlFile, command) -> {
            SqsIntegration sqsIntegration = integrations.getSqsIntegration();
            checkIntegrationExistence(sqsIntegration, SqsIntegration.class);
            Sqs sqs = (Sqs) command;
            validateAlias(sqsIntegration.getSqs(), sqs.getAlias());
            validateSqsCommand(xmlFile, sqs);
        });

        validatorMap.put(o -> o instanceof Ses, (xmlFile, command) -> {
            SesIntegration sesIntegration = integrations.getSesIntegration();
            checkIntegrationExistence(sesIntegration, SesIntegration.class);
            Ses ses = (Ses) command;
            validateAlias(sesIntegration.getSes(), ses.getAlias());
        });

        validatorMap.put(o -> o instanceof Sendgrid, (xmlFile, command) -> {
            SendgridIntegration sendgridIntegration = integrations.getSendgridIntegration();
            checkIntegrationExistence(sendgridIntegration, SendgridIntegration.class);
            Sendgrid sendgrid = (Sendgrid) command;
            validateAlias(sendgridIntegration.getSendgrid(), sendgrid.getAlias());
            validateSendgridCommand(xmlFile, sendgrid);
        });

        validatorMap.put(o -> o instanceof Smtp, (xmlFile, command) -> {
            SmtpIntegration smtpIntegration = integrations.getSmtpIntegration();
            checkIntegrationExistence(smtpIntegration, SmtpIntegration.class);
            Smtp smtp = (Smtp) command;
            validateAlias(smtpIntegration.getSmtp(), smtp.getAlias());
        });

        validatorMap.put(o -> o instanceof Twilio, (xmlFile, command) -> {
            TwilioIntegration twilioIntegration = integrations.getTwilioIntegration();
            checkIntegrationExistence(twilioIntegration, TwilioIntegration.class);
            Twilio twilio = (Twilio) command;
            validateAlias(twilioIntegration.getTwilio(), twilio.getAlias());
        });

        validatorMap.put(o -> o instanceof Websocket, (xmlFile, command) -> {
            Websockets wsIntegration = integrations.getWebsockets();
            checkIntegrationExistence(wsIntegration, Websockets.class);
            Websocket websocket = (Websocket) command;
            validateAlias(wsIntegration.getApi(), websocket.getAlias());
            validateWebsocketCommand(xmlFile, websocket);
        });

        validatorMap.put(o -> o instanceof Lambda, (xmlFile, command) -> {
            LambdaIntegration lambdaIntegration = integrations.getLambdaIntegration();
            checkIntegrationExistence(lambdaIntegration, LambdaIntegration.class);
            Lambda lambda = (Lambda) command;
            validateAlias(lambdaIntegration.getLambda(), lambda.getAlias());
            validateLambdaCommand(xmlFile, lambda);
        });

        validatorMap.put(o -> o instanceof Include, (xmlFile, command) -> {
            Include include = (Include) command;
            validateIncludeAction(include, xmlFile);
        });

        validatorMap.put(o -> o instanceof Var, (xmlFile, command) -> {
            Var var = (Var) command;
            validateVarCommand(xmlFile, var.getFile(), var.getSql());
        });

        validatorMap.put(o -> o instanceof Web, (xmlFile, command) -> {
            validateWebCommands((Web) command, xmlFile);
        });

        validatorMap.put(o -> o instanceof Mobilebrowser, (xmlFile, command) -> {
            validateMobilebrowserCommands((Mobilebrowser) command, xmlFile);
        });

        validatorMap.put(o -> o instanceof Native, (xmlFile, command) -> {
            validateNativeCommands((Native) command);
        });

        this.abstractCommandValidatorsMap = Collections.unmodifiableMap(validatorMap);
    }

    @Override
    public void validate(final Scenario scenario, final File xmlFile) {
        if (scenario.isActive()) {
            validateVariationsIfExist(scenario, xmlFile);
            validateIfContainsNativeAndMobileCommands(scenario.getCommands());
            scenario.getCommands().forEach(command -> validateCommand(command, xmlFile));
        }
    }

    private void validateVariationsIfExist(final Scenario scenario, final File xmlFile) {
        if (isNotBlank(scenario.getVariations())) {
            GlobalVariations.process(scenario, xmlFile);
        }
    }

    private void validateIfContainsNativeAndMobileCommands(final List<AbstractCommand> commands) {
        boolean containsNativeAndMobileCommands =
                commands.stream().anyMatch(command -> command instanceof Native)
                        && commands.stream().anyMatch(command -> command instanceof Mobilebrowser);
        if (containsNativeAndMobileCommands && MobileUtil.isNativeAndMobilebrowserConfigEnabled()) {
            validateNativeAndMobileAppiumConfig();
        }
    }

    private void validateNativeAndMobileAppiumConfig() {
        if (isSameUrl()) {
            throw new DefaultFrameworkException(SAME_APPIUM_URL);
        }
        if (isSameNativeAndMobileDevices()) {
            throw new DefaultFrameworkException(SAME_MOBILE_DEVICES);
        }
    }

    private boolean isSameUrl() {
        return GlobalTestConfigurationProvider.getUiConfigs().values().stream()
                .filter(uiConfig -> allNotNull(uiConfig.getMobilebrowser(), uiConfig.getNative()))
                .filter(uiConfig -> allNotNull(uiConfig.getNative().getConnection().getAppiumServer(),
                        uiConfig.getMobilebrowser().getConnection().getAppiumServer()))
                .anyMatch(uiConfig -> Objects.equals(
                        uiConfig.getMobilebrowser().getConnection().getAppiumServer().getServerUrl(),
                        uiConfig.getNative().getConnection().getAppiumServer().getServerUrl()));
    }

    private boolean isSameNativeAndMobileDevices() {
        return GlobalTestConfigurationProvider.getUiConfigs().values().stream()
                .filter(uiConfig -> allNotNull(uiConfig.getMobilebrowser(), uiConfig.getNative()))
                .anyMatch(this::isSameNativeAndMobileDevices);
    }

    private boolean isSameNativeAndMobileDevices(final UiConfig uiConfig) {
        List<String> nativeUdids = uiConfig.getNative().getDevices().getDevice().stream()
                .map(NativeDevice::getAppiumCapabilities)
                .filter(Objects::nonNull)
                .map(AppiumCapabilities::getUdid)
                .collect(Collectors.toList());

        return uiConfig.getMobilebrowser().getDevices().getDevice().stream()
                .map(MobilebrowserDevice::getAppiumCapabilities)
                .filter(Objects::nonNull)
                .map(AppiumCapabilities::getUdid)
                .anyMatch(nativeUdids::contains);
    }

    private void validateFileExistenceInDataFolder(final String commandFile) {
        if (isNotBlank(commandFile)) {
            FileSearcher.searchFileFromDataFolder(commandFile);
        }
    }

    private void validateFileIfExist(final File xmlFile, final String commandFile) {
        if (isNotBlank(commandFile)) {
            FileSearcher.searchFileFromDir(xmlFile, commandFile);
        }
    }

    private void checkIntegrationExistence(final Object integration, final Class<?> name) {
        if (isNull(integration)) {
            throw new DefaultFrameworkException(INTEGRATION_NOT_FOUND, name.getSimpleName());
        }
    }

    private void validateAlias(final List<? extends Integration> integrationList, final String alias) {
        IntegrationsUtil.findForAlias(integrationList, alias);
    }

    //CHECKSTYLE:OFF
    private void validateVarCommand(final File xmlFile, final FromFile fromFile, final FromSQL fromSQL) {
        if (nonNull(fromFile)) {
            validateFileIfExist(xmlFile, fromFile.getFileName());
        }
        if (nonNull(fromSQL)) {
            List<? extends Integration> integrationList;
            switch (fromSQL.getDbType()) {
                case POSTGRES:
                    checkIntegrationExistence(integrations.getPostgresIntegration(), PostgresIntegration.class);
                    integrationList = integrations.getPostgresIntegration().getPostgres();
                    break;
                case MYSQL:
                    checkIntegrationExistence(integrations.getMysqlIntegration(), MysqlIntegration.class);
                    integrationList = integrations.getMysqlIntegration().getMysql();
                    break;
                case ORACLE:
                    checkIntegrationExistence(integrations.getOracleIntegration(), OracleIntegration.class);
                    integrationList = integrations.getOracleIntegration().getOracle();
                    break;
                case CLICKHOUSE:
                    checkIntegrationExistence(integrations.getClickhouseIntegration(), ClickhouseIntegration.class);
                    integrationList = integrations.getClickhouseIntegration().getClickhouse();
                    break;
                default:
                    throw new DefaultFrameworkException(DB_NOT_SUPPORTED, fromSQL.getDbType());
            }
            validateAlias(integrationList, fromSQL.getAlias());
        }
    }
    //CHECKSTYLE:ON

    private void validateElasticsearchCommand(final File xmlFile, final Elasticsearch elasticsearch) {
        Stream.of(elasticsearch.getPost(), elasticsearch.getGet(), elasticsearch.getPut(), elasticsearch.getDelete())
                .filter(Objects::nonNull)
                .filter(v -> isNotBlank(v.getResponse().getFile()))
                .forEach(v -> FileSearcher.searchFileFromDir(xmlFile, v.getResponse().getFile()));
    }

    private void validateSqsCommand(final File xmlFile, final Sqs sqs) {
        Stream.of(sqs.getReceive(), sqs.getSend())
                .filter(StringUtils::isNotBlank).filter(o -> o.endsWith(JSON_EXTENSION))
                .forEach(v -> FileSearcher.searchFileFromDir(xmlFile, v));
    }

    private void validateRabbitCommand(final File xmlFile, final Rabbit rabbit) {
        rabbit.getSendOrReceive().stream()
                .map(this::getRabbitFilename)
                .filter(StringUtils::isNotBlank)
                .forEach(filename -> FileSearcher.searchFileFromDir(xmlFile, filename));
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
                .forEach(filename -> FileSearcher.searchFileFromDir(xmlFile, filename));
    }

    private String getKafkaFilename(final Object kafkaCommand) {
        if (kafkaCommand instanceof SendKafkaMessage) {
            return ((SendKafkaMessage) kafkaCommand).getFile();
        } else {
            return ((ReceiveKafkaMessage) kafkaCommand).getFile();
        }
    }

    private void validateS3Command(final File xmlFile, final S3 s3) {
        Stream.of(s3.getDownload(), s3.getUpload())
                .filter(StringUtils::isNotBlank)
                .forEach(v -> FileSearcher.searchFileFromDir(xmlFile, v));
    }

    private void validateSendgridCommand(final File xmlFile, final Sendgrid sendgrid) {
        SendgridInfo sendgridInfo = SendGridUtil.getSendgridMethodMetadata(sendgrid).getHttpInfo();
        Response response = sendgridInfo.getResponse();
        if (nonNull(response) && isNotBlank(response.getFile())) {
            FileSearcher.searchFileFromDir(xmlFile, response.getFile());
        }

        SendgridWithBody commandWithBody = (SendgridWithBody) sendgridInfo;
        Body body = commandWithBody.getBody();
        if (nonNull(body) && nonNull(body.getFrom())) {
            FileSearcher.searchFileFromDir(xmlFile, body.getFrom().getFile());
        }
    }

    private void validateExistsDatasets(final Migrate migrate) {
        List<String> datasets = migrate.getDataset();
        StorageName storageName = migrate.getName();
        datasets.forEach(dataset -> DatasetValidator.validateDatasetByExtension(dataset, storageName));
    }

    private void validateHttpCommand(final File xmlFile, final Http http) {
        IntegrationsUtil.findApiForAlias(integrations.getApis().getApi(), http.getAlias());

        HttpInfo httpInfo = HttpUtil.getHttpMethodMetadata(http).getHttpInfo();
        Response response = httpInfo.getResponse();
        if (nonNull(response) && isNotBlank(response.getFile())) {
            FileSearcher.searchFileFromDir(xmlFile, response.getFile());
        }
    }

    private void validateWebsocketCommand(final File xmlFile, final Websocket websocket) {
        List<Object> commands = new ArrayList<>();
        if (nonNull(websocket.getStomp())) {
            addWebsocketCommandsToCheck(websocket.getStomp().getSubscribeOrSendOrReceive(), commands);
        } else {
            addWebsocketCommandsToCheck(websocket.getSendOrReceive(), commands);
        }
        commands.stream()
                .map(this::getWebsocketFilename)
                .filter(StringUtils::isNotBlank)
                .forEach(filename -> FileSearcher.searchFileFromDir(xmlFile, filename));
    }

    private void addWebsocketCommandsToCheck(final List<Object> commandList, final List<Object> commands) {
        commandList.stream()
                .peek(commands::add)
                .filter(ws -> ws instanceof WebsocketSend && nonNull(((WebsocketSend) ws).getReceive()))
                .map(o -> ((WebsocketSend) o).getReceive())
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
        if (nonNull(response) && isNotBlank(response.getFile())) {
            FileSearcher.searchFileFromDir(xmlFile, response.getFile());
        }
        LambdaBody body = lambda.getBody();
        if (nonNull(body) && nonNull(body.getFrom())) {
            FileSearcher.searchFileFromDir(xmlFile, body.getFrom().getFile());
        }
    }

    private void validateWebCommands(final Web command, final File xmlFile) {
        if (BrowserUtil.filterDefaultEnabledBrowsers().isEmpty()
                || !GlobalTestConfigurationProvider.getDefaultUiConfigs().getWeb().isEnabled()) {
            throw new DefaultFrameworkException(NOT_ENABLED_BROWSERS);
        }
        validateSubCommands(command.getClickOrInputOrAssert(), xmlFile);
    }

    private void validateMobilebrowserCommands(final Mobilebrowser command, final File xmlFile) {
        if (MobileUtil.filterDefaultEnabledMobilebrowserDevices().isEmpty()
                || !GlobalTestConfigurationProvider.getDefaultUiConfigs().getMobilebrowser().isEnabled()) {
            throw new DefaultFrameworkException(NOT_ENABLED_MOBILEBROWSER_DEVICE);
        }
        validateSubCommands(command.getClickOrInputOrAssert(), xmlFile);
    }

    private void validateSubCommands(final List<AbstractUiCommand> subCommands, final File xmlFile) {
        subCommands.forEach(o -> {
            if (o instanceof Javascript) {
                validateFileExistenceInDataFolder(((Javascript) o).getFile());
            } else if (o instanceof Scroll && ScrollType.INNER == ((Scroll) o).getType()) {
                validateLocator((Scroll) o, NO_LOCATOR_FOUND_FOR_INNER_SCROLL);
            } else if (o instanceof WebVar) {
                WebVar webVar = (WebVar) o;
                validateVarCommand(xmlFile, webVar.getFile(), webVar.getSql());
            }
        });
    }

    private void validateNativeCommands(final Native command) {
        if (MobileUtil.filterDefaultEnabledNativeDevices().isEmpty()
                || !GlobalTestConfigurationProvider.getDefaultUiConfigs().getNative().isEnabled()) {
            throw new DefaultFrameworkException(NOT_ENABLED_NATIVE_DEVICE);
        }
        command.getClickOrInputOrAssert().forEach(o -> {
            if (o instanceof SwipeNative && SwipeType.ELEMENT == ((SwipeNative) o).getType()) {
                validateLocator((SwipeNative) o, NO_LOCATOR_FOUND_FOR_ELEMENT_SWIPE);
            }
            if (o instanceof ScrollNative && ScrollType.INNER == ((ScrollNative) o).getType()) {
                validateLocator((ScrollNative) o, NO_LOCATOR_FOUND_FOR_INNER_SCROLL);
            }
        });
    }

    private void validateLocator(final CommandWithOptionalLocator command, final String exceptionMessage) {
        if (!isNotBlank(command.getLocatorId())) {
            throw new DefaultFrameworkException(exceptionMessage);
        }
    }

    private void validateShellCommand(final File xmlFile, final Shell shell) {
        validateFileIfExist(xmlFile, shell.getFile());
        List<String> shellFiles = shell.getShellFile();
        if (!shellFiles.isEmpty()) {
            shellFiles.forEach(this::validateFileExistenceInDataFolder);
        }
    }

    private void validateAuthCommand(final Auth auth) {
        boolean authCmdAliasNotMatch = auth.getCommands().stream()
                .anyMatch(command -> command instanceof Http
                        && !((Http) command).getAlias().equalsIgnoreCase(auth.getApiAlias()));
        if (authCmdAliasNotMatch) {
            throw new DefaultFrameworkException(AUTH_ALIASES_DOESNT_MATCH);
        }
    }

    private void validateIncludeAction(final Include include, final File xmlFile) {
        if (isNotBlank(include.getScenario())) {
            File includedScenarioFolder = new File(TestResourceSettings.getInstance().getScenariosFolder(),
                    include.getScenario());
            File includedFile = FileSearcher.searchFileFromDir(includedScenarioFolder,
                    TestResourceSettings.SCENARIO_FILENAME);
            if (includedFile.equals(xmlFile)) {
                throw new DefaultFrameworkException(SCENARIO_CANNOT_BE_INCLUDED_TO_ITSELF);
            }
        }
    }

    private void validateCommand(final AbstractCommand command, final File configFile) {
        abstractCommandValidatorsMap.keySet().stream()
                .filter(key -> key.test(command))
                .map(abstractCommandValidatorsMap::get)
                .forEach(v -> v.accept(configFile, command));
    }

    private interface AbstractCommandPredicate extends Predicate<AbstractCommand> { }
    private interface AbstractCommandValidator extends BiConsumer<File, AbstractCommand> { }
}
