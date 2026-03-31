package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.condition.OnMysqlEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Conditional({OnMysqlEnabledCondition.class})
@Component("mySqlOperation")
public class MySqlOperation extends AbstractSqlOperation {

    public MySqlOperation(@Autowired(required = false) @Qualifier("mySqlDataSource")
                          final Map<AliasEnv, DataSource> mySqlDataSource) {
        super(mySqlDataSource, Mysql.class);
    }

    @Override
    protected AbstractSqlExecutor createExecutor(final DataSource dataSource) {
        return new MySqlExecutor(dataSource);
    }
}
