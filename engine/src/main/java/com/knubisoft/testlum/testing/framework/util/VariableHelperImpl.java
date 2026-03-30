package com.knubisoft.testlum.testing.framework.util;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.DateShift;
import com.knubisoft.testlum.testing.model.scenario.FromConstant;
import com.knubisoft.testlum.testing.model.scenario.FromExpression;
import com.knubisoft.testlum.testing.model.scenario.FromFile;
import com.knubisoft.testlum.testing.model.scenario.FromPath;
import com.knubisoft.testlum.testing.model.scenario.FromRandomGenerate;
import com.knubisoft.testlum.testing.model.scenario.FromSQL;
import com.knubisoft.testlum.testing.model.scenario.FromDate;
import com.knubisoft.testlum.testing.model.scenario.FromAlert;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.cornutum.regexpgen.RandomGen;
import org.cornutum.regexpgen.RegExpGen;
import org.cornutum.regexpgen.RegExpGenBuilder;
import org.cornutum.regexpgen.js.Provider;
import org.cornutum.regexpgen.random.RandomBoundsGen;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.context.ApplicationContext;
import org.openqa.selenium.Alert;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.DOLLAR_SIGN;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SLASH_SEPARATOR;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VAR_QUERY_RESULT_ERROR;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.*;
import static java.util.Objects.nonNull;

@Slf4j
@Component
public class VariableHelperImpl implements VariableHelper {

    private static final String VAR_CONTEXT_LOG = LogFormat.table("Created from");
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";
    private static final String JAVA_COMPATIBLE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String ALLOWED_DATE_LETTERS = "yMdHmsSnuEaZzXx";

    private final Map<RandomPredicate, RandomFunction> randomGenerateMethodMap;

    private final AliasToStorageOperation aliasToStorageOperation;
    private final ResultUtil resultUtil;
    private final LogUtil logUtil;

    public VariableHelperImpl(final ApplicationContext ctx) {
        RandomStringUtils util = RandomStringUtils.secure();
        this.randomGenerateMethodMap = Map.of(
                r -> nonNull(r.getNumeric()), r -> util.nextNumeric(r.getLength()),
                r -> nonNull(r.getAlphabetic()), r -> util.nextAlphabetic(r.getLength()),
                r -> nonNull(r.getAlphanumeric()), r -> util.nextAlphanumeric(r.getLength()),
                r -> nonNull(r.getRandomRegexp()), this::generateStringByRegexp);
        this.aliasToStorageOperation = ctx.getBean(AliasToStorageOperation.class);
        this.resultUtil = ctx.getBean(ResultUtil.class);
        this.logUtil = ctx.getBean(LogUtil.class);
    }

