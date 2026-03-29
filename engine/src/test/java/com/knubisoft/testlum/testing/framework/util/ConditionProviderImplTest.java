package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConditionProviderImplTest {

    @Mock
    private ConditionUtil conditionUtil;

    @InjectMocks
    private ConditionProviderImpl conditionProvider;

    @Mock
    private ScenarioContext context;

    @Mock
    private CommandResult result;

    @Nested
    class IsTrue {
        @Test
        void delegatesToConditionUtil() {
            when(conditionUtil.isTrue("cond", context, result)).thenReturn(true);
            assertTrue(conditionProvider.isTrue("cond", context, result));
            verify(conditionUtil).isTrue("cond", context, result);
        }

        @Test
        void returnsFalseWhenConditionUtilReturnsFalse() {
            when(conditionUtil.isTrue("cond", context, result)).thenReturn(false);
            assertFalse(conditionProvider.isTrue("cond", context, result));
        }
    }

    @Nested
    class ProcessCondition {
        @Test
        void delegatesToConditionUtil() {
            conditionProvider.processCondition("name", "expr", context, result);
            verify(conditionUtil).processCondition("name", "expr", context, result);
        }
    }
}
