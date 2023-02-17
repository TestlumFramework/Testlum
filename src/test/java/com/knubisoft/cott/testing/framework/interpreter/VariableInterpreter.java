package com.knubisoft.cott.testing.framework.interpreter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.ListSource;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.By;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.FromSQL;
import com.knubisoft.cott.testing.model.scenario.Var;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.VAR_QUERY_RESULT_ERROR;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_VARIABLE_WITH_PATH_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CONSTANT;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.COOKIES;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.DOM;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.FILE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.JSON_PATH;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NO_EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.RELATIONAL_DB_QUERY;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.XML_PATH;

@Slf4j
@InterpreterForClass(Var.class)
public class VariableInterpreter extends AbstractInterpreter<Var> {
    @Autowired(required = false)
    private NameToAdapterAlias nameToAdapterAlias;
    private final Map<AbstractResourcePredicate, AbstractResourceGetter> abstractResourceMap;

    public VariableInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        Map<AbstractResourcePredicate, AbstractResourceGetter> resourceMap = new HashMap<>();
        resourceMap.put(var -> Objects.nonNull(var.getFromSQL()),
                (var, result) -> getDbResult(var.getFromSQL(), var.getName(), result));
        resourceMap.put(var -> Objects.nonNull(var.getFromFile()),
                (var, result) -> getFileResult(var.getFromFile().getFile(), var.getName(), result));
        resourceMap.put(var -> Objects.nonNull(var.getFromExpression()),
                (var, result) -> getExpressionResult(var.getFromExpression().getValue(), var.getName(), result));
        resourceMap.put(var -> Objects.nonNull(var.getFromConstant()),
                (var, result) -> getValueResult(var.getFromConstant().getValue(), var.getName(), result));
        resourceMap.put(var -> Objects.nonNull(var.getFromPath()),
                (var, result) -> getPathResult(var.getFromPath().getPath(), var.getName(), result));
        resourceMap.put(var -> Objects.nonNull(var.getFromCookie()),
                (var, result) -> getWebCookiesResult(var.getName(), result));
        resourceMap.put(var -> Objects.nonNull(var.getFromDom()),
                (var, result) -> getDomResult(var.getFromDom().getLocator(), var.getName(), result));
        abstractResourceMap = Collections.unmodifiableMap(resourceMap);
    }

    @Override
    protected void acceptImpl(final Var var, final CommandResult result) {
        try {
            setContextVariable(var, result);
        } catch (Exception e) {
            log.info(FAILED_VARIABLE_WITH_PATH_LOG, var.getName(), var.getComment());
            throw e;
        }
    }

    private void setContextVariable(final Var var, final CommandResult result) {
        String value = getValueForContext(var, result);
        dependencies.getScenarioContext().set(var.getName(), value);
        LogUtil.logVarInfo(var.getName(), value);
    }

    private String getValueForContext(final Var var, final CommandResult result) {
        return abstractResourceMap.keySet().stream()
                .filter(key -> key.test(var))
                .findFirst()
                .map(abstractResourceMap::get)
                .map(v -> v.apply(var, result)).get();

    }

    private String getDomResult(final String locator, final String varName, final CommandResult result) {
        String valueResult = dependencies.getWebDriver().findElement(By.xpath(locator)).getText();
        ResultUtil.addVariableMetaData(DOM, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getFileResult(final String filePath, final String varName, final CommandResult result) {
        String valueResult = FileSearcher.searchFileToString(filePath, dependencies.getFile());
        ResultUtil.addVariableMetaData(FILE, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getValueResult(final String value, final String varName, final CommandResult result) {
        String valueResult = inject(value);
        ResultUtil.addVariableMetaData(CONSTANT, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getWebCookiesResult(final String varName, final CommandResult result) {
        Set<Cookie> cookies = dependencies.getWebDriver().manage().getCookies();
        String valueResult = cookies.stream()
                .map(cookie -> String.join(DelimiterConstant.EQUALS_MARK, cookie.getName(), cookie.getValue()))
                .collect(Collectors.joining(DelimiterConstant.SEMICOLON));
        ResultUtil.addVariableMetaData(COOKIES, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getExpressionResult(final String expression, final String varName, final CommandResult result) {
        String injectedExpression = inject(expression);
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(injectedExpression);
        String valueResult = Objects.requireNonNull(exp.getValue()).toString();
        ResultUtil.addVariableMetaData(EXPRESSION, varName, expression, valueResult, result);
        return valueResult;
    }

    @SneakyThrows
    private String getPathResult(final String path, final String varName, final CommandResult result) {
        if (path.startsWith("$")) {
            return evaluateJPath(path, varName, result);
        }
        if (path.startsWith("/") || path.startsWith("//")) {
            return evaluateXPath(path, varName, result);
        }
        throw new DefaultFrameworkException("Path <%s> is not supported", path);
    }

    @SneakyThrows
    private String evaluateXPath(final String path, final String varName, final CommandResult result) {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder
                .parse(new InputSource(new StringReader(dependencies.getScenarioContext().getBody())));
        XPath xPath = XPathFactory.newInstance().newXPath();
        String valueResult = xPath.evaluate(path, document);
        ResultUtil.addVariableMetaData(XML_PATH, varName, path, valueResult, result);
        return valueResult;
    }

    private String evaluateJPath(final String path, final String varName, final CommandResult result) {
        DocumentContext contextBody = JsonPath.parse(dependencies.getScenarioContext().getBody());
        String valueResult = contextBody.read(path).toString();
        ResultUtil.addVariableMetaData(JSON_PATH, varName, path, valueResult, result);
        return valueResult;
    }

    private String getDbResult(final FromSQL dbResult, final String varName, final CommandResult result) {
        String metadataKey = dbResult.getDbType().name() + DelimiterConstant.UNDERSCORE + dbResult.getAlias();
        StorageOperation storageOperation = nameToAdapterAlias.getByNameOrThrow(metadataKey).getStorageOperation();
        String valueResult = getActualRelationalDbResult(dbResult, storageOperation);
        ResultUtil.addVariableMetaData(RELATIONAL_DB_QUERY, varName, dbResult.getQuery(), valueResult, result);
        return valueResult;
    }

    private String getActualRelationalDbResult(final FromSQL relationalDbResult,
                                               final StorageOperation storageOperation) {
        String alias = relationalDbResult.getAlias();
        List<String> singleQuery = new ArrayList<>(Collections.singletonList(relationalDbResult.getQuery()));
        LogUtil.logAllQueries(singleQuery, alias);
        StorageOperation.StorageOperationResult queryResult =
                storageOperation.apply(new ListSource(singleQuery), inject(alias));
        return getResultValue(queryResult, getKeyOfQueryResultValue(queryResult));
    }

    private String getResultValue(final StorageOperation.StorageOperationResult storageOperationResult,
                                  final String key) {
        ArrayList<StorageOperation.QueryResult> rawList =
                (ArrayList<StorageOperation.QueryResult>) storageOperationResult.getRaw();
        ArrayList<LinkedCaseInsensitiveMap<String>> content =
                (ArrayList<LinkedCaseInsensitiveMap<String>>) rawList.get(0).getContent();
        verifyIfContentNotEmpty(content);
        Map<String, String> mapWithContent = content.get(0);
        return String.valueOf(mapWithContent.get(key));
    }

    private String getKeyOfQueryResultValue(final StorageOperation.StorageOperationResult applyRelationalDb) {
        ArrayList<StorageOperation.QueryResult> rawList =
                (ArrayList<StorageOperation.QueryResult>) applyRelationalDb.getRaw();
        String[] queryParts = rawList.get(0).getQuery().split(DelimiterConstant.SPACE);
        return queryParts[1];
    }

    private void verifyIfContentNotEmpty(final ArrayList<LinkedCaseInsensitiveMap<String>> content) {
        if (content.size() < 1) {
            throw new DefaultFrameworkException(VAR_QUERY_RESULT_ERROR);
        }
    }

    private interface AbstractResourcePredicate extends Predicate<Var> {
    }

    private interface AbstractResourceGetter extends BiFunction<Var, CommandResult, String> {
    }
}
