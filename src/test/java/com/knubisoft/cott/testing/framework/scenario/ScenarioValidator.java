package com.knubisoft.cott.testing.framework.scenario;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.BrowserUtil;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.HttpUtil;
import com.knubisoft.cott.testing.framework.util.SendGridUtil;
import com.knubisoft.cott.testing.framework.validator.XMLValidator;
import com.knubisoft.cott.testing.model.global_config.AbstractDevice;
import com.knubisoft.cott.testing.model.global_config.Apis;
import com.knubisoft.cott.testing.model.global_config.ClickhouseIntegration;
import com.knubisoft.cott.testing.model.global_config.DynamoIntegration;
import com.knubisoft.cott.testing.model.global_config.ElasticsearchIntegration;
import com.knubisoft.cott.testing.model.global_config.Integration;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.KafkaIntegration;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.MongoIntegration;
import com.knubisoft.cott.testing.model.global_config.MysqlIntegration;
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
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.Auth;
import com.knubisoft.cott.testing.model.scenario.Body;
import com.knubisoft.cott.testing.model.scenario.Clickhouse;
import com.knubisoft.cott.testing.model.scenario.CommonWeb;
import com.knubisoft.cott.testing.model.scenario.Dynamo;
import com.knubisoft.cott.testing.model.scenario.Elasticsearch;
import com.knubisoft.cott.testing.model.scenario.Http;
import com.knubisoft.cott.testing.model.scenario.HttpInfo;
import com.knubisoft.cott.testing.model.scenario.Include;
import com.knubisoft.cott.testing.model.scenario.Javascript;
import com.knubisoft.cott.testing.model.scenario.Kafka;
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
import com.knubisoft.cott.testing.model.scenario.Twilio;
import com.knubisoft.cott.testing.model.scenario.Ui;
import com.knubisoft.cott.testing.model.scenario.When;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.ALIAS_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.API_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.INTEGRATION_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SAME_APPIUM_URL;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SAME_MOBILE_DEVICES;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCENARIO_CANNOT_BE_INCLUDED_TO_ITSELF;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.JSON_EXTENSION;

public class ScenarioValidator implements XMLValidator<Scenario> {


    private final Map<AbstractCommandPredicate, AbstractCommandValidator> abstractCommandValidatorsMap;

    private final Integrations integrations = GlobalTestConfigurationProvider.getIntegrations();

