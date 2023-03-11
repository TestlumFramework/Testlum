package com.knubisoft.cott.testing.framework.util;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.ListSource;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import com.knubisoft.cott.testing.model.scenario.FromConstant;
import com.knubisoft.cott.testing.model.scenario.FromExpression;
import com.knubisoft.cott.testing.model.scenario.FromFile;
import com.knubisoft.cott.testing.model.scenario.FromPath;
import com.knubisoft.cott.testing.model.scenario.FromSQL;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.DOLLAR_SIGN;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SLASH_SEPARATOR;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.VAR_QUERY_RESULT_ERROR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CONSTANT;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.FILE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.JSON_PATH;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NO_EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.RELATIONAL_DB_QUERY;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.XML_PATH;

@Slf4j
@Component
@Scope("prototype")
public class VarService {
    @Autowired
    private NameToAdapterAlias nameToAdapterAlias;

    private ScenarioContext scenarioContext;

    public void setScenarioContext(final ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
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
        String value = fromConstant.getValue();
        String valueResult = inject(value);
        ResultUtil.addVariableMetaData(CONSTANT, varName, NO_EXPRESSION, valueResult, result);
        return valueResult;
    }


    public String getExpressionResult(final FromExpression fromExpression,
                                      final String varName,
                                      final CommandResult result) {
        String expression = fromExpression.getValue();
        String injectedExpression = inject(expression);
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(injectedExpression);
        String valueResult = Objects.requireNonNull(exp.getValue()).toString();
        ResultUtil.addVariableMetaData(EXPRESSION, varName, expression, valueResult, result);
        return valueResult;
    }

    @SneakyThrows
    public String getPathResult(final FromPath fromPath,
                                final String varName,
                                final CommandResult result) {
        String path = fromPath.getValue();
        if (path.startsWith(DOLLAR_SIGN)) {
            return evaluateJPath(path, varName, result);
        }
        if (path.startsWith(SLASH_SEPARATOR)) {
            return evaluateXPath(path, varName, result);
        }
        throw new DefaultFrameworkException("Path <%s> is not supported", path);
    }


    private String evaluateXPath(final String path,
                                 final String varName,
                                 final CommandResult result) throws Exception {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder
                .parse(new InputSource(new StringReader(scenarioContext.getBody())));
        XPath xPath = XPathFactory.newInstance().newXPath();
        String valueResult = xPath.evaluate(path, document);
        ResultUtil.addVariableMetaData(XML_PATH, varName, path, valueResult, result);
        return valueResult;
    }

    private String evaluateJPath(final String path,
                                 final String varName,
                                 final CommandResult result) {
        DocumentContext contextBody = JsonPath.parse(scenarioContext.getBody());
        String valueResult = contextBody.read(path).toString();
        ResultUtil.addVariableMetaData(JSON_PATH, varName, path, valueResult, result);
        return valueResult;
    }

    public String getSQLResult(final FromSQL fromSQL, final String varName, final CommandResult result) {
        String metadataKey = fromSQL.getDbType().name() + DelimiterConstant.UNDERSCORE + fromSQL.getAlias();
        StorageOperation storageOperation = nameToAdapterAlias.getByNameOrThrow(metadataKey).getStorageOperation();
        String valueResult = getActualQueryResult(fromSQL, storageOperation);
        ResultUtil.addVariableMetaData(RELATIONAL_DB_QUERY, varName, fromSQL.getQuery(), valueResult, result);
        return valueResult;
    }

    private String getActualQueryResult(final FromSQL fromSQL,
                                        final StorageOperation storageOperation) {
        String alias = fromSQL.getAlias();
        List<String> singleQuery = new ArrayList<>(Collections.singletonList(inject(fromSQL.getQuery())));
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

    private String inject(final String original) {
        return scenarioContext.inject(original);
    }

}
