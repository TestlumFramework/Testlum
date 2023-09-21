package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.db.util.LogUtil;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.db.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Mysql;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;

@Slf4j
@InterpreterForClass(Mysql.class)
public class MySqlInterpreter extends AbstractInterpreter<Mysql> {

    @Autowired(required = false)
    @Qualifier("mySqlOperation")
    private AbstractStorageOperation mySqlOperation;

    public MySqlInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mysql o, final CommandResult result) {
        Mysql mysql = injectCommand(o);
        String actual = getActual(mysql, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpected(getContentIfFile(mysql.getFile()));

        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));
        result.setActual(StringPrettifier.asJsonResult(actual));

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Mysql mysql, final CommandResult result) {
        String alias = mysql.getAlias();
        List<String> queries = mysql.getQuery();
        LogUtil.logAllQueries(queries, alias);
        ResultUtil.addDatabaseMetaData(alias, queries, result);
        StorageOperationResult applyMySql = mySqlOperation.apply(new ListSource(queries), alias);
        return toString(applyMySql.getRaw());
    }
}
