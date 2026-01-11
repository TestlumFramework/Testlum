package com.knubisoft.testlum.testing.framework.util;

import com.github.curiousoddman.rgxgen.RgxGen;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.DOLLAR_SIGN;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SLASH_SEPARATOR;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VAR_QUERY_RESULT_ERROR;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TABLE_FORMAT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CONSTANT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.EXPRESSION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.FILE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.GENERATED_STRING;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.JSON_PATH;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.NO_EXPRESSION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.RELATIONAL_DB_QUERY;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.XML_PATH;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Slf4j
@Component
public class VariableHelperImpl implements VariableHelper {

    private static final String VAR_CONTEXT_LOG = format(TABLE_FORMAT, "Created from", "{}");
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    private final Map<RandomPredicate, RandomFunction> randomGenerateMethodMap;
    @Autowired
    private AliasToStorageOperation aliasToStorageOperation;

    public VariableHelperImpl() {
        randomGenerateMethodMap = Map.of(
                r -> nonNull(r.getNumeric()), r -> RandomStringUtils.randomNumeric(r.getLength()),
                r -> nonNull(r.getAlphabetic()), r -> RandomStringUtils.randomAlphabetic(r.getLength()),
                r -> nonNull(r.getAlphanumeric()), r -> RandomStringUtils.randomAlphanumeric(r.getLength()),
                r -> nonNull(r.getRandomRegexp()), this::generateStringByRegexp);
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
        ResultUtil.addVariableMetaData(GENERATED_STRING, varName, exp, valueResult, result);
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
        RgxGen rgxGen = RgxGen.parse(randomGenerate.getRandomRegexp().getPattern());
        int requiredLength = randomGenerate.getLength();
        StringBuilder randomString = new StringBuilder();
        while (randomString.length() < requiredLength) {
            randomString.append(rgxGen.generate());
        }
        if (randomString.length() > requiredLength) {
            randomString.delete(requiredLength, randomString.length());
        }
        return randomString.toString();
    }

