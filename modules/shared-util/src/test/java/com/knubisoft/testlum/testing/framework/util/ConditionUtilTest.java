package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link ConditionUtil} verifying SpEL condition evaluation,
 * context integration, and error handling.
 */
class ConditionUtilTest {

    private ConditionUtil conditionUtil;
    private ScenarioContext context;
    private CommandResult result;

    @BeforeEach
    void setUp() {
        final ResultUtil resultUtil = mock(ResultUtil.class);
        final LogUtil logUtil = mock(LogUtil.class);
        doNothing().when(logUtil).logCondition(anyString(), anyBoolean());
        doNothing().when(resultUtil).addCommandOnConditionMetaData(anyString(), anyBoolean(), any());
        conditionUtil = new ConditionUtil(resultUtil, logUtil);
        context = new ScenarioContext(new HashMap<>());
        result = new CommandResult();
    }

    @Nested
    class IsTrue {
        @Test
        void blankConditionReturnsTrue() {
            assertTrue(conditionUtil.isTrue("", context, result));
            assertTrue(conditionUtil.isTrue(null, context, result));
            assertTrue(conditionUtil.isTrue("   ", context, result));
        }

        @Test
        void trueExpressionReturnsTrue() {
            context.setCondition("myFlag", true);
            assertTrue(conditionUtil.isTrue("myFlag", context, result));
        }

        @Test
        void falseExpressionReturnsFalse() {
            context.setCondition("myFlag", false);
            assertFalse(conditionUtil.isTrue("myFlag", context, result));
        }

        @Test
        void invalidConditionThrows() {
            assertThrows(DefaultFrameworkException.class,
                    () -> conditionUtil.isTrue("invalidExpr", context, result));
        }
    }

    @Nested
    class ProcessCondition {
        @Test
        void processConditionSetsContextVariable() {
            final ResultUtil resultUtil = mock(ResultUtil.class);
            final LogUtil logUtil = mock(LogUtil.class);
            doNothing().when(logUtil).logConditionInfo(anyString(), anyString(), anyBoolean());
            doNothing().when(resultUtil).addConditionMetaData(anyString(), anyString(), anyBoolean(), any());

            final ConditionUtil util = new ConditionUtil(resultUtil, logUtil);
            util.processCondition("myCondition", "true", context, result);
            // After processing, the condition should be set in context
            assertTrue(context.getCondition("myCondition").contains("true"));
        }

        @Test
        void invalidExpressionThrows() {
            assertThrows(DefaultFrameworkException.class,
                    () -> conditionUtil.processCondition("cond", "!!!invalid!!!", context, result));
        }

        @Test
        void falseExpressionSetsContextToFalse() {
            final ResultUtil resultUtil = mock(ResultUtil.class);
            final LogUtil logUtil = mock(LogUtil.class);
            doNothing().when(logUtil).logConditionInfo(anyString(), anyString(), anyBoolean());
            doNothing().when(resultUtil).addConditionMetaData(anyString(), anyString(), anyBoolean(), any());

            final ConditionUtil util = new ConditionUtil(resultUtil, logUtil);
            util.processCondition("myCondition", "false", context, result);
            assertTrue(context.getCondition("myCondition").contains("false"));
        }
    }

    @Nested
    class ComplexExpressions {
        @Test
        void compoundBooleanExpression() {
            context.setCondition("a", true);
            context.setCondition("b", false);
            assertFalse(conditionUtil.isTrue("a && b", context, result));
        }

        @Test
        void stringLiteralExpression() {
            final ResultUtil resultUtil = mock(ResultUtil.class);
            final LogUtil logUtil = mock(LogUtil.class);
            doNothing().when(logUtil).logConditionInfo(anyString(), anyString(), anyBoolean());
            doNothing().when(resultUtil).addConditionMetaData(anyString(), anyString(), anyBoolean(), any());

            final ConditionUtil util = new ConditionUtil(resultUtil, logUtil);
            util.processCondition("strCond", "'hello' == 'hello'", context, result);
            assertTrue(context.getCondition("strCond").contains("true"));
        }

        @Test
        void numericExpression() {
            final ResultUtil resultUtil = mock(ResultUtil.class);
            final LogUtil logUtil = mock(LogUtil.class);
            doNothing().when(logUtil).logConditionInfo(anyString(), anyString(), anyBoolean());
            doNothing().when(resultUtil).addConditionMetaData(anyString(), anyString(), anyBoolean(), any());

            final ConditionUtil util = new ConditionUtil(resultUtil, logUtil);
            util.processCondition("numCond", "5 > 3", context, result);
            assertTrue(context.getCondition("numCond").contains("true"));
        }
    }
}
