package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.db.sql.MySqlOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Mysql;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Mysql.class)
public class MySqlInterpreter extends AbstractInterpreter<Mysql> {

    @Autowired(required = false)
    private MySqlOperation mySqlOperation;

    public MySqlInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mysql mySql, final CommandResult result) {
        String expected = inject(getContentIfFile(mySql.getFile()));
        String actual = getActual(mySql, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpected(expected);

        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));
        result.setActual(StringPrettifier.asJsonResult(actual));

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Mysql mySql, final CommandResult result) {
        String alias = mySql.getAlias();
        List<String> queries = getSqlList(mySql);
        LogUtil.logAllQueries(queries, alias);
        ResultUtil.addDatabaseMetaData(alias, queries, result);
        StorageOperation.StorageOperationResult applyMySql = mySqlOperation.apply(new ListSource(queries),
                inject(alias));
        return toString(applyMySql.getRaw());
    }

    private List<String> getSqlList(final Mysql mySql) {
        return mySql.getQuery().stream()
                .map(this::inject)
                .collect(Collectors.toList());
    }
}
