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

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.DOLLAR_SIGN;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SLASH_SEPARATOR;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.VAR_QUERY_RESULT_ERROR;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_VARIABLE_WITH_PATH_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CONSTANT;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.COOKIES;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.HTML_DOM;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.FILE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.JSON_PATH;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NO_EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.RELATIONAL_DB_QUERY;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.XML_PATH;
import static java.util.Objects.nonNull;

@Slf4j
@InterpreterForClass(Var.class)
public class VariableInterpreter extends AbstractInterpreter<Var> {
    @Autowired(required = false)
    private NameToAdapterAlias nameToAdapterAlias;
    private final Map<VarFromPredicate, VarFromMethod> varToMethodMap;

    public VariableInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        Map<VarFromPredicate, VarFromMethod> varMap = new HashMap<>();
        varMap.put(var -> nonNull(var.getFromSQL()), this::getSQLResult);
        varMap.put(var -> nonNull(var.getFromFile()), this::getFileResult);
        varMap.put(var -> nonNull(var.getFromExpression()), this::getExpressionResult);
        varMap.put(var -> nonNull(var.getFromConstant()), this::getConstantResult);
        varMap.put(var -> nonNull(var.getFromPath()), this::getPathResult);
        varMap.put(var -> nonNull(var.getFromCookie()), this::getWebCookiesResult);
        varMap.put(var -> nonNull(var.getFromDom()), this::getDomResult);
        varToMethodMap = Collections.unmodifiableMap(varMap);
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
        return varToMethodMap.keySet().stream()
                .filter(key -> key.test(var))
                .findFirst()
                .map(varToMethodMap::get)
                .orElseThrow(() -> new DefaultFrameworkException("Type of 'Var' tag is not supported"))
                .apply(var, result);
    }

    private String getDomResult(final Var var, final CommandResult result) {
        String xpath = var.getFromDom().getXpath();
        String valueResult = dependencies.getWebDriver().findElement(By.xpath(xpath)).getText();
        ResultUtil.addVariableMetaData(HTML_DOM, var.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getFileResult(final Var var, final CommandResult result) {
        String fileName = var.getFromFile().getFileName();
        String valueResult = FileSearcher.searchFileToString(fileName, dependencies.getFile());
        ResultUtil.addVariableMetaData(FILE, var.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getConstantResult(final Var var, final CommandResult result) {
        String value = var.getFromConstant().getValue();
        String valueResult = inject(value);
        ResultUtil.addVariableMetaData(CONSTANT, var.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getWebCookiesResult(final Var var, final CommandResult result) {
        Set<Cookie> cookies = dependencies.getWebDriver().manage().getCookies();
        String valueResult = cookies.stream()
                .map(cookie -> String.join(DelimiterConstant.EQUALS_MARK, cookie.getName(), cookie.getValue()))
                .collect(Collectors.joining(DelimiterConstant.SEMICOLON));
        ResultUtil.addVariableMetaData(COOKIES, var.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getExpressionResult(final Var var, final CommandResult result) {
        String expression = var.getFromExpression().getValue();
        String injectedExpression = inject(expression);
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(injectedExpression);
        String valueResult = Objects.requireNonNull(exp.getValue()).toString();
        ResultUtil.addVariableMetaData(EXPRESSION, var.getName(), expression, valueResult, result);
        return valueResult;
    }

    @SneakyThrows
    private String getPathResult(final Var var, final CommandResult result) {
        String path = var.getFromPath().getValue();
        if (path.startsWith(DOLLAR_SIGN)) {
            return evaluateJPath(path, var.getName(), result);
        }
        if (path.startsWith(SLASH_SEPARATOR)) {
            return evaluateXPath(path, var.getName(), result);
        }
        throw new DefaultFrameworkException("Path <%s> is not supported", path);
    }



    private String evaluateXPath(final String path, final String varName, final CommandResult result) throws Exception {
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

    private String getSQLResult(final Var var, final CommandResult result) {
        FromSQL fromSQL = var.getFromSQL();
        String metadataKey = fromSQL.getDbType().name() + DelimiterConstant.UNDERSCORE + fromSQL.getAlias();
        StorageOperation storageOperation = nameToAdapterAlias.getByNameOrThrow(metadataKey).getStorageOperation();
        String valueResult = getActualQueryResult(fromSQL, storageOperation);
        ResultUtil.addVariableMetaData(RELATIONAL_DB_QUERY, var.getName(), fromSQL.getQuery(), valueResult, result);
        return valueResult;
    }

    private String getActualQueryResult(final FromSQL fromSQL,
                                        final StorageOperation storageOperation) {
        String alias = fromSQL.getAlias();
        List<String> singleQuery = new ArrayList<>(Collections.singletonList(fromSQL.getQuery()));
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

    private interface VarFromPredicate extends Predicate<Var> {
    }

    private interface VarFromMethod extends BiFunction<Var, CommandResult, String> {
    }
}
