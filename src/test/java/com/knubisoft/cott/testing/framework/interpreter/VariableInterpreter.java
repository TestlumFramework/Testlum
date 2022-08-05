package com.knubisoft.cott.testing.framework.interpreter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.PostgresResult;
import com.knubisoft.cott.testing.model.scenario.Var;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.ListSource;
import com.knubisoft.cott.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.cott.testing.framework.exception.IncorrectQueryForVarException;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_VARIABLE_WITH_PATH_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CONSTANT;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.JSON_PATH;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NO_EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.POSTGRES_QUERY;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.XPATH;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Var.class)
public class VariableInterpreter extends AbstractInterpreter<Var> {
    private static final String COUNT_ROWS_QUERY = "SELECT count(*) from (%s)result";
    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    public VariableInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Var o, final CommandResult result) {
        try {
            setContextVariable(o, result);
        } catch (Exception e) {
            log.info(FAILED_VARIABLE_WITH_PATH_LOG, o.getName(), o.getComment());
            throw e;
        }
    }

    private void setContextVariable(final Var o, final CommandResult result) {
        ScenarioContext context = dependencies.getScenarioContext();
        String value = getValueForContext(o, context, result);
        context.set(o.getName(), value);
        LogUtil.logVarInfo(o.getName(), value);
    }

    //CHECKSTYLE:OFF
    private String getValueForContext(final Var o, final ScenarioContext context, final CommandResult result) {
        String value;
        if (o.getJpath() != null) {
            DocumentContext contextBody = JsonPath.parse(context.getBody());
            value = contextBody.read(o.getJpath()).toString();
            ResultUtil.addVariableMetaData(JSON_PATH, o.getName(), o.getJpath(), value, result);
        } else if (o.getXpath() != null) {
            WebDriver webDriver = dependencies.getWebDriver();
            value = webDriver.findElement(By.xpath(o.getXpath())).getText();
            ResultUtil.addVariableMetaData(XPATH, o.getName(), o.getXpath(), value, result);
        } else if (o.getPostgresResult() != null) {
            value = getActualPostgresResult(o.getPostgresResult(), result);
            ResultUtil.addVariableMetaData(POSTGRES_QUERY,
                    o.getName(), o.getPostgresResult().getQuery(), value, result);
        } else if (o.getExpression() != null) {
            value = getExpressionResult(o.getExpression());
            ResultUtil.addVariableMetaData(EXPRESSION, o.getName(), o.getExpression(), value, result);
        } else {
            value = inject(o.getValue());
            ResultUtil.addVariableMetaData(CONSTANT, o.getName(), NO_EXPRESSION, value, result);
        }
        return value;
    }

    private String getExpressionResult(final String expression) {
        ExpressionParser parser = new SpelExpressionParser();
        String injectedExpression = inject(expression);
        Expression exp = parser.parseExpression(injectedExpression);
        return Objects.requireNonNull(exp.getValue()).toString();
    }

    private String getActualPostgresResult(final PostgresResult postgresResult, final CommandResult result) {
        List<String> query = new ArrayList<>(Arrays.asList(postgresResult.getQuery()));
        result.put("query", query);
        verifyQueryResult(postgresResult.getQuery(), postgresResult.getDatabaseName());
        StorageOperation.StorageOperationResult applyPostgres =
                postgresSqlOperation.apply(new ListSource(query), postgresResult.getDatabaseName());
        String[] queryParts = getQuery(applyPostgres).split(SPACE);
        String keyOfQueryResult = queryParts[1];
        return getContent(applyPostgres, keyOfQueryResult);
    }

    private String getQuery(final StorageOperation.StorageOperationResult applyPostgres) {
        ArrayList<StorageOperation.QueryResult> list =
                (ArrayList<StorageOperation.QueryResult>) applyPostgres.getRaw();
        return list.get(0).getQuery();
    }

    private void verifyQueryResult(final String query, final String databaseName) {
        String newQuery = format(COUNT_ROWS_QUERY, query);
        int count = applyQueryAndGetResult(newQuery, databaseName);
        if (count > 1) {
            throw new IncorrectQueryForVarException(query, count);
        }
    }

    private int applyQueryAndGetResult(final String newQuery, final String databaseName) {
        List<String> newQueryInList = new ArrayList<>(Arrays.asList(newQuery));
        StorageOperation.StorageOperationResult applyPostgres =
                postgresSqlOperation.apply(new ListSource(newQueryInList), databaseName);
        return Integer.parseInt(getContent(applyPostgres, "count"));
    }

    private String getContent(final StorageOperation.StorageOperationResult applyPostgres,
                              final String key) {
        ArrayList<StorageOperation.QueryResult> list =
                (ArrayList<StorageOperation.QueryResult>) applyPostgres.getRaw();
        ArrayList<LinkedHashMap<String, String>> content =
                (ArrayList<LinkedHashMap<String, String>>) list.get(0).getContent();
        return content.get(0).get(key);
    }
    //CHECKSTYLE:ON
}
