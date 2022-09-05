package com.knubisoft.cott.testing.framework.interpreter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.ListSource;
import com.knubisoft.cott.testing.framework.db.sql.MySqlOperation;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.exception.IncorrectQueryForVarException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.RelationalDbResult;
import com.knubisoft.cott.testing.model.scenario.Var;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_VARIABLE_WITH_PATH_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CONSTANT;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.JSON_PATH;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NO_EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.RELATIONAL_DB_QUERY;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Var.class)
public class VariableInterpreter extends AbstractInterpreter<Var> {
    private static final String COUNT_ROWS_QUERY = "SELECT count(*) from (%s)result";
    private StorageOperation sqlOperation;
    @Autowired(required = false)
    private NameToAdapterAlias nameToAdapterAlias;

    public VariableInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
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

    //CHECKSTYLE:ON

    private void setContextVariable(final Var var, final CommandResult result) {
        ScenarioContext context = dependencies.getScenarioContext();
        String value = getValueForContext(var, context, result);
        context.set(var.getName(), value);
        LogUtil.logVarInfo(var.getName(), value);
    }

    //CHECKSTYLE:OFF
    private String getValueForContext(final Var var, final ScenarioContext context, final CommandResult result) {
        String value = null;
        if (var.getRelationalDbResult() != null) {
            value = getDbResult(var, result);
        } else if (var.getResultFrom() != null) {
            value = getResultFrom(var, context, result);
        }
        return value;
    }

    private String getResultFrom(final Var var, final ScenarioContext context, final CommandResult result) {
        switch (var.getResultFrom().getType()) {
            case EXPRESSION:
                return getExpressionResult(var, result);
            case JPATH:
                return getJpathResult(var, context, result);
            case VALUE:
                return getValueResult(var, result);
            default:
                throw new DefaultFrameworkException("Type of 'Var' tag is not supported");
        }
    }

    private String getValueResult(final Var var, final CommandResult result) {
        String value = inject(var.getResultFrom().getInputValue());
        ResultUtil.addVariableMetaData(CONSTANT, var.getName(), NO_EXPRESSION, value, result);
        return value;
    }

    private String getExpressionResult(final Var var, final CommandResult result) {
        String value = parseExpressions(var.getResultFrom().getInputValue());
        ResultUtil.addVariableMetaData(EXPRESSION, var.getName(),
                var.getResultFrom().getInputValue(), value, result);
        return value;
    }

    private String parseExpressions(final String expression) {
        ExpressionParser parser = new SpelExpressionParser();
        String injectedExpression = inject(expression);
        Expression exp = parser.parseExpression(injectedExpression);
        return Objects.toString(exp.getValue(dependencies.getScenarioContext()));
    }

    private String getJpathResult(final Var var, final ScenarioContext context, final CommandResult result) {
        DocumentContext contextBody = JsonPath.parse(context.getBody());
        String value = contextBody.read(var.getResultFrom().getInputValue()).toString();
        ResultUtil.addVariableMetaData(JSON_PATH, var.getName(),
                var.getResultFrom().getInputValue(), value, result);
        return value;
    }

    private String getDbResult(final Var var, final CommandResult result) {
        sqlOperation = nameToAdapterAlias.getStorageOperation(var.getRelationalDbResult().getDbType()
                + UNDERSCORE + var.getRelationalDbResult().getAlias());
        String amountOfLinks = getActualRelationalDbResult(var.getRelationalDbResult(), result);
        ResultUtil.addVariableMetaData(RELATIONAL_DB_QUERY,
                var.getName(), var.getRelationalDbResult().getQuery(), amountOfLinks, result);
        return amountOfLinks;
    }

    private String getActualRelationalDbResult(final RelationalDbResult relationalDbResult,
                                               final CommandResult result) {
        String alias = relationalDbResult.getAlias();
        List<String> query = new ArrayList<>(Collections.singletonList(relationalDbResult.getQuery()));
        result.put("query", query);
        LogUtil.logAllQueries(query, alias);
        verifyQueryResult(relationalDbResult.getQuery(), relationalDbResult.getAlias());
        ResultUtil.addDatabaseMetaData(alias, query, result);
        StorageOperation.StorageOperationResult applyRelationalDb =
                sqlOperation.apply(new ListSource(query), inject(alias));
        String[] queryParts = getQuery(applyRelationalDb).split(SPACE);
        String keyOfQueryResult = queryParts[1];
        return getContent(applyRelationalDb, keyOfQueryResult);
    }

    private String getQuery(final StorageOperation.StorageOperationResult applyRelationalDb) {
        ArrayList<StorageOperation.QueryResult> list =
                (ArrayList<StorageOperation.QueryResult>) applyRelationalDb.getRaw();
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
        StorageOperation.StorageOperationResult applyRelationalDb =
                sqlOperation.apply(new ListSource(newQueryInList), databaseName);
        String count;
        if (sqlOperation instanceof MySqlOperation) {
            count = "count(*)";
        } else {
            count = "count";
        }
        return Integer.parseInt(getContent(applyRelationalDb, count));
    }

    private String getContent(final StorageOperation.StorageOperationResult applyRelationalDb,
                              final String key) {
        ArrayList<StorageOperation.QueryResult> list =
                (ArrayList<StorageOperation.QueryResult>) applyRelationalDb.getRaw();
        ArrayList<LinkedCaseInsensitiveMap<String>> content =
                (ArrayList<LinkedCaseInsensitiveMap<String>>) list.get(0).getContent();
        Map<String, String> mapWithContent = content.get(0);
        return String.valueOf(mapWithContent.get(key));
    }

}
