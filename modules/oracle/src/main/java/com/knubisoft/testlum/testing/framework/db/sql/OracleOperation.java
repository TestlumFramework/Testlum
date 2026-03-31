package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.condition.OnOracleEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Oracle;
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
