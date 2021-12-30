package com.knubisoft.e2e.testing.framework.scenario;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.model.global_config.Postgres;
import com.knubisoft.e2e.testing.model.scenario.AbstractCommand;
import com.knubisoft.e2e.testing.model.scenario.CommandWithLocator;
import com.knubisoft.e2e.testing.model.scenario.Dynamo;
import com.knubisoft.e2e.testing.model.scenario.Elasticsearch;
import com.knubisoft.e2e.testing.model.scenario.Http;
import com.knubisoft.e2e.testing.model.scenario.Include;
import com.knubisoft.e2e.testing.model.scenario.PostgresResult;
import com.knubisoft.e2e.testing.model.scenario.S3;
import com.knubisoft.e2e.testing.model.scenario.Var;
import com.knubisoft.e2e.testing.framework.db.source.FileSource;
import com.knubisoft.e2e.testing.framework.locator.GlobalLocators;
import com.knubisoft.e2e.testing.framework.parser.XMLValidator;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.framework.util.HttpUtil;
import com.knubisoft.e2e.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import com.knubisoft.e2e.testing.model.scenario.HttpInfo;
import com.knubisoft.e2e.testing.model.scenario.Migrate;
import com.knubisoft.e2e.testing.model.scenario.Mongo;
import com.knubisoft.e2e.testing.model.scenario.Mysql;
import com.knubisoft.e2e.testing.model.scenario.Oracle;
import com.knubisoft.e2e.testing.model.scenario.Redis;
import com.knubisoft.e2e.testing.model.scenario.Repeat;
import com.knubisoft.e2e.testing.model.scenario.Response;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
import com.knubisoft.e2e.testing.model.scenario.When;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.FAILED_CONNECTION_TO_DATABASE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCENARIO_CANNOT_BE_INCLUDED_TO_ITSELF;
import static java.lang.String.format;

public class ScenarioValidator implements XMLValidator<Scenario> {

    private final Map<AbstractCommandPredicate, AbstractCommandValidator> abstractCommandValidatorsMap;
    private final FileSearcher fileSearcher;
    private final File patchesFolder;
    private final TestResourceSettings testResourceSettings;


    public ScenarioValidator(final FileSearcher fileSearcher) {
        this.fileSearcher = fileSearcher;
        this.testResourceSettings = TestResourceSettings.getInstance();
        this.patchesFolder = testResourceSettings.getPatchesFolder();

        Map<AbstractCommandPredicate, AbstractCommandValidator> validatorMap = new HashMap<>();

        validatorMap.put(o -> o instanceof Auth, (xmlFile, command) -> {
            Auth auth = (Auth) command;
            validateFileExistence(testResourceSettings.getCredentialsFolder(), auth.getCredentials());
        });

        validatorMap.put(o -> o instanceof Http, (xmlFile, command) -> {
            Http http = (Http) command;
            validateHttpCommand(http, xmlFile);
        });

        validatorMap.put(o -> o instanceof Migrate, (xmlFile, command) -> {
            Migrate migrate = (Migrate) command;
            validateExistsPatches(migrate);
        });

        validatorMap.put(o -> o instanceof Elasticsearch, (xmlFile, command) -> {
            Elasticsearch elasticsearch = (Elasticsearch) command;
            validateElasticsearchCommand(xmlFile, elasticsearch);
        });

        validatorMap.put(o -> o instanceof S3, (xmlFile, command) -> {
            S3 s3 = (S3) command;
            validateS3Command(xmlFile, s3);
        });

        validatorMap.put(o -> o instanceof When, (xmlFile, command) -> {
            When when = (When) command;
            validateWhenCommand(xmlFile, when);
        });

        validatorMap.put(o -> o instanceof com.knubisoft.e2e.testing.model.scenario.Postgres, (xmlFile, command) -> {
            com.knubisoft.e2e.testing.model.scenario.Postgres postgres = (com.knubisoft.e2e.testing.model.scenario.Postgres) command;
            validateFileExistence(xmlFile, postgres.getFile());
        });

        validatorMap.put(o -> o instanceof Mysql, (xmlFile, command) -> {
            Mysql mysql = (Mysql) command;
            validateFileExistence(xmlFile, mysql.getFile());
        });

        validatorMap.put(o -> o instanceof Oracle, (xmlFile, command) -> {
            Oracle oracle = (Oracle) command;
            validateFileExistence(xmlFile, oracle.getFile());
        });

        validatorMap.put(o -> o instanceof Redis, (xmlFile, command) -> {
            Redis redis = (Redis) command;
            validateFileExistence(xmlFile, redis.getFile());
        });

        validatorMap.put(o -> o instanceof Mongo, (xmlFile, command) -> {
            Mongo mongo = (Mongo) command;
            validateFileExistence(xmlFile, mongo.getFile());
        });

        validatorMap.put(o -> o instanceof Dynamo, (xmlFile, command) -> {
            Dynamo dynamo = (Dynamo) command;
            validateFileExistence(xmlFile, dynamo.getFile());
        });

        this.abstractCommandValidatorsMap = Collections.unmodifiableMap(validatorMap);
    }

