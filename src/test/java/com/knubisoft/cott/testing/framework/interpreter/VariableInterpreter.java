package com.knubisoft.cott.testing.framework.interpreter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.ListSource;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.RelationalDbResult;
import com.knubisoft.cott.testing.model.scenario.ResultFrom;
import com.knubisoft.cott.testing.model.scenario.Var;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.ArrayList;
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

@Slf4j
@InterpreterForClass(Var.class)
public class VariableInterpreter extends AbstractInterpreter<Var> {
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

    private void setContextVariable(final Var var, final CommandResult result) {
        String value = getValueForContext(var, result);
        dependencies.getScenarioContext().set(var.getName(), value);
        LogUtil.logVarInfo(var.getName(), value);
    }

    private String getValueForContext(final Var var, final CommandResult result) {
        if (Objects.nonNull(var.getRelationalDbResult())) {
            return getDbResult(var.getRelationalDbResult(), var.getName(), result);
        }
        return getResultFrom(var, result);
    }

    private String getResultFrom(final Var var, final CommandResult result) {
        ResultFrom resultFrom = var.getResultFrom();
        switch (resultFrom.getType()) {
            case EXPRESSION:
                return getExpressionResult(resultFrom.getValue(), var.getName(), result);
            case JPATH:
                return getJpathResult(resultFrom.getValue(), var.getName(), result);
            case VALUE:
                return getValueResult(resultFrom.getValue(), var.getName(), result);
            default:
                throw new DefaultFrameworkException("Type of 'Var' tag is not supported");
        }
    }

    private String getValueResult(final String value, final String varName, final CommandResult result) {
        String valueResult = inject(value);
        ResultUtil.addVariableMetaData(CONSTANT, varName, NO_EXPRESSION, value, result);
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

    private String getJpathResult(final String jpath, final String varName, final CommandResult result) {
        DocumentContext contextBody = JsonPath.parse(dependencies.getScenarioContext().getBody());
        String valueResult = contextBody.read(jpath).toString();
        ResultUtil.addVariableMetaData(JSON_PATH, varName, jpath, valueResult, result);
        return valueResult;
    }

    private String getDbResult(final RelationalDbResult dbResult, final String varName, final CommandResult result) {
        String metadataKey = dbResult.getDbType().name() + UNDERSCORE + dbResult.getAlias();
        StorageOperation storageOperation = nameToAdapterAlias.getByNameOrThrow(metadataKey).getStorageOperation();
        String valueResult = getActualRelationalDbResult(dbResult, storageOperation);
        ResultUtil.addVariableMetaData(RELATIONAL_DB_QUERY, varName, dbResult.getQuery(), valueResult, result);
        return valueResult;
    }

    private String getActualRelationalDbResult(final RelationalDbResult relationalDbResult,
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
        Map<String, String> mapWithContent = content.get(0);
        return String.valueOf(mapWithContent.get(key));
    }

    private String getKeyOfQueryResultValue(final StorageOperation.StorageOperationResult applyRelationalDb) {
        ArrayList<StorageOperation.QueryResult> rawList =
                (ArrayList<StorageOperation.QueryResult>) applyRelationalDb.getRaw();
        String[] queryParts = rawList.get(0).getQuery().split(SPACE);
        return queryParts[1];
    }

}
