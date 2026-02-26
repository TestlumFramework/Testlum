package com.knubisoft.testlum.testing.framework.configuration.condition;

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
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getSqlDatabaseIntegration())
                .map(SqlDatabaseIntegration::getSqlDatabase)
                .orElse(null);
    }
}
