package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
import com.knubisoft.testlum.testing.model.global_config.MysqlIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OnMysqlEnabledConditionTest {

    private final OnMysqlEnabledCondition condition = new OnMysqlEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsMysqlListWhenIntegrationsPresent() {
            Integrations integrations = mock(Integrations.class);
            MysqlIntegration mysqlIntegration = mock(MysqlIntegration.class);
            Mysql mysql = mock(Mysql.class);

            when(integrations.getMysqlIntegration()).thenReturn(mysqlIntegration);
            when(mysqlIntegration.getMysql()).thenReturn(List.of(mysql));

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertSame(mysql, result.get().get(0));
        }

        @Test
        void returnsEmptyWhenIntegrationsOptionalIsEmpty() {
            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenMysqlIntegrationIsNull() {
            Integrations integrations = mock(Integrations.class);
            when(integrations.getMysqlIntegration()).thenReturn(null);

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenMysqlListIsNull() {
            Integrations integrations = mock(Integrations.class);
            MysqlIntegration mysqlIntegration = mock(MysqlIntegration.class);

            when(integrations.getMysqlIntegration()).thenReturn(mysqlIntegration);
            when(mysqlIntegration.getMysql()).thenReturn(null);

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsMultipleMysqlIntegrations() {
            Integrations integrations = mock(Integrations.class);
            MysqlIntegration mysqlIntegration = mock(MysqlIntegration.class);
            Mysql m1 = mock(Mysql.class);
            Mysql m2 = mock(Mysql.class);
            Mysql m3 = mock(Mysql.class);

            when(integrations.getMysqlIntegration()).thenReturn(mysqlIntegration);
            when(mysqlIntegration.getMysql()).thenReturn(List.of(m1, m2, m3));

            Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(3, result.get().size());
        }
    }
}
