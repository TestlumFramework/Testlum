package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabase;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabaseIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnSqlDatabaseEnableCondition extends AbstractCondition<SqlDatabase> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getSqlDatabaseIntegration).map(SqlDatabaseIntegration::getSqlDatabase);
    }
}
