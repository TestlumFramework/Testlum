package com.testlum.testing.framework.db.sql;

import com.testlum.testing.framework.condition.OnOracleEnabledCondition;
import com.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.model.global_config.Oracle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Conditional({OnOracleEnabledCondition.class})
@Component("oracleOperation")
public class OracleOperation extends AbstractSqlOperation {

    public OracleOperation(@Autowired(required = false) @Qualifier("oracleDataSource")
                           final Map<AliasEnv, DataSource> oracleDataSource) {
        super(oracleDataSource, Oracle.class);
    }

    @Override
    protected AbstractSqlExecutor createExecutor(final DataSource dataSource) {
        return new OracleExecutor(dataSource);
    }
}
