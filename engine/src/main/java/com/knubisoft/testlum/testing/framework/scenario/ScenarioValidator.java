package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.UIConfiguration;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
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
import com.knubisoft.testlum.testing.model.global_config.AppiumCapabilities;
import com.knubisoft.testlum.testing.model.global_config.ClickhouseIntegration;
import com.knubisoft.testlum.testing.model.global_config.DynamoIntegration;
import com.knubisoft.testlum.testing.model.global_config.ElasticsearchIntegration;
import com.knubisoft.testlum.testing.model.global_config.GraphqlIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.KafkaIntegration;
import com.knubisoft.testlum.testing.model.global_config.LambdaIntegration;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.MongoIntegration;
import com.knubisoft.testlum.testing.model.global_config.MysqlIntegration;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
    private final UIConfiguration uiConfigs;

    private Map<AbstractCommandPredicate, AbstractCommandValidator> abstractCommandValidatorsMap;
    private Map<AbstractCommandPredicate, AbstractCommandValidator> uiCommandValidatorsMap;

    private final ThreadLocal<List<Map<String, String>>> variationList = ThreadLocal.withInitial(ArrayList::new);

    @PostConstruct
    public void init() {
        this.abstractCommandValidatorsMap = createCommandsValidatorsMap(integrations);
        this.uiCommandValidatorsMap = createUICommandsValidatorMap();
    }

    private @NotNull Map<AbstractCommandPredicate, AbstractCommandValidator>
    createCommandsValidatorsMap(final Integrations integrations) {
        Map<AbstractCommandPredicate, AbstractCommandValidator> map = new LinkedHashMap<>();
        registerApiValidators(map, integrations);
        registerDatabaseValidators(map, integrations);
        registerMessagingValidators(map, integrations);
        registerCloudValidators(map, integrations);
        registerNotificationValidators(map, integrations);
        registerMiscValidators(map);
        registerUiValidators(map);
        return Collections.unmodifiableMap(map);
    }

    private void registerApiValidators(final Map<AbstractCommandPredicate, AbstractCommandValidator> map,
                                       final Integrations integrations) {
        map.put(o -> o instanceof Auth, (f, c) -> validateAuth((Auth) c, integrations));
        map.put(o -> o instanceof Http, (f, c) -> validateHttp(f, (Http) c, integrations));
        map.put(o -> o instanceof Graphql, (f, c) -> validateGraphql(f, (Graphql) c, integrations));
        map.put(o -> o instanceof Websocket, (f, c) -> validateWs(f, (Websocket) c, integrations));
        map.put(o -> o instanceof Elasticsearch, (f, c) -> validateEs(f, (Elasticsearch) c, integrations));
    }

    private void validateAuth(final Auth auth, final Integrations i) {
        checkIntegrationExistence(i.getApis(), Apis.class);
        validateFileExistenceInDataFolder(auth.getCredentials());
        validateAuthCommand(auth);
    }

    private void validateHttp(final File f, final Http http, final Integrations i) {
        checkIntegrationExistence(i.getApis(), Apis.class);
        validateHttpCommand(f, http);
    }

    private void validateGraphql(final File f, final Graphql gql, final Integrations i) {
        checkIntegrationExistence(i.getGraphqlIntegration(), GraphqlIntegration.class);
        validateAlias(i.getGraphqlIntegration().getApi(), gql.getAlias());
        validateGraphqlCommand(f, gql);
    }

    private void validateWs(final File f, final Websocket ws, final Integrations i) {
        checkIntegrationExistence(i.getWebsockets(), Websockets.class);
        validateAlias(i.getWebsockets().getApi(), ws.getAlias());
        validateWebsocketCommand(f, ws);
    }

    private void validateEs(final File f, final Elasticsearch es, final Integrations i) {
        checkIntegrationExistence(i.getElasticsearchIntegration(), ElasticsearchIntegration.class);
        validateAlias(i.getElasticsearchIntegration().getElasticsearch(), es.getAlias());
        validateElasticsearchCommand(f, es);
    }

    private void registerDatabaseValidators(final Map<AbstractCommandPredicate, AbstractCommandValidator> map,
                                            final Integrations integrations) {
        map.put(o -> o instanceof Postgres, (f, c) -> validatePostgres(f, (Postgres) c, integrations));
        map.put(o -> o instanceof Mysql, (f, c) -> validateMysql(f, (Mysql) c, integrations));
        map.put(o -> o instanceof Oracle, (f, c) -> validateOracle(f, (Oracle) c, integrations));
        map.put(o -> o instanceof Clickhouse, (f, c) -> validateClickhouse(f, (Clickhouse) c, integrations));
        map.put(o -> o instanceof Redis, (f, c) -> validateRedis(f, (Redis) c, integrations));
        map.put(o -> o instanceof Mongo, (f, c) -> validateMongo(f, (Mongo) c, integrations));
        map.put(o -> o instanceof Dynamo, (f, c) -> validateDynamo(f, (Dynamo) c, integrations));
    }

    private void validatePostgres(final File f, final Postgres d, final Integrations i) {
        validateDbCmd(f, d.getAlias(), d.getFile(), i.getPostgresIntegration(),
                PostgresIntegration.class, PostgresIntegration::getPostgres);
    }

    private void validateMysql(final File f, final Mysql d, final Integrations i) {
        validateDbCmd(f, d.getAlias(), d.getFile(), i.getMysqlIntegration(),
                MysqlIntegration.class, MysqlIntegration::getMysql);
    }

    private void validateOracle(final File f, final Oracle d, final Integrations i) {
        validateDbCmd(f, d.getAlias(), d.getFile(), i.getOracleIntegration(),
                OracleIntegration.class, OracleIntegration::getOracle);
    }

    private void validateClickhouse(final File f, final Clickhouse d, final Integrations i) {
        validateDbCmd(f, d.getAlias(), d.getFile(), i.getClickhouseIntegration(),
                ClickhouseIntegration.class, ClickhouseIntegration::getClickhouse);
    }

    private void validateRedis(final File f, final Redis d, final Integrations i) {
        validateDbCmd(f, d.getAlias(), d.getFile(), i.getRedisIntegration(),
                RedisIntegration.class, RedisIntegration::getRedis);
    }

    private void validateMongo(final File f, final Mongo d, final Integrations i) {
        validateDbCmd(f, d.getAlias(), d.getFile(), i.getMongoIntegration(),
                MongoIntegration.class, MongoIntegration::getMongo);
    }

    private void validateDynamo(final File f, final Dynamo d, final Integrations i) {
        validateDbCmd(f, d.getAlias(), d.getFile(), i.getDynamoIntegration(),
                DynamoIntegration.class, DynamoIntegration::getDynamo);
    }

    private <I> void validateDbCmd(final File xmlFile, final String alias, final String file,
                                    final I integration, final Class<I> intClass,
                                    final java.util.function.Function<I, List<? extends Integration>> listFn) {
        checkIntegrationExistence(integration, intClass);
        validateAlias(listFn.apply(integration), alias);
        validateFileIfExist(xmlFile, file);
    }

    private void registerMessagingValidators(final Map<AbstractCommandPredicate, AbstractCommandValidator> map,
                                             final Integrations i) {
        map.put(o -> o instanceof Rabbit, (f, c) -> validateRabbit(f, (Rabbit) c, i));
        map.put(o -> o instanceof Kafka, (f, c) -> validateKafka(f, (Kafka) c, i));
        map.put(o -> o instanceof Sqs, (f, c) -> validateSqs(f, (Sqs) c, i));
    }

    private void validateRabbit(final File f, final Rabbit r, final Integrations i) {
        checkIntegrationExistence(i.getRabbitmqIntegration(), RabbitmqIntegration.class);
        validateAlias(i.getRabbitmqIntegration().getRabbitmq(), r.getAlias());
        validateRabbitCommand(f, r);
    }

    private void validateKafka(final File f, final Kafka k, final Integrations i) {
        checkIntegrationExistence(i.getKafkaIntegration(), KafkaIntegration.class);
        validateAlias(i.getKafkaIntegration().getKafka(), k.getAlias());
        validateKafkaCommand(f, k);
    }

    private void validateSqs(final File f, final Sqs s, final Integrations i) {
        checkIntegrationExistence(i.getSqsIntegration(), SqsIntegration.class);
        validateAlias(i.getSqsIntegration().getSqs(), s.getAlias());
        validateSqsCommand(f, s);
    }

    private void registerCloudValidators(final Map<AbstractCommandPredicate, AbstractCommandValidator> map,
                                         final Integrations integrations) {
        map.put(o -> o instanceof S3, (f, c) -> {
            checkIntegrationExistence(integrations.getS3Integration(), S3Integration.class);
            S3 s3 = (S3) c;
            validateAlias(integrations.getS3Integration().getS3(), s3.getAlias());
            validateS3Command(f, s3);
        });
        map.put(o -> o instanceof Lambda, (f, c) -> {
            checkIntegrationExistence(integrations.getLambdaIntegration(), LambdaIntegration.class);
            Lambda l = (Lambda) c;
            validateAlias(integrations.getLambdaIntegration().getLambda(), l.getAlias());
            validateLambdaCommand(f, l);
        });
    }

    private void registerNotificationValidators(final Map<AbstractCommandPredicate, AbstractCommandValidator> map,
                                                final Integrations i) {
        map.put(o -> o instanceof Ses, (f, c) -> validateSes((Ses) c, i));
        map.put(o -> o instanceof Smtp, (f, c) -> validateSmtp((Smtp) c, i));
        map.put(o -> o instanceof Sendgrid, (f, c) -> validateSg(f, (Sendgrid) c, i));
        map.put(o -> o instanceof Twilio, (f, c) -> validateTwilio((Twilio) c, i));
    }

    private void validateSes(final Ses ses, final Integrations i) {
        checkIntegrationExistence(i.getSesIntegration(), SesIntegration.class);
        validateAlias(i.getSesIntegration().getSes(), ses.getAlias());
    }

    private void validateSmtp(final Smtp smtp, final Integrations i) {
        checkIntegrationExistence(i.getSmtpIntegration(), SmtpIntegration.class);
        validateAlias(i.getSmtpIntegration().getSmtp(), smtp.getAlias());
    }

    private void validateSg(final File f, final Sendgrid sg, final Integrations i) {
        checkIntegrationExistence(i.getSendgridIntegration(), SendgridIntegration.class);
        validateAlias(i.getSendgridIntegration().getSendgrid(), sg.getAlias());
        validateSendgridCommand(f, sg);
    }

    private void validateTwilio(final Twilio twilio, final Integrations i) {
        checkIntegrationExistence(i.getTwilioIntegration(), TwilioIntegration.class);
        validateAlias(i.getTwilioIntegration().getTwilio(), twilio.getAlias());
    }

    private void registerMiscValidators(final Map<AbstractCommandPredicate, AbstractCommandValidator> map) {
        map.put(o -> o instanceof Migrate, (xmlFile, command) -> validateExistsDatasets((Migrate) command));
        map.put(o -> o instanceof Shell, (xmlFile, command) -> validateShellCommand(xmlFile, (Shell) command));
        map.put(o -> o instanceof Include, (xmlFile, command) -> validateIncludeAction((Include) command, xmlFile));
        map.put(o -> o instanceof Var, (xmlFile, command) -> {
            Var var = (Var) command;
            validateVarCommand(xmlFile, var.getFile(), var.getSql());
        });
        map.put(o -> o instanceof Repeat, (xmlFile, command) ->
                validateRepeatCommand(((Repeat) command).getVariations()));
    }

    private void registerUiValidators(final Map<AbstractCommandPredicate, AbstractCommandValidator> map) {
        map.put(o -> o instanceof Web, (xmlFile, command) -> validateWebCommands((Web) command, xmlFile));
        map.put(o -> o instanceof Mobilebrowser, (xmlFile, command) ->
                validateMobileBrowserCommands((Mobilebrowser) command, xmlFile));
        map.put(o -> o instanceof Native, (xmlFile, command) ->
                validateNativeCommands((Native) command, xmlFile));
    }

    private @NotNull Map<AbstractCommandPredicate, AbstractCommandValidator> createUICommandsValidatorMap() {
        Map<AbstractCommandPredicate, AbstractCommandValidator> map = new LinkedHashMap<>();
        registerUiVarValidators(map);
        registerUiRepeatValidators(map);
        registerUiFileValidators(map);
        return Collections.unmodifiableMap(map);
    }

    private void registerUiVarValidators(final Map<AbstractCommandPredicate, AbstractCommandValidator> map) {
        map.put(o -> o instanceof WebVar, (xmlFile, command) -> {
            WebVar var = (WebVar) command;
            validateVarCommand(xmlFile, var.getFile(), var.getSql());
        });
        map.put(o -> o instanceof NativeVar, (xmlFile, command) -> {
            NativeVar var = (NativeVar) command;
            validateVarCommand(xmlFile, var.getFile(), var.getSql());
        });
    }

    private void registerUiRepeatValidators(final Map<AbstractCommandPredicate, AbstractCommandValidator> map) {
        map.put(o -> o instanceof WebRepeat, (f, c) -> validateRepeatCommand(((WebRepeat) c).getVariations()));
        map.put(o -> o instanceof NativeRepeat, (f, c) -> validateRepeatCommand(((NativeRepeat) c).getVariations()));
        map.put(o -> o instanceof MobilebrowserRepeat,
                (f, c) -> validateRepeatCommand(((MobilebrowserRepeat) c).getVariations()));
    }

    private void registerUiFileValidators(final Map<AbstractCommandPredicate, AbstractCommandValidator> map) {
        map.put(o -> o instanceof Scroll && ScrollType.INNER == ((Scroll) o).getType(),
                (xmlFile, command) -> validateLocator((Scroll) command));
        map.put(o -> o instanceof Image, (xmlFile, command) ->
                validateFileIfExist(xmlFile, ((Image) command).getFile()));
        map.put(o -> o instanceof DragAndDrop, (xmlFile, command) ->
                validateFileIfExist(xmlFile, ((DragAndDrop) command).getFileName()));
        map.put(o -> o instanceof Javascript, (xmlFile, command) ->
                validateFileExistenceInDataFolder(((Javascript) command).getFile()));
    }

    @Override
    public void validate(final Scenario scenario, final File xmlFile) {
        if (scenario.getSettings().isActive()) {
            try {
                variationList.get().clear();
                validateVariationsIfExist(scenario, xmlFile);
                validateIfContainsNativeAndMobileCommands(scenario.getCommands());
                scenario.getCommands().forEach(command -> validateCommand(command, xmlFile));
            } finally {
                variationList.remove();
            }
        }
    }

    private void validateVariationsIfExist(final Scenario scenario, final File xmlFile) {
        if (StringUtils.isNotBlank(scenario.getSettings().getVariations())) {
            globalVariationsProvider.process(scenario, xmlFile);
            variationList.get().addAll(globalVariationsProvider.getVariations(scenario.getSettings().getVariations()));
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
                && fileName.trim().contains(DelimiterConstant.DOUBLE_CLOSE_BRACE) && !variationList.get().isEmpty()) {
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
                && fileName.trim().contains(DelimiterConstant.DOUBLE_CLOSE_BRACE) && !variationList.get().isEmpty()) {
            validateFileNamesIfVariations(xmlFile, fileName);
        } else if (StringUtils.isNotBlank(fileName) && !fileName.trim().contains(DelimiterConstant.DOUBLE_OPEN_BRACE)
                && !fileName.trim().contains(DelimiterConstant.DOUBLE_CLOSE_BRACE)) {
            fileSearcher.searchFileFromDir(xmlFile, fileName);
        }
    }

    private File validateFileNamesIfVariations(final File xmlFile, final String fileName) {
        String fileNameKey = this.extractFileNameFromVariationVariable(fileName);
        String variationValue = variationList.get().stream()
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

    private void validateVarCommand(final File xmlFile, final FromFile fromFile, final FromSQL fromSQL) {
        if (Objects.nonNull(fromFile)) {
            validateFileIfExist(xmlFile, fromFile.getFileName());
        }
        if (Objects.nonNull(fromSQL)) {
            validateAlias(getDbIntegrationList(fromSQL.getDbType()), fromSQL.getAlias());
        }
    }

    private List<? extends Integration> getDbIntegrationList(final RelationalDB dbType) {
        return switch (dbType) {
            case POSTGRES -> checkedList(integrations.getPostgresIntegration(), PostgresIntegration.class,
                    PostgresIntegration::getPostgres);
            case MYSQL -> checkedList(integrations.getMysqlIntegration(), MysqlIntegration.class,
                    MysqlIntegration::getMysql);
            case ORACLE -> checkedList(integrations.getOracleIntegration(), OracleIntegration.class,
                    OracleIntegration::getOracle);
            case CLICKHOUSE -> checkedList(integrations.getClickhouseIntegration(), ClickhouseIntegration.class,
                    ClickhouseIntegration::getClickhouse);
        };
    }

    private <I> List<? extends Integration> checkedList(final I integration, final Class<I> intClass,
                                                        final java.util.function.Function<I,
                                                                List<? extends Integration>> listFn) {
        checkIntegrationExistence(integration, intClass);
        return listFn.apply(integration);
    }

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
        List<Object> commands = Objects.nonNull(websocket.getStomp())
                ? websocket.getStomp().getSubscribeOrSendOrReceive()
                : websocket.getSendOrReceive();
        commands.stream()
                .map(this::getWebsocketFilename)
                .filter(StringUtils::isNotBlank)
                .forEach(filename -> validateFileIfExist(xmlFile, filename));
    }

    private String getWebsocketFilename(final Object command) {
        if (command instanceof WebsocketSend ws) {
            return ws.getFile();
        } else if (command instanceof WebsocketReceive wr) {
            return wr.getFile();
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
        if (rabbitCommand instanceof SendRmqMessage send) {
            return send.getFile();
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
        if (kafkaCommand instanceof SendKafkaMessage send) {
            return send.getFile();
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
        if (sqsCommand instanceof SendSqsMessage send) {
            return send.getFile();
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
            variationList.get().addAll(globalVariationsProvider.getVariations(variationsFileName));
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
        if (StringUtils.isBlank(command.getLocator())) {
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
