package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.condition.OnClickhouseEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Conditional({OnClickhouseEnabledCondition.class})
@Component("clickhouseOperation")
public class ClickhouseOperation extends AbstractSqlOperation {

    public ClickhouseOperation(@Autowired @Qualifier("clickhouseDataSource")
                               final Map<AliasEnv, DataSource> clickhouseDataSource) {
        super(clickhouseDataSource, Clickhouse.class);
    }

    @Override
    protected AbstractSqlExecutor createExecutor(final DataSource dataSource) {
        return new ClickhouseExecutor(dataSource);
    }
}