    @Override
    public String getFileResult(final FromFile fromFile,
                                final String varName,
                                final UnaryOperator<String> fileToString,
                                final CommandResult result) {
        String valueResult = fileToString.apply(fromFile.getFileName());
        ResultUtil.addVariableMetaData(FILE, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    @Override
    public String getConstantResult(final FromConstant fromConstant,
                                    final String varName,
                                    final CommandResult result) {
        String valueResult = fromConstant.getValue();
        ResultUtil.addVariableMetaData(CONSTANT, varName, NO_EXPRESSION, valueResult, result);
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
        ResultUtil.addVariableMetaData(EXPRESSION, varName, expression, valueResult, result);
        return valueResult;
    }

    @Override
    @SneakyThrows
    public String getPathResult(final FromPath fromPath,
                                final String varName,
                                final ScenarioContext scenarioContext,
                                final CommandResult result) {
        String path = fromPath.getValue();
        String body = getBodyFromContext(fromPath, scenarioContext);
        if (path.startsWith(DOLLAR_SIGN)) {
            return evaluateJPath(path, varName, body, result);
        }
        if (path.startsWith(SLASH_SEPARATOR)) {
            return evaluateXPath(path, varName, body, result);
        }
        throw new DefaultFrameworkException("Path <%s> is not supported", path);
    }

    private String getBodyFromContext(final FromPath fromPath, final ScenarioContext scenarioContext) {
        String body = resolveBodySource(fromPath, scenarioContext);
        logVarContextInfo(body);
        return body;
    }

    private void logVarContextInfo(final String body) {
        log.info(VAR_CONTEXT_LOG, body);
    }

    private String resolveBodySource(final FromPath fromPath, final ScenarioContext scenarioContext) {
        if (fromPath.getFromFile() == null && fromPath.getFromVar() == null) {
            return scenarioContext.getBody().getValue();
        }
        return fromPath.getFromFile() == null
                ? scenarioContext.get(fromPath.getFromVar())
                : scenarioContext.get(fromPath.getFromFile());
    }


    private String evaluateXPath(final String path,
                                 final String varName,
                                 final String body,
                                 final CommandResult result) throws Exception {
        Document jsoupDoc = Jsoup.parse(body, "", Parser.xmlParser());
        org.w3c.dom.Document w3cDocument = convertJsoupToW3CDocument(jsoupDoc);
        XPath xPath = XPathFactory.newInstance().newXPath();
        String valueResult = (String) xPath.evaluate(path, w3cDocument, XPathConstants.STRING);
        ResultUtil.addVariableMetaData(XML_PATH, varName, path, valueResult, result);
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
        ResultUtil.addVariableMetaData(JSON_PATH, varName, path, valueResult, result);
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
        ResultUtil.addVariableMetaData(RELATIONAL_DB_QUERY, fromSQL, varName, valueResult, result);
        return valueResult;
    }

    @Override
    public String getDateResult(final FromDate fromDate, final String varName, final CommandResult result) {
        String format = fromDate.getFormat();
        if (format == null || format.trim().isEmpty()) {
            format = "yyyy-MM-dd";
        }

        String valueResult = calculateDate(fromDate, format);
        ResultUtil.addVariableMetaData(GENERATED_STRING, varName, format, valueResult, result);
        return valueResult;
    }

    private String calculateDate(FromDate fromDate, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime dateTime = LocalDateTime.now();

        if (fromDate.getSpecified() != null) {
            String value = fromDate.getSpecified().getValue();
            dateTime = parseSpecifiedValue(value, format);
            return dateTime.format(formatter);
        }

        if (fromDate.getRelative() != null) {
            String shift = fromDate.getRelative().getShift();
            if (shift != null && !shift.trim().isEmpty()) {
                dateTime = applyShift(dateTime, shift);
            }
            return dateTime.format(formatter);
        }

        return dateTime.format(formatter);
    }

    private LocalDateTime parseSpecifiedValue(String value, String pattern) {
        if (value == null || value.trim().isEmpty()) {
            return LocalDateTime.now();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        TemporalAccessor accessor = formatter.parse(value);

        LocalDate date = accessor.query(TemporalQueries.localDate());
        LocalTime time = accessor.query(TemporalQueries.localTime());

        if (date != null && time != null) {
            return LocalDateTime.of(date, time);
        }

        if (date != null) {
            return date.atStartOfDay();
        }

        if (time != null) {
            return time.atDate(LocalDate.now());
        }

        throw new DefaultFrameworkException(
                String.format("Value '%s' matches format '%s', but does not contain enough information to build a LocalDateTime.", value, pattern)
        );
    }

    private LocalDateTime applyShift(LocalDateTime dateTime, String shift) {
        String[] parts = shift.trim().split("\\s+");
        if (parts.length < 2) return dateTime;

        try {
            int amount = Integer.parseInt(parts[0]);
            String unitText = parts[1];

            return ShiftUnit.apply(dateTime, amount, unitText);

        } catch (NumberFormatException e) {
            return dateTime;
        }
    }

    private void checkAlias(final FromSQL fromSQL) {
        if (fromSQL.getAlias() == null) {
            fromSQL.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private String getActualQueryResult(final FromSQL fromSQL, final AbstractStorageOperation storageOperation) {
        String alias = fromSQL.getAlias();
        List<String> singleQuery = new ArrayList<>(Collections.singletonList(fromSQL.getQuery()));
        LogUtil.logAllQueries(fromSQL.getDbType().name(), singleQuery, alias);
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

    private enum ShiftUnit {
        DAY("day", LocalDateTime::plusDays),
        MONTH("month", LocalDateTime::plusMonths),
        YEAR("year", LocalDateTime::plusYears),
        HOUR("hour", LocalDateTime::plusHours),
        MINUTE("minute", LocalDateTime::plusMinutes),
        SECOND("second", LocalDateTime::plusSeconds);

        private final String prefix;
        private final BiFunction<LocalDateTime, Long, LocalDateTime> operation;

        ShiftUnit(String prefix, BiFunction<LocalDateTime, Long, LocalDateTime> operation) {
            this.prefix = prefix;
            this.operation = operation;
        }

        public static LocalDateTime apply(LocalDateTime dateTime, int amount, String text) {
            String lowerText = text.toLowerCase(Locale.ROOT);

            return Arrays.stream(values())
                    .filter(unit -> lowerText.startsWith(unit.prefix))
                    .findFirst()
                    .map(unit -> unit.operation.apply(dateTime, (long) amount))
                    .orElse(dateTime);
        }
    }
}