    public ScenarioValidator() {

        Map<AbstractCommandPredicate, AbstractCommandValidator> validatorMap = new HashMap<>();

        validatorMap.put(o -> o instanceof Auth, (xmlFile, command) -> {
            Auth auth = (Auth) command;
            validateFileExistenceInDataFolder(auth.getCredentials());
        });

        validatorMap.put(o -> o instanceof Http, (xmlFile, command) -> {
            checkIntegrationExistence(integrations.getApis(), Apis.class);
            Http http = (Http) command;
            validateHttpCommand(xmlFile, http);
        });

        validatorMap.put(o -> CommonWeb.class.isAssignableFrom(o.getClass()), (xmlFile, command) -> {
            CommonWeb commonWeb = (CommonWeb) command;
            validateCommonWebCommands(commonWeb);
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

        validatorMap.put(o -> o instanceof When, (xmlFile, command) -> {
            When when = (When) command;
            validateWhenCommand(xmlFile, when);
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

        validatorMap.put(o -> o instanceof Include, (xmlFile, command) -> {
            Include include = (Include) command;
            validateIncludeAction(include, xmlFile);
        });

        this.abstractCommandValidatorsMap = Collections.unmodifiableMap(validatorMap);
    }

    @Override
    public void validate(final Scenario scenario, final File xmlFile) {
        if (scenario.isActive()) {
            List<AbstractCommand> uiCommands = getUiCommands(scenario.getCommands());
            if (!uiCommands.isEmpty() && isUiCommandsContainsTwoTagsTypes(scenario.getCommands())) {
                validateAppiumUrl();
                validateDevices();
            }
            scenario.getCommands().forEach(it -> validateCommand(it, xmlFile));
        }
    }

    private List<AbstractCommand> getUiCommands(final List<AbstractCommand> scenarioCommands) {
        return scenarioCommands.stream().filter(abstractCommand ->
                Ui.class.isAssignableFrom(abstractCommand.getClass())).collect(Collectors.toList());
    }

    private boolean isUiCommandsContainsTwoTagsTypes(final List<AbstractCommand> uiCommands) {
        List<AbstractCommand> nativeList = uiCommands.stream().filter(abstractCommand ->
                abstractCommand instanceof Native).collect(Collectors.toList());
        List<AbstractCommand> mobilebrowserList = uiCommands.stream().filter(abstractCommand ->
                abstractCommand instanceof Mobilebrowser).collect(Collectors.toList());
        return !nativeList.isEmpty() && !mobilebrowserList.isEmpty();
    }

    private void validateAppiumUrl() {
        if (GlobalTestConfigurationProvider.provide().getMobilebrowser().getAppiumServerUrl()
                .equals(GlobalTestConfigurationProvider.provide().getNative().getAppiumServerUrl())) {
            throw new DefaultFrameworkException(SAME_APPIUM_URL);
        }
    }

    private void validateDevices() {
        if (BrowserUtil.filterEnabledMobilebrowserDevices().stream().map(MobilebrowserDevice::getUdid)
                .anyMatch(deviceUdid -> BrowserUtil.filterEnabledNativeDevices().stream()
                        .map(AbstractDevice::getUdid).collect(Collectors.toList()).contains(deviceUdid))) {
            throw new DefaultFrameworkException(SAME_MOBILE_DEVICES);
        }
    }

    private void validateFileExistenceInDataFolder(final String commandFile) {
        if (StringUtils.hasText(commandFile)) {
            FileSearcher.searchFileFromDataFolder(commandFile);
        }
    }

    private void validateFileIfExist(final File xmlFile, final String commandFile) {
        if (StringUtils.hasText(commandFile)) {
            FileSearcher.searchFileFromDir(xmlFile, commandFile);
        }
    }

    private void checkIntegrationExistence(final Object integration, final Class<?> name) {
        if (Objects.isNull(integration)) {
            throw new DefaultFrameworkException(INTEGRATION_NOT_FOUND, name.getSimpleName());
        }
    }

    private void validateAlias(final List<? extends Integration> integrationsList, final String alias) {
        integrationsList.stream()
                .filter(Integration::isEnabled)
                .filter(o -> o.getAlias().equals(alias))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(ALIAS_NOT_FOUND, alias));
    }

    private void validateElasticsearchCommand(final File xmlFile, final Elasticsearch elasticsearch) {
        Stream.of(elasticsearch.getPost(), elasticsearch.getGet(), elasticsearch.getPut(), elasticsearch.getDelete())
                .filter(Objects::nonNull)
                .filter(v -> StringUtils.hasText(v.getResponse().getFile()))
                .forEach(v -> FileSearcher.searchFileFromDir(xmlFile, v.getResponse().getFile()));
    }

    private void validateSqsCommand(final File xmlFile, final Sqs sqs) {
        Stream.of(sqs.getReceive(), sqs.getSend())
                .filter(StringUtils::hasText).filter(o -> o.endsWith(JSON_EXTENSION))
                .forEach(v -> FileSearcher.searchFileFromDir(xmlFile, v));
    }

    private void validateRabbitCommand(final File xmlFile, final Rabbit rabbit) {
        rabbit.getSendOrReceive().stream().map(this::getRabbitFilename).filter(StringUtils::hasText)
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
        kafka.getSendOrReceive().stream().map(this::getKafkaFilename).filter(StringUtils::hasText)
                .forEach(filename -> FileSearcher.searchFileFromDir(xmlFile, filename));
    }

    private String getKafkaFilename(final Object kafkaCommand) {
        if (kafkaCommand instanceof SendKafkaMessage) {
            return ((SendKafkaMessage) kafkaCommand).getFile();
        } else {
            return ((ReceiveKafkaMessage) kafkaCommand).getFile();
        }
    }


    private void validateWhenCommand(final File xmlFile, final When when) {
        Stream.of(when.getRequest(), when.getThen())
                .filter(StringUtils::hasText)
                .forEach(v -> FileSearcher.searchFileFromDir(xmlFile, v));
    }

    private void validateS3Command(final File xmlFile, final S3 s3) {
        Stream.of(s3.getDownload(), s3.getUpload())
                .filter(StringUtils::hasText)
                .forEach(v -> FileSearcher.searchFileFromDir(xmlFile, v));
    }

    private void validateSendgridCommand(final File xmlFile, final Sendgrid sendgrid) {
        SendgridInfo sendgridInfo = SendGridUtil.getSendgridMethodMetadata(sendgrid).getHttpInfo();
        Response response = sendgridInfo.getResponse();
        if (response != null && response.getFile() != null) {
            FileSearcher.searchFileFromDir(xmlFile, response.getFile());
        }

        SendgridWithBody commandWithBody = (SendgridWithBody) sendgridInfo;
        Body body = commandWithBody.getBody();
        if (body != null && body.getFrom() != null) {
            FileSearcher.searchFileFromDir(xmlFile, body.getFrom().getFile());
        }

    }

    private void validateExistsDatasets(final Migrate migrate) {
        List<String> datasets = migrate.getDataset();
        StorageName storageName = migrate.getName();
        datasets.forEach(dataset -> DatasetValidator.validateDatasetByExtension(dataset, storageName));
    }

    private void validateHttpCommand(final File xmlFile, final Http http) {
        integrations.getApis().getApi().stream()
                .filter(s -> s.getAlias().equals(http.getAlias())).findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(API_NOT_FOUND, http.getAlias()));

        HttpInfo httpInfo = HttpUtil.getHttpMethodMetadata(http).getHttpInfo();
        Response response = httpInfo.getResponse();
        if (response != null && response.getFile() != null) {
            FileSearcher.searchFileFromDir(xmlFile, response.getFile());
        }
    }

    private void validateCommonWebCommands(final CommonWeb command) {
        command.getJavascriptOrNavigateOrHovers().forEach(o -> {
            if (o instanceof Javascript) {
                validateFileExistenceInDataFolder(((Javascript) o).getFile());
            }
        });
    }

    private void validateShellCommand(final File xmlFile, final Shell shell) {
        validateFileIfExist(xmlFile, shell.getFile());
        List<String> shellFiles = shell.getShellFile();
        if (!shellFiles.isEmpty()) {
            shellFiles.forEach(this::validateFileExistenceInDataFolder);
        }
    }

    private void validateIncludeAction(final Include include, final File xmlFile) {
        if (StringUtils.hasText(include.getScenario())) {
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

    private interface AbstractCommandPredicate extends Predicate<AbstractCommand> {

    }

    private interface AbstractCommandValidator extends BiConsumer<File, AbstractCommand> {

    }

}
