package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.sql.MySqlOperation;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.Mysql;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.ALIAS_LOG;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
@InterpreterForClass(Mysql.class)
public class MySqlInterpreter extends AbstractInterpreter<Mysql> {

    private static final String SQL_LOG_TEMPLATE = "%10s %-100s";

    @Autowired(required = false)
    private MySqlOperation mySqlOperation;

    public MySqlInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mysql mySql, final CommandResult result) {
        String actual = getActual(mySql, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpectedFile(mySql.getFile());

        result.setExpected(comparator.getExpected());
        result.setActual(actual);

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Mysql mySql, final CommandResult result) {
        log.info(ALIAS_LOG, mySql.getAlias());
        List<String> sqls = getSqlList(mySql);
        result.put("sqls", sqls);
        StorageOperation.StorageOperationResult applyMySql = mySqlOperation.apply(new ListSource(sqls),
                inject(mySql.getAlias()));
        return toString(applyMySql.getRaw());
    }

    private List<String> getSqlList(final Mysql mySql) {
        List<String> queriesMySql = mySql.getQuery().stream()
                .map(this::inject)
                .collect(Collectors.toList());

        queriesMySql.forEach(it -> log.info(format(SQL_LOG_TEMPLATE, EMPTY, it)));
        return queriesMySql;
    }
}