    @Override
    public void validate(final Scenario scenario, final File xmlFile) {
        if (scenario.isActive()) {
            scenario.getCommands().forEach(it -> validateAction(it, xmlFile, scenario));
        }
        validateReadonly(scenario);
        validatePostgresTests(scenario, xmlFile);
    }

    private void validateFileExistence(final File xmlFile, final String commandFile) {
        if (org.springframework.util.StringUtils.hasText(commandFile)) {
            fileSearcher.search(xmlFile, commandFile);
        }
    }

    private void validateElasticsearchCommand(final File xmlFile, final Elasticsearch elasticsearch) {
        Stream.of(elasticsearch.getPost(), elasticsearch.getGet(), elasticsearch.getPut(), elasticsearch.getDelete())
                .filter(Objects::nonNull)
                .filter(v -> org.springframework.util.StringUtils.hasText(v.getResponse().getFile()))
                .forEach(v -> fileSearcher.search(xmlFile, v.getResponse().getFile()));
    }

    private void validateWhenCommand(final File xmlFile, final When when) {
        Stream.of(when.getRequest(), when.getThen())
                .filter(org.springframework.util.StringUtils::hasText)
                .forEach(v -> fileSearcher.search(xmlFile, v));
    }

    private void validateS3Command(final File xmlFile, final S3 s3) {
        Stream.of(s3.getDownload(), s3.getUpload())
                .filter(org.springframework.util.StringUtils::hasText)
                .forEach(v -> fileSearcher.search(xmlFile, v));
    }

    private void validateExistsPatches(final Migrate migrate) {
        List<String> patches = migrate.getPatches();
        patches.forEach(each -> fileSearcher.search(testResourceSettings.getPatchesFolder(), each));
    }

    private void validateHttpCommand(final Http http, final File xmlFile) {
        HttpInfo httpInfo = HttpUtil.getHttpMethodMetadata(http).getHttpInfo();
        Response response = httpInfo.getResponse();
        if (response != null && response.getFile() != null) {
            fileSearcher.search(xmlFile, response.getFile());
        }
    }

    //CHECKSTYLE:OFF
    private void validatePostgresTests(final Scenario scenario,
                                       final File xmlFile) {
        boolean isPostgresEnabled = GlobalTestConfigurationProvider.provide().getPostgreses()
                .getPostgres().stream().anyMatch(Postgres::isEnabled);
        if (!isPostgresEnabled) {
            for (AbstractCommand command : scenario.getCommands()) {
                offScenarioIfPostgresDisabled(scenario, command);
                if (command instanceof Auth) {
                    validateAuthAction((Auth) command, xmlFile, scenario);
                } else if (command instanceof Repeat) {
                    validateRepeatAction((Repeat) command, xmlFile, scenario);
                }
            }
        }
    }
    private void validateAction(final AbstractCommand command,
                                final File xmlFile,
                                final Scenario scenario) {
        if (command instanceof Auth) {
            validateAuthAction((Auth) command, xmlFile, scenario);
        } else if (command instanceof Repeat) {
            validateRepeatAction((Repeat) command, xmlFile, scenario);
        } else if (command instanceof Include) {
            validateIncludeAction((Include) command, xmlFile);
        } else {
            validateAbstractCommand(command, xmlFile);
        }
    }

    //CHECKSTYLE:ON

    private void validateAuthAction(final Auth auth,
                                    final File xmlFile,
                                    final Scenario scenario) {
        for (AbstractCommand each : auth.getCommands()) {
            offScenarioIfPostgresDisabled(scenario, each);
            validateAbstractCommand(each, xmlFile);
        }
    }

    private void validateRepeatAction(final Repeat repeat,
                                      final File xmlFile,
                                      final Scenario scenario) {
        for (AbstractCommand each : repeat.getCommands()) {
            offScenarioIfPostgresDisabled(scenario, each);
            validateAbstractCommand(each, xmlFile);
        }
    }

