package com.knubisoft.cott.testing.framework.scenario;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.SendGridUtil;
import com.knubisoft.cott.testing.framework.validator.XMLValidator;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.HttpUtil;
import com.knubisoft.cott.testing.model.global_config.Integration;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.Auth;
import com.knubisoft.cott.testing.model.scenario.Body;
import com.knubisoft.cott.testing.model.scenario.Clickhouse;
import com.knubisoft.cott.testing.model.scenario.Dynamo;
import com.knubisoft.cott.testing.model.scenario.Elasticsearch;
import com.knubisoft.cott.testing.model.scenario.Http;
import com.knubisoft.cott.testing.model.scenario.HttpInfo;
import com.knubisoft.cott.testing.model.scenario.Include;
import com.knubisoft.cott.testing.model.scenario.Javascript;
import com.knubisoft.cott.testing.model.scenario.Kafka;
import com.knubisoft.cott.testing.model.scenario.Migrate;
import com.knubisoft.cott.testing.model.scenario.Mongo;
import com.knubisoft.cott.testing.model.scenario.Mysql;
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
import com.knubisoft.cott.testing.model.scenario.Shell;
import com.knubisoft.cott.testing.model.scenario.Sqs;
import com.knubisoft.cott.testing.model.scenario.StorageName;
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
import java.util.stream.Stream;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.ALIAS_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.API_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCENARIO_CANNOT_BE_INCLUDED_TO_ITSELF;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.JSON_EXTENSION;

public class ScenarioValidator implements XMLValidator<Scenario> {


    private final Map<AbstractCommandPredicate, AbstractCommandValidator> abstractCommandValidatorsMap;

    private final Integrations integrations = GlobalTestConfigurationProvider.getIntegrations();

