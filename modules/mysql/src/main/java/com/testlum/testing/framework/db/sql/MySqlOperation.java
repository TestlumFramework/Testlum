package com.testlum.testing.framework.db.sql;

import com.testlum.testing.framework.condition.OnMysqlEnabledCondition;
import com.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.model.global_config.Mysql;
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