    private void offScenarioIfPostgresDisabled(final Scenario scenario,
                                               final AbstractCommand command) {
        if (command instanceof com.knubisoft.e2e.testing.model.scenario.Postgres || command instanceof Migrate) {
            scenario.setActive(false);
        }
    }
    //CHECKSTYLE:OFF

    private void validateAbstractCommand(final AbstractCommand command, final File xmlFile) {
        if (command instanceof CommandWithLocator) {
            validateExistsLocator((CommandWithLocator) command);
        } else if (command instanceof Http) {
            validateHttpCommand((Http) command, xmlFile);
        } else if (command instanceof com.knubisoft.e2e.testing.model.scenario.Postgres) {
            validatePostgresCommand((com.knubisoft.e2e.testing.model.scenario.Postgres) command, xmlFile);
        } else if (command instanceof Migrate) {
            validateExistsPatches((Migrate) command);
        } else if (command instanceof Var) {
            validateVarCommand((Var) command);
        }
    }

    private void validateVarCommand(final Var command) {
        if (command.getPostgresResult() != null) {
            checkDatabaseConnection(command.getPostgresResult());
        }
    }
    private void checkDatabaseConnection(final PostgresResult postgresResult) {
        GlobalTestConfiguration configuration = GlobalTestConfigurationProvider.provide();
        for (Postgres postgres
                : configuration.getPostgreses().getPostgres()) {
            if ((postgres.getAlias().equals(postgresResult.getDatabaseName())
                    && !postgres.isEnabled())) {
                throw new DefaultFrameworkException(format(FAILED_CONNECTION_TO_DATABASE,
                        postgresResult.getDatabaseName()));
            }
        }
    }

    //CHECKSTYLE:ON

    private void validateExistsLocator(final CommandWithLocator command) {
        String id = command.getLocatorId();
        if (id != null) {
            GlobalLocators.getInstance().getLocator(id);
        }
    }

    private void validateIncludeAction(final Include include, final File xmlFile) {
        if (StringUtils.isNotEmpty(include.getScenario())) {
            File includedScenarioFolder = new File(TestResourceSettings.getInstance().getScenariosFolder(),
                    include.getScenario());
            File includedFile = fileSearcher.search(includedScenarioFolder, TestResourceSettings.SCENARIO_FILENAME);
            if (includedFile.equals(xmlFile)) {
                throw new DefaultFrameworkException(SCENARIO_CANNOT_BE_INCLUDED_TO_ITSELF);
            }
        }
    }

    private void validatePostgresCommand(final com.knubisoft.e2e.testing.model.scenario.Postgres postgres, final File xmlFile) {
        if (!postgres.getFile().isEmpty() && postgres.getFile() != null) {
            fileSearcher.search(xmlFile, postgres.getFile());
        }
    }

    //CHECKSTYLE:OFF
    private void validateReadonly(final Scenario scenario) {
        boolean turnOffReadonly = false;
        for (AbstractCommand command : scenario.getCommands()) {
            if (command instanceof Migrate) {
                turnOffReadonly = arePatchesMutating(((Migrate) command));
            } else if (command instanceof com.knubisoft.e2e.testing.model.scenario.Postgres) {
                turnOffReadonly = isQueryContainsMutatingAction(((com.knubisoft.e2e.testing.model.scenario.Postgres) command).getQuery());
            } else if (command instanceof Auth) {
                turnOffReadonly = true;
            } else if (command instanceof Http) {
                turnOffReadonly = isHttpContainsMutatingAction((Http) command);
            }
            if (turnOffReadonly) {
                scenario.getTags().setReadonly(false);
                return;
            }
        }
    }
    //CHECKSTYLE:ON

    private boolean arePatchesMutating(final Migrate migrate) {
        return migrate.getPatches()
                .stream()
                .map(each -> createFileSource(patchesFolder, each))
                .anyMatch(i -> isQueryContainsMutatingAction(i.getQueries()));
    }

    private FileSource createFileSource(final File patchesFolder, final String patchFileName) {
        return new FileSource(new File(patchesFolder, patchFileName));
    }

    private boolean isHttpContainsMutatingAction(final Http command) {
        return command.getPost() != null
                || command.getPut() != null
                || command.getPatch() != null
                || command.getDelete() != null;
    }

    private boolean isQueryContainsMutatingAction(final List<String> query) {
        return query.stream().map(String::toUpperCase).anyMatch(i -> i.contains("UPDATE")
                || i.contains("DELETE")
                || i.contains("INSERT"));
    }

    private interface AbstractCommandPredicate extends Predicate<AbstractCommand> {

    }

    private interface AbstractCommandValidator extends BiConsumer<File, AbstractCommand> {

    }

}