    public ScenarioValidator() {

        Map<AbstractCommandPredicate, AbstractCommandValidator> validatorMap = new HashMap<>();

        validatorMap.put(o -> o instanceof Auth, (xmlFile, command) -> {
            Auth auth = (Auth) command;
            validateFileExistenceInData(auth.getCredentials());
        });

        validatorMap.put(o -> o instanceof Http, (xmlFile, command) -> {
            Http http = (Http) command;
            validateHttpCommand(xmlFile, http);
        });

        validatorMap.put(o -> o instanceof Ui, (xmlFile, command) -> {
            Ui ui = (Ui) command;
            validateUiCommands(ui);
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
            Elasticsearch elasticsearch = (Elasticsearch) command;
            validateAlias(integrations.getElasticsearchIntegration().getElasticsearch(), elasticsearch.getAlias());
            validateElasticsearchCommand(xmlFile, elasticsearch);
        });

        validatorMap.put(o -> o instanceof S3, (xmlFile, command) -> {
            S3 s3 = (S3) command;
            validateAlias(integrations.getS3Integration().getS3(), s3.getAlias());
            validateS3Command(xmlFile, s3);
        });

        validatorMap.put(o -> o instanceof When, (xmlFile, command) -> {
            When when = (When) command;
            validateWhenCommand(xmlFile, when);
        });

        validatorMap.put(o -> o instanceof Postgres, (xmlFile, command) -> {
            Postgres postgres = (Postgres) command;
            validateAlias(integrations.getPostgresIntegration().getPostgres(), postgres.getAlias());
            validateFileIfExist(xmlFile, postgres.getFile());
        });

        validatorMap.put(o -> o instanceof Mysql, (xmlFile, command) -> {
            Mysql mysql = (Mysql) command;
            validateAlias(integrations.getMysqlIntegration().getMysql(), mysql.getAlias());
            validateFileIfExist(xmlFile, mysql.getFile());
        });

        validatorMap.put(o -> o instanceof Oracle, (xmlFile, command) -> {
            Oracle oracle = (Oracle) command;
            validateAlias(integrations.getOracleIntegration().getOracle(), oracle.getAlias());
            validateFileIfExist(xmlFile, oracle.getFile());
        });

        validatorMap.put(o -> o instanceof Redis, (xmlFile, command) -> {
            Redis redis = (Redis) command;
            validateAlias(integrations.getRedisIntegration().getRedis(), redis.getAlias());
            validateFileIfExist(xmlFile, redis.getFile());
        });

        validatorMap.put(o -> o instanceof Mongo, (xmlFile, command) -> {
            Mongo mongo = (Mongo) command;
            validateAlias(integrations.getMongoIntegration().getMongo(), mongo.getAlias());
            validateFileIfExist(xmlFile, mongo.getFile());
        });

        validatorMap.put(o -> o instanceof Dynamo, (xmlFile, command) -> {
            Dynamo dynamo = (Dynamo) command;
            validateAlias(integrations.getDynamoIntegration().getDynamo(), dynamo.getAlias());
            validateFileIfExist(xmlFile, dynamo.getFile());
        });

        validatorMap.put(o -> o instanceof Clickhouse, (xmlFile, command) -> {
            Clickhouse clickhouse = (Clickhouse) command;
            validateAlias(integrations.getClickhouseIntegration().getClickhouse(), clickhouse.getAlias());
            validateFileIfExist(xmlFile, clickhouse.getFile());
        });

        validatorMap.put(o -> o instanceof Rabbit, (xmlFile, command) -> {
            Rabbit rabbit = (Rabbit) command;
            validateAlias(integrations.getRabbitmqIntegration().getRabbitmq(), rabbit.getAlias());
            validateRabbitCommand(xmlFile, rabbit);
        });

        validatorMap.put(o -> o instanceof Kafka, (xmlFile, command) -> {
            Kafka kafka = (Kafka) command;
            validateAlias(integrations.getKafkaIntegration().getKafka(), kafka.getAlias());
            validateKafkaCommand(xmlFile, kafka);
        });

        validatorMap.put(o -> o instanceof Sqs, (xmlFile, command) -> {
           Sqs sqs = (Sqs) command;
           validateAlias(integrations.getSqsIntegration().getSqs(), sqs.getAlias());
           validateSqsCommand(xmlFile, sqs);
        });

        validatorMap.put(o -> o instanceof Sendgrid, (xmlFile, command) -> {
            Sendgrid sendgrid = (Sendgrid) command;
            validateAlias(integrations.getSendgridIntegration().getSendgrid(), sendgrid.getAlias());
            validateSendgridCommand(xmlFile, sendgrid);
        });

        validatorMap.put(o -> o instanceof Include, (xmlFile, command) -> {
            Include include = (Include) command;
            validateIncludeAction(include, xmlFile);
        });

        this.abstractCommandValidatorsMap = Collections.unmodifiableMap(validatorMap);
    }

    @Override
    public void validate(final Scenario scenario, final File xmlFile) {
        scenario.getCommands().forEach(it -> validateCommand(it, xmlFile));
    }

    private void validateFileExistenceInData(final String commandFile) {
        if (StringUtils.hasText(commandFile)) {
            FileSearcher.searchFileFromDataFolder(commandFile);
        }
    }

    private void validateFileIfExist(final File xmlFile, final String commandFile) {
        if (StringUtils.hasText(commandFile)) {
            FileSearcher.searchFileFromDir(xmlFile, commandFile);
        }
    }

    private void validateAlias(final List<? extends Integration> integrationsList, final String alias) {
        integrationsList.stream().filter(Integration::isEnabled).filter(o -> o.getAlias().equals(alias))
                .findFirst().orElseThrow(() -> new DefaultFrameworkException(ALIAS_NOT_FOUND, alias));
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

    private void validateUiCommands(final Ui command) {
        command.getClickOrInputOrNavigate().forEach(o -> {
            if (o instanceof Javascript) {
                validateFileExistenceInData(((Javascript) o).getFile());
            }
        });
    }

    private void validateShellCommand(final File xmlFile, final Shell shell) {
        validateFileIfExist(xmlFile, shell.getFile());
        List<String> shellFiles = shell.getShellFile();
        if (!shellFiles.isEmpty()) {
            shellFiles.forEach(this::validateFileExistenceInData);
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
