package com.knubisoft.e2e.testing.framework.interpreter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.model.scenario.PostgresResult;
import com.knubisoft.e2e.testing.model.scenario.Var;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.e2e.testing.framework.exception.IncorrectQueryForVarException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.scenario.ScenarioContext;
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

import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.FAILED_VARIABLE_WITH_PATH_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.TEMPLATE_LOG;
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
        result.put("key", o.getName());
        result.put("value", value);
        context.set(o.getName(), value);
        log.info(TEMPLATE_LOG, value);
    }

    //CHECKSTYLE:OFF
    private String getValueForContext(final Var o, final ScenarioContext context, final CommandResult result) {
        if (o.getJpath() != null) {
            result.put("type", "json path");
            result.put("expression", o.getJpath());
            DocumentContext contextBody = JsonPath.parse(context.getBody());
            return contextBody.read(o.getJpath()).toString();
        } else if (o.getXpath() != null) {
            result.put("type", "xml path");
            result.put("expression", o.getXpath());
            WebDriver webDriver = dependencies.getWebDriver();
            return webDriver.findElement(By.xpath(o.getXpath())).getText();
        } else if (o.getPostgresResult() != null) {
            result.put("type", "postgres query");
            result.put("expression", o.getPostgresResult().getQuery());
            return getActualPostgresResult(o.getPostgresResult(), result);
        } else if (o.getExpression() != null) {
            result.put("type", "expression");
            result.put("expression", o.getExpression());
            return getExpressionResult(o.getExpression());
        } else {
            result.put("type", "constant");
            result.put("expression", "no expression");
            return inject(o.getValue());
        }
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
