package com.knubisoft.testlum.testing.framework.util;

import com.github.curiousoddman.rgxgen.RgxGen;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.FromConstant;
import com.knubisoft.testlum.testing.model.scenario.FromExpression;
import com.knubisoft.testlum.testing.model.scenario.FromFile;
import com.knubisoft.testlum.testing.model.scenario.FromPath;
import com.knubisoft.testlum.testing.model.scenario.FromRandomGenerate;
import com.knubisoft.testlum.testing.model.scenario.FromSQL;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.DOLLAR_SIGN;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SLASH_SEPARATOR;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VAR_QUERY_RESULT_ERROR;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CONSTANT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.EXPRESSION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.FILE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.GENERATED_STRING;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.JSON_PATH;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.NO_EXPRESSION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.RELATIONAL_DB_QUERY;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.XML_PATH;
import static java.util.Objects.nonNull;

@Slf4j
@Component
public class VariableHelper {

    private final Map<RandomPredicate, RandomFunction> randomGenerateMethodMap;
    @Autowired
    private NameToAdapterAlias nameToAdapterAlias;

    public VariableHelper() {
        Map<RandomPredicate, RandomFunction> functionMap = new HashMap<>();
        functionMap.put(r -> nonNull(r.getNumeric()), r -> RandomStringUtils.randomNumeric(r.getLength()));
        functionMap.put(r -> nonNull(r.getAlphabetic()), r -> RandomStringUtils.randomAlphabetic(r.getLength()));
        functionMap.put(r -> nonNull(r.getAlphanumeric()), r -> RandomStringUtils.randomAlphanumeric(r.getLength()));
        functionMap.put(r -> nonNull(r.getRandomRegexp()), this::generateStringByRegexp);
        randomGenerateMethodMap = Collections.unmodifiableMap(functionMap);
    }

    public <T extends AbstractCommand> VarMethod<T> lookupVarMethod(final Map<VarPredicate<T>, VarMethod<T>> methodMap,
                                                                    final T var) {
        return methodMap.entrySet().stream()
                .filter(entry -> entry.getKey().test(var))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.VAR_TYPE_NOT_SUPPORTED,
                        var.getClass().getSimpleName()))
                .getValue();
    }

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
        RgxGen rgxGen = new RgxGen(randomGenerate.getRandomRegexp().getPattern());
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

    public String getFileResult(final FromFile fromFile,
                                final File file,
                                final String varName,
                                final CommandResult result) {
        String valueResult = FileSearcher.searchFileToString(fromFile.getFileName(), file);
        ResultUtil.addVariableMetaData(FILE, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    public String getConstantResult(final FromConstant fromConstant,
                                    final String varName,
                                    final CommandResult result) {
        String valueResult = fromConstant.getValue();
        ResultUtil.addVariableMetaData(CONSTANT, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

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

    @SneakyThrows
    public String getPathResult(final FromPath fromPath,
                                final String varName,
                                final ScenarioContext scenarioContext,
                                final CommandResult result) {
        String path = fromPath.getValue();
        if (path.startsWith(DOLLAR_SIGN)) {
            return evaluateJPath(path, varName, scenarioContext, result);
        }
        if (path.startsWith(SLASH_SEPARATOR)) {
            return evaluateXPath(path, varName, scenarioContext, result);
        }
        throw new DefaultFrameworkException("Path <%s> is not supported", path);
    }


    private String evaluateXPath(final String path,
                                 final String varName,
                                 final ScenarioContext scenarioContext,
                                 final CommandResult result) throws Exception {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource body = new InputSource(new StringReader(scenarioContext.getBody()));
        Document document = documentBuilder.parse(body);
        XPath xPath = XPathFactory.newInstance().newXPath();
        String valueResult = xPath.evaluate(path, document);
        ResultUtil.addVariableMetaData(XML_PATH, varName, path, valueResult, result);
        return valueResult;
    }

    private String evaluateJPath(final String path,
                                 final String varName,
                                 final ScenarioContext scenarioContext,
                                 final CommandResult result) {
        DocumentContext contextBody = JsonPath.parse(scenarioContext.getBody());
        String valueResult = Objects.nonNull(contextBody.read(path)) ? contextBody.read(path).toString() : null;
        ResultUtil.addVariableMetaData(JSON_PATH, varName, path, valueResult, result);
        return valueResult;
    }

    public String getSQLResult(final FromSQL fromSQL,
                               final String varName,
                               final CommandResult result) {
        String metadataKey = fromSQL.getDbType().name() + DelimiterConstant.UNDERSCORE + fromSQL.getAlias();
        StorageOperation storageOperation = nameToAdapterAlias.getByNameOrThrow(metadataKey).getStorageOperation();
        String valueResult = getActualQueryResult(fromSQL, storageOperation);
        ResultUtil.addVariableMetaData(RELATIONAL_DB_QUERY, varName, fromSQL.getQuery(), valueResult, result);
        return valueResult;
    }

    private String getActualQueryResult(final FromSQL fromSQL, final StorageOperation storageOperation) {
        String alias = fromSQL.getAlias();
        List<String> singleQuery = new ArrayList<>(Collections.singletonList(fromSQL.getQuery()));
        LogUtil.logAllQueries(singleQuery, alias);
        StorageOperation.StorageOperationResult queryResult = storageOperation.apply(
                new ListSource(singleQuery), alias);
        return getResultValue(queryResult, getKeyOfQueryResultValue(queryResult));
    }

    @SuppressWarnings("unchecked")
    private String getResultValue(final StorageOperation.StorageOperationResult storageOperationResult,
                                  final String key) {
        List<StorageOperation.QueryResult<?>> rawList =
                (List<StorageOperation.QueryResult<?>>) storageOperationResult.getRaw();
        List<LinkedCaseInsensitiveMap<String>> content =
                (List<LinkedCaseInsensitiveMap<String>>) rawList.get(0).getContent();
        verifyIfContentNotEmpty(content);
        Map<String, String> mapWithContent = content.get(0);
        return String.valueOf(mapWithContent.get(key));
    }

    private void verifyIfContentNotEmpty(final List<LinkedCaseInsensitiveMap<String>> content) {
        if (content.size() < 1) {
            throw new DefaultFrameworkException(VAR_QUERY_RESULT_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private String getKeyOfQueryResultValue(final StorageOperation.StorageOperationResult applyRelationalDb) {
        List<StorageOperation.QueryResult<?>> rawList =
                (List<StorageOperation.QueryResult<?>>) applyRelationalDb.getRaw();
        String[] queryParts = rawList.get(0).getQuery().split(DelimiterConstant.SPACE);
        return queryParts[1];
    }

    public interface VarPredicate<T extends AbstractCommand> extends Predicate<T> { }
    public interface VarMethod<T extends AbstractCommand> extends BiFunction<T, CommandResult, String> { }
    private interface RandomPredicate extends Predicate<FromRandomGenerate> { }
    private interface RandomFunction extends Function<FromRandomGenerate, String> { }
}