    @Override
    public <T extends AbstractCommand> VarMethod<T> lookupVarMethod(final Map<VarPredicate<T>, VarMethod<T>> methodMap,
                                                                    final T var) {
        return methodMap.entrySet().stream()
                .filter(entry -> entry.getKey().test(var))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.VAR_TYPE_NOT_SUPPORTED,
                        var.getClass().getSimpleName()))
                .getValue();
    }

    @Override
    public String getRandomGenerateResult(final FromRandomGenerate randomGenerate,
                                          final String varName,
                                          final CommandResult result) {
        String valueResult = getRandomGeneratedString(randomGenerate);
        String exp = nonNull(randomGenerate.getRandomRegexp())
                ? randomGenerate.getRandomRegexp().getPattern() : NO_EXPRESSION;
        resultUtil.addVariableMetaData(GENERATED_STRING, varName, exp, valueResult, result);
        return valueResult;
    }

    private String getRandomGeneratedString(final FromRandomGenerate randomGenerate) {
        return randomGenerateMethodMap.entrySet().stream()
                .filter(e -> e.getKey().test(randomGenerate))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.GENERATION_METHOD_NOT_SUPPORTED))
                .getValue().apply(randomGenerate);
    }

    private String generateStringByRegexp(final FromRandomGenerate randomGenerate) {
        int requiredLength = randomGenerate.getLength();
        RandomGen random = new RandomBoundsGen();
        RegExpGen generator = RegExpGenBuilder.generateRegExp(Provider.forEcmaScript())
                .matching(randomGenerate.getRandomRegexp().getPattern());
        return generator.generate(random, requiredLength, requiredLength);
    }

    @Override
    public String getFileResult(final FromFile fromFile,
                                final String varName,
                                final UnaryOperator<String> fileToString,
                                final CommandResult result) {
        String valueResult = fileToString.apply(fromFile.getFileName());
        resultUtil.addVariableMetaData(FILE, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    @Override
    public String getConstantResult(final FromConstant fromConstant,
                                    final String varName,
                                    final CommandResult result) {
        String valueResult = fromConstant.getValue();
        resultUtil.addVariableMetaData(CONSTANT, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    @Override
    public String getExpressionResult(final FromExpression fromExpression,
                                      final String varName,
                                      final CommandResult result) {
        String expression = fromExpression.getValue();
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(expression);
        String valueResult = exp.getValue(String.class);
        resultUtil.addVariableMetaData(EXPRESSION, varName, expression, valueResult, result);
        return valueResult;
    }

    @Override
    @SneakyThrows
    public String getPathResult(final FromPath fromPath,
                                final String varName,
                                final ScenarioContext scenarioContext,
                                final CommandResult result,
                                final UnaryOperator<String> fileToString) {
        String path = fromPath.getValue();
        String body = getBodyFromContext(fromPath, scenarioContext, fileToString);
        if (path.startsWith(DOLLAR_SIGN)) {
            return evaluateJPath(path, varName, body, result);
        }
        if (path.startsWith(SLASH_SEPARATOR)) {
            return evaluateXPath(path, varName, body, result);
        }
        throw new DefaultFrameworkException("Path <%s> is not supported", path);
    }

    private String getBodyFromContext(final FromPath fromPath, final ScenarioContext scenarioContext,
                                      final UnaryOperator<String> fileToString) {
        String body = resolveBodySource(fromPath, scenarioContext, fileToString);
        logVarContextInfo(body);
        return body;
    }

    private void logVarContextInfo(final String body) {
        log.info(VAR_CONTEXT_LOG, body);
    }

    private String resolveBodySource(final FromPath fromPath, final ScenarioContext scenarioContext,
                                     final UnaryOperator<String> fileToString) {
        if (fromPath.getFromFile() == null && fromPath.getFromVar() == null) {
            return scenarioContext.getBody().getValue();
        }
        return fromPath.getFromFile() == null
                ? scenarioContext.get(fromPath.getFromVar())
                : fileToString.apply(fromPath.getFromFile());
    }


    private String evaluateXPath(final String path,
                                 final String varName,
                                 final String body,
                                 final CommandResult result) throws Exception {
        Document jsoupDoc = Jsoup.parse(body, "", Parser.xmlParser());
        org.w3c.dom.Document w3cDocument = convertJsoupToW3CDocument(jsoupDoc);
        XPath xPath = XPathFactory.newInstance().newXPath();
        String valueResult = (String) xPath.evaluate(path, w3cDocument, XPathConstants.STRING);
        resultUtil.addVariableMetaData(XML_PATH, varName, path, valueResult, result);
        return valueResult;
    }

    private org.w3c.dom.Document convertJsoupToW3CDocument(final Document jsoupDoc) throws Exception {
        String htmlContent = jsoupDoc.html();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        InputSource inputSource = new InputSource(new StringReader(htmlContent));
        return factory.newDocumentBuilder().parse(inputSource);
    }

    private String evaluateJPath(final String path,
                                 final String varName,
                                 final String body,
                                 final CommandResult result) {
        DocumentContext contextBody = JsonPath.parse(body);
        String valueResult = Objects.nonNull(contextBody.read(path)) ? contextBody.read(path).toString() : null;
        resultUtil.addVariableMetaData(JSON_PATH, varName, path, valueResult, result);
        return valueResult;
    }

    @Override
    public String getSQLResult(final FromSQL fromSQL,
                               final String varName,
                               final CommandResult result) {
        checkAlias(fromSQL);
        String metadataKey = fromSQL.getDbType().name() + DelimiterConstant.UNDERSCORE + fromSQL.getAlias();
        AbstractStorageOperation storageOperation = aliasToStorageOperation.getByNameOrThrow(metadataKey);
        String valueResult = getActualQueryResult(fromSQL, storageOperation);
        resultUtil.addVariableMetaData(RELATIONAL_DB_QUERY, fromSQL, varName, valueResult, result);
        return valueResult;
    }

    @Override
    public String getAlertResult(final FromAlert fromAlert, final String varName,
                                 final Alert browserAlert, final CommandResult result) {
        String valueResult = browserAlert.getText();
        resultUtil.addVariableMetaData(ALERT, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    @Override
    public String getDateResult(final FromDate fromDate, final String variableName,
                                final CommandResult commandResult) {
        String dateFormatPattern = normalizeDateFormat(fromDate.getFormat());
        ZoneId zoneId = resolveZoneId(fromDate.getTimezone());
        DateTimeFormatter dateTimeFormatter = createDateTimeFormatter(dateFormatPattern);
        ZonedDateTime calculatedDateTime = calculateDateTime(fromDate, dateFormatPattern, dateTimeFormatter, zoneId);
        return formatAndRegisterResult(calculatedDateTime, dateTimeFormatter,
                variableName, dateFormatPattern, commandResult);
    }

    private ZonedDateTime calculateDateTime(final FromDate fromDate, final String dateFormatPattern,
                                            final DateTimeFormatter dateTimeFormatter, final ZoneId zoneId) {
        if (fromDate.getConstant() != null) {
            return parseSpecifiedValue(fromDate.getConstant().getValue(), dateFormatPattern, dateTimeFormatter, zoneId);
        }
        if (fromDate.getBeforeNow() != null) {
            return applyDateShift(ZonedDateTime.now(zoneId), fromDate.getBeforeNow(), -1);
        }
        if (fromDate.getAfterNow() != null) {
            return applyDateShift(ZonedDateTime.now(zoneId), fromDate.getAfterNow(), 1);
        }
        return ZonedDateTime.now(zoneId);
    }

    private String formatAndRegisterResult(final ZonedDateTime dateTime, final DateTimeFormatter dateTimeFormatter,
                                           final String variableName, final String pattern,
                                           final CommandResult commandResult) {
        try {
            String formattedResult = dateTime.format(dateTimeFormatter);
            resultUtil.addVariableMetaData(GENERATED_STRING, variableName, pattern, formattedResult, commandResult);
            return formattedResult;
        } catch (DateTimeException e) {
            throw new DefaultFrameworkException(String.format(ExceptionMessage.DATE_FORMATTING_FAILED,
                                                pattern, e.getMessage()));
        }
    }

    private DateTimeFormatter createDateTimeFormatter(final String dateFormatPattern) {
        validateDateFormatPattern(dateFormatPattern);
        try {
            return new DateTimeFormatterBuilder().appendPattern(dateFormatPattern)
                    .parseDefaulting(ChronoField.YEAR_OF_ERA, ZonedDateTime.now().getYear())
                    .parseDefaulting(ChronoField.MONTH_OF_YEAR, ZonedDateTime.now().getMonthValue())
                    .parseDefaulting(ChronoField.DAY_OF_MONTH, ZonedDateTime.now().getDayOfMonth())
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter();
        } catch (IllegalArgumentException e) {
            throw new DefaultFrameworkException(
                    String.format(ExceptionMessage.INVALID_DATE_FORMAT_PATTERN, dateFormatPattern, e.getMessage()));
        }
    }

    private String normalizeDateFormat(final String dateFormat) {
        return StringUtils.isBlank(dateFormat) ? JAVA_COMPATIBLE_FORMAT : dateFormat;
    }

    private ZoneId resolveZoneId(final String timezone) {
        if (StringUtils.isBlank(timezone)) {
            return ZoneId.systemDefault();
        }
        try {
            String normalizedTimezone = timezone.startsWith("UTC") ? timezone.replace("UTC", "GMT") : timezone;
            return ZoneId.of(normalizedTimezone);
        } catch (Exception e) {
            throw new DefaultFrameworkException(String.format(ExceptionMessage.INVALID_TIMEZONE, timezone));
        }
    }

    private ZonedDateTime parseSpecifiedValue(final String valueToParse, final String dateFormatPattern,
                                              final DateTimeFormatter dateTimeFormatter, final ZoneId zoneId) {
        if (StringUtils.isBlank(valueToParse)) {
            return ZonedDateTime.now(zoneId);
        }
        TemporalAccessor temporalAccessor = parseToTemporalAccessor(valueToParse, dateFormatPattern, dateTimeFormatter);
        return convertToZonedDateTime(temporalAccessor, zoneId, valueToParse, dateFormatPattern);
    }

    private TemporalAccessor parseToTemporalAccessor(final String valueToParse, final String dateFormatPattern,
                                                     final DateTimeFormatter dateTimeFormatter) {
        try {
            return dateTimeFormatter.parse(valueToParse);
        } catch (DateTimeException e) {
            throw new DefaultFrameworkException(
                    String.format(ExceptionMessage.VALUE_DOES_NOT_MATCH_FORMAT, valueToParse, dateFormatPattern));
        }
    }

    private void validateDateFormatPattern(final String dateFormatPattern) {
        if (hasUnquotedInvalidChars(dateFormatPattern)) {
            throw new DefaultFrameworkException(ExceptionMessage.INVALID_DATE_FORMAT_PATTERN,
                    dateFormatPattern, "Pattern contains unsupported letters or unquoted digits");
        }
        verifyPatternFunctionality(dateFormatPattern);
    }

    private void verifyPatternFunctionality(final String dateFormatPattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormatPattern)
                    .withResolverStyle(ResolverStyle.STRICT);
            ZonedDateTime now = ZonedDateTime.now();
            String formatted = formatter.format(now);
            formatter.parse(formatted);
        } catch (RuntimeException e) {
            throw new DefaultFrameworkException(ExceptionMessage.INVALID_DATE_FORMAT_PATTERN,
                    dateFormatPattern, "Invalid pattern syntax: " + e.getMessage());
        }
    }

    private boolean hasUnquotedInvalidChars(String pattern) {
        String cleaned = pattern.replaceAll("'[^']*'", "");
        return cleaned.chars().anyMatch(this::isInvalidChar);
    }

    private boolean isInvalidChar(int c) {
        if (Character.isLetter(c)) {
            return ALLOWED_DATE_LETTERS.indexOf(c) == -1;
        }
        return Character.isDigit(c);
    }

    private ZonedDateTime convertToZonedDateTime(final TemporalAccessor temporalAccessor, final ZoneId zoneId,
                                                 final String originalValue, final String pattern) {
        LocalDate localDate = temporalAccessor.query(TemporalQueries.localDate());
        LocalTime localTime = temporalAccessor.query(TemporalQueries.localTime());

        if (localDate != null) {
            return localTime != null ? ZonedDateTime.of(localDate, localTime, zoneId) : localDate.atStartOfDay(zoneId);
        }
        if (localTime != null) {
            return ZonedDateTime.of(LocalDate.now(zoneId), localTime, zoneId);
        }
        throw new DefaultFrameworkException(
                String.format(ExceptionMessage.POOR_DATETIME_INFORMATION, originalValue, pattern));
    }

    private ZonedDateTime applyDateShift(final ZonedDateTime zonedDateTime, final DateShift dateShift,
                                         final int signMultiplier) {
        int value;
        try {
            value = Integer.parseInt(dateShift.getValue());
        } catch (NumberFormatException e) {
            throw new DefaultFrameworkException(
                    String.format("Invalid date shift value: '%s'. Expected a valid integer.", dateShift.getValue())
            );
        }
        int shiftAmount = value * signMultiplier;
        return calculateDateWithShift(zonedDateTime, dateShift, shiftAmount);
    }

    private ZonedDateTime calculateDateWithShift(final ZonedDateTime zonedDateTime, final DateShift dateShift,
                                                 final int shiftAmount) {
        return switch (dateShift.getUnit()) {
            case MINUTES -> zonedDateTime.plusMinutes(shiftAmount);
            case SECONDS -> zonedDateTime.plusSeconds(shiftAmount);
            case HOURS -> zonedDateTime.plusHours(shiftAmount);
            case DAYS -> zonedDateTime.plusDays(shiftAmount);
            case MONTHS -> zonedDateTime.plusMonths(shiftAmount);
            case YEARS -> zonedDateTime.plusYears(shiftAmount);
        };
    }

    private void checkAlias(final FromSQL fromSQL) {
        if (fromSQL.getAlias() == null) {
            fromSQL.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private String getActualQueryResult(final FromSQL fromSQL, final AbstractStorageOperation storageOperation) {
        String alias = fromSQL.getAlias();
        List<String> singleQuery = new ArrayList<>(Collections.singletonList(fromSQL.getQuery()));
        logUtil.logAllQueries(fromSQL.getDbType().name(), singleQuery, alias);
        AbstractStorageOperation.StorageOperationResult queryResult = storageOperation.apply(
                new ListSource(singleQuery), alias);
        return getResultValue(queryResult, getKeyOfQueryResultValue(queryResult));
    }

    @SuppressWarnings("unchecked")
    private String getResultValue(final AbstractStorageOperation.StorageOperationResult storageOperationResult,
                                  final String key) {
        List<AbstractStorageOperation.QueryResult<?>> rawList =
                (List<AbstractStorageOperation.QueryResult<?>>) storageOperationResult.getRaw();
        List<LinkedCaseInsensitiveMap<String>> content =
                (List<LinkedCaseInsensitiveMap<String>>) rawList.get(0).getContent();
        verifyIfContentNotEmpty(content);
        Map<String, String> mapWithContent = content.get(0);
        return String.valueOf(mapWithContent.get(key));
    }

    private void verifyIfContentNotEmpty(final List<LinkedCaseInsensitiveMap<String>> content) {
        if (content.isEmpty()) {
            throw new DefaultFrameworkException(VAR_QUERY_RESULT_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private String getKeyOfQueryResultValue(final AbstractStorageOperation.StorageOperationResult applyRelationalDb) {
        List<AbstractStorageOperation.QueryResult<?>> rawList =
                (List<AbstractStorageOperation.QueryResult<?>>) applyRelationalDb.getRaw();
        String[] queryParts = rawList.get(0).getQuery().split(DelimiterConstant.SPACE);
        return queryParts[1];
    }
}