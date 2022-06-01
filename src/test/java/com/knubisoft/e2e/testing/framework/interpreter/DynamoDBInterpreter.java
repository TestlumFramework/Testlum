package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.model.scenario.Dynamo;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.dynamodb.DynamoDBOperation;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Dynamo.class)
public class DynamoDBInterpreter extends AbstractInterpreter<Dynamo> {

    @Autowired(required = false)
    private DynamoDBOperation dynamoDBOperation;

    public DynamoDBInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Dynamo ddb, final CommandResult result) {
        String actual = getActual(ddb, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpectedFile(ddb.getFile());
        result.setActual(actual);
        result.setExpected(comparator.getExpected());

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Dynamo ddb, final CommandResult result) {
        List<String> queries = getDynamoQueryList(ddb);
        LogUtil.logAllQueries(queries, ddb.getAlias());
        result.put("sqls", queries);
        StorageOperation.StorageOperationResult apply = dynamoDBOperation
                .apply(new ListSource(queries), ddb.getAlias());
        return JacksonMapperUtil.writeAsStringForDynamoDbOnly(apply.getRaw());
    }

    private List<String> getDynamoQueryList(final Dynamo ddb) {
        return ddb.getQuery().stream()
                .map(this::inject)
                .collect(Collectors.toList());
    }
}
