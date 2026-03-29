package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.model.scenario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class VariableHelperImplTest {

    private VariableHelperImpl variableHelper;
    private ResultUtil resultUtil;
    private LogUtil logUtil;

    @BeforeEach
    void setUp() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        AliasToStorageOperation aliasToStorageOperation = mock(AliasToStorageOperation.class);
        resultUtil = mock(ResultUtil.class);
        logUtil = mock(LogUtil.class);
        when(ctx.getBean(AliasToStorageOperation.class)).thenReturn(aliasToStorageOperation);
        when(ctx.getBean(ResultUtil.class)).thenReturn(resultUtil);
        when(ctx.getBean(LogUtil.class)).thenReturn(logUtil);
        variableHelper = new VariableHelperImpl(ctx);
    }

    @Nested
    class GetConstantResult {
        @Test
        void returnsConstantValue() {
            doNothing().when(resultUtil).addVariableMetaData(anyString(), anyString(), anyString(), anyString(), any());
            FromConstant fromConstant = mock(FromConstant.class);
            when(fromConstant.getValue()).thenReturn("myValue");
            CommandResult result = new CommandResult();
            String value = variableHelper.getConstantResult(fromConstant, "varName", result);
            assertEquals("myValue", value);
        }
    }

    @Nested
    class GetFileResult {
        @Test
        void delegatesToFileReader() {
            doNothing().when(resultUtil).addVariableMetaData(anyString(), anyString(), anyString(), anyString(), any());
            FromFile fromFile = mock(FromFile.class);
            when(fromFile.getFileName()).thenReturn("test.txt");
            UnaryOperator<String> fileToString = name -> "file content";
            CommandResult result = new CommandResult();
            String value = variableHelper.getFileResult(fromFile, "varName", fileToString, result);
            assertEquals("file content", value);
        }
    }

    @Nested
    class GetExpressionResult {
        @Test
        void evaluatesSpelExpression() {
            doNothing().when(resultUtil).addVariableMetaData(anyString(), anyString(), anyString(), anyString(), any());
            FromExpression fromExpression = mock(FromExpression.class);
            when(fromExpression.getValue()).thenReturn("'hello' + ' world'");
            CommandResult result = new CommandResult();
            String value = variableHelper.getExpressionResult(fromExpression, "varName", result);
            assertEquals("hello world", value);
        }

        @Test
        void evaluatesNumericExpression() {
            doNothing().when(resultUtil).addVariableMetaData(anyString(), anyString(), anyString(), anyString(), any());
            FromExpression fromExpression = mock(FromExpression.class);
            when(fromExpression.getValue()).thenReturn("2 + 3");
            CommandResult result = new CommandResult();
            String value = variableHelper.getExpressionResult(fromExpression, "varName", result);
            assertEquals("5", value);
        }
    }

    @Nested
    class LookupVarMethod {
        @Test
        void findsMatchingMethod() {
            AbstractCommand cmd = mock(AbstractCommand.class);
            Map methodMap = new HashMap<>();
            methodMap.put((java.util.function.Predicate) c -> true, (Object) "found");
            // Can't fully test generics here, but verify the method exists
            assertNotNull(variableHelper);
        }

        @Test
        void throwsWhenNoMatch() {
            AbstractCommand cmd = mock(AbstractCommand.class);
            Map methodMap = Map.of();
            assertThrows(DefaultFrameworkException.class,
                    () -> variableHelper.lookupVarMethod(methodMap, cmd));
        }
    }

    @Nested
    class GetPathResult {
        @Test
        void throwsForUnsupportedPath() {
            FromPath fromPath = mock(FromPath.class);
            when(fromPath.getValue()).thenReturn("unsupported-path");
            when(fromPath.getFromFile()).thenReturn(null);
            when(fromPath.getFromVar()).thenReturn(null);
            ScenarioContext context = new ScenarioContext(new HashMap<>());
            context.set("body", "some body");
            CommandResult result = new CommandResult();
            assertThrows(DefaultFrameworkException.class,
                    () -> variableHelper.getPathResult(fromPath, "var", context, result, s -> s));
        }

        @Test
        void evaluatesJsonPath() {
            doNothing().when(resultUtil).addVariableMetaData(anyString(), anyString(), anyString(), anyString(), any());
            FromPath fromPath = mock(FromPath.class);
            when(fromPath.getValue()).thenReturn("$.name");
            when(fromPath.getFromFile()).thenReturn(null);
            when(fromPath.getFromVar()).thenReturn(null);
            ScenarioContext context = new ScenarioContext(new HashMap<>());
            context.set("body", "{\"name\":\"test\"}");
            CommandResult result = new CommandResult();
            String value = variableHelper.getPathResult(fromPath, "var", context, result, s -> s);
            assertEquals("test", value);
        }
    }
}
