package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.model.scenario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.*;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VariableHelperImplTest {

    private VariableHelperImpl variableHelper;
    private ResultUtil resultUtil;
    private LogUtil logUtil;
    private AliasToStorageOperation aliasToStorageOperation;

    @BeforeEach
    void setUp() {
        aliasToStorageOperation = mock(AliasToStorageOperation.class);
        resultUtil = mock(ResultUtil.class);
        logUtil = mock(LogUtil.class);
        final ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.getBean(AliasToStorageOperation.class)).thenReturn(aliasToStorageOperation);
        when(ctx.getBean(ResultUtil.class)).thenReturn(resultUtil);
        when(ctx.getBean(LogUtil.class)).thenReturn(logUtil);
        variableHelper = new VariableHelperImpl(ctx);
    }

    @Nested
    class LookupVarMethodTests {

        @Test
        void findsMatchingMethodWhenPredicateMatches() {
            AbstractCommand cmd = mock(AbstractCommand.class);
            VariableHelper.VarMethod<AbstractCommand> expectedMethod = (c, r) -> "result";
            Map<VariableHelper.VarPredicate<AbstractCommand>, VariableHelper.VarMethod<AbstractCommand>> methodMap =
                    new LinkedHashMap<>();
            VariableHelper.VarPredicate<AbstractCommand> truePredicate = c -> true;
            methodMap.put(truePredicate, expectedMethod);

            VariableHelper.VarMethod<AbstractCommand> found = variableHelper.lookupVarMethod(methodMap, cmd);
            assertSame(expectedMethod, found);
        }

        @Test
        void findsFirstMatchingMethodWhenMultiplePredicatesMatch() {
            AbstractCommand cmd = mock(AbstractCommand.class);
            VariableHelper.VarMethod<AbstractCommand> firstMethod = (c, r) -> "first";
            VariableHelper.VarMethod<AbstractCommand> secondMethod = (c, r) -> "second";
            Map<VariableHelper.VarPredicate<AbstractCommand>, VariableHelper.VarMethod<AbstractCommand>> methodMap =
                    new LinkedHashMap<>();
            methodMap.put(c -> true, firstMethod);
            methodMap.put(c -> true, secondMethod);

            VariableHelper.VarMethod<AbstractCommand> found = variableHelper.lookupVarMethod(methodMap, cmd);
            assertSame(firstMethod, found);
        }

        @Test
        void throwsWhenNoPredicateMatches() {
            AbstractCommand cmd = mock(AbstractCommand.class);
            Map<VariableHelper.VarPredicate<AbstractCommand>, VariableHelper.VarMethod<AbstractCommand>> methodMap =
                    new LinkedHashMap<>();
            methodMap.put(c -> false, (c, r) -> "never");

            assertThrows(DefaultFrameworkException.class,
                    () -> variableHelper.lookupVarMethod(methodMap, cmd));
        }

        @Test
        void throwsWhenMapIsEmpty() {
            AbstractCommand cmd = mock(AbstractCommand.class);
            Map<VariableHelper.VarPredicate<AbstractCommand>, VariableHelper.VarMethod<AbstractCommand>> methodMap =
                    Map.of();

            assertThrows(DefaultFrameworkException.class,
                    () -> variableHelper.lookupVarMethod(methodMap, cmd));
        }
    }

    @Nested
    class GetRandomGenerateResultTests {

        @Test
        void generatesNumericString() {
            FromRandomGenerate randomGenerate = new FromRandomGenerate();
            randomGenerate.setNumeric(new RandomNumeric());
            randomGenerate.setLength(10);
            CommandResult result = new CommandResult();

            String value = variableHelper.getRandomGenerateResult(randomGenerate, "numVar", result);

            assertNotNull(value);
            assertEquals(10, value.length());
            assertTrue(value.matches("\\d+"), "Expected numeric string but got: " + value);
            verify(resultUtil).addVariableMetaData(
                    eq("Randomly generated string"), eq("numVar"), eq("No expression"), anyString(), eq(result));
        }

        @Test
        void generatesAlphabeticString() {
            FromRandomGenerate randomGenerate = new FromRandomGenerate();
            randomGenerate.setAlphabetic(new RandomAlphabetic());
            randomGenerate.setLength(8);
            CommandResult result = new CommandResult();

            String value = variableHelper.getRandomGenerateResult(randomGenerate, "alphaVar", result);

            assertNotNull(value);
            assertEquals(8, value.length());
            assertTrue(value.matches("[a-zA-Z]+"), "Expected alphabetic string but got: " + value);
            verify(resultUtil).addVariableMetaData(
                    eq("Randomly generated string"), eq("alphaVar"), eq("No expression"), anyString(), eq(result));
        }

        @Test
        void generatesAlphanumericString() {
            FromRandomGenerate randomGenerate = new FromRandomGenerate();
            randomGenerate.setAlphanumeric(new RandomAlphanumeric());
            randomGenerate.setLength(12);
            CommandResult result = new CommandResult();

            String value = variableHelper.getRandomGenerateResult(randomGenerate, "alphaNumVar", result);

            assertNotNull(value);
            assertEquals(12, value.length());
            assertTrue(value.matches("[a-zA-Z0-9]+"), "Expected alphanumeric string but got: " + value);
            verify(resultUtil).addVariableMetaData(
                    eq("Randomly generated string"), eq("alphaNumVar"), eq("No expression"), anyString(), eq(result));
        }

        @Test
        void generatesStringByRegexp() {
            FromRandomGenerate randomGenerate = new FromRandomGenerate();
            RandomRegexp regexp = new RandomRegexp();
            regexp.setPattern("[A-Z][0-9]");
            randomGenerate.setRandomRegexp(regexp);
            randomGenerate.setLength(2);
            CommandResult result = new CommandResult();

            String value = variableHelper.getRandomGenerateResult(randomGenerate, "regexpVar", result);

            assertNotNull(value);
            verify(resultUtil).addVariableMetaData(
                    eq("Randomly generated string"), eq("regexpVar"), eq("[A-Z][0-9]"), anyString(), eq(result));
        }

        @Test
        void throwsWhenNoGenerationMethodSet() {
            FromRandomGenerate randomGenerate = new FromRandomGenerate();
            randomGenerate.setLength(5);
            CommandResult result = new CommandResult();

            assertThrows(DefaultFrameworkException.class,
                    () -> variableHelper.getRandomGenerateResult(randomGenerate, "var", result));
        }
    }

    @Nested
    class GetFileResultTests {

        @Test
        void delegatesToFileReaderAndReturnsContent() {
            FromFile fromFile = mock(FromFile.class);
            when(fromFile.getFileName()).thenReturn("test.txt");
            UnaryOperator<String> fileToString = name -> "file content for " + name;
            CommandResult result = new CommandResult();

            String value = variableHelper.getFileResult(fromFile, "fileVar", fileToString, result);

            assertEquals("file content for test.txt", value);
            verify(resultUtil).addVariableMetaData(
                    eq("File"), eq("fileVar"), eq("No expression"), eq("file content for test.txt"), eq(result));
        }

        @Test
        void handlesEmptyFileContent() {
            FromFile fromFile = mock(FromFile.class);
            when(fromFile.getFileName()).thenReturn("empty.txt");
            UnaryOperator<String> fileToString = name -> "";
            CommandResult result = new CommandResult();

            String value = variableHelper.getFileResult(fromFile, "emptyFileVar", fileToString, result);

            assertEquals("", value);
        }
    }

    @Nested
    class GetConstantResultTests {

        @Test
        void returnsConstantValue() {
            FromConstant fromConstant = mock(FromConstant.class);
            when(fromConstant.getValue()).thenReturn("myValue");
            CommandResult result = new CommandResult();

            String value = variableHelper.getConstantResult(fromConstant, "constVar", result);

            assertEquals("myValue", value);
            verify(resultUtil).addVariableMetaData(
                    eq("Constant"), eq("constVar"), eq("No expression"), eq("myValue"), eq(result));
        }

        @Test
        void returnsNullConstantValue() {
            FromConstant fromConstant = mock(FromConstant.class);
            when(fromConstant.getValue()).thenReturn(null);
            CommandResult result = new CommandResult();

            String value = variableHelper.getConstantResult(fromConstant, "nullVar", result);

            assertNull(value);
        }
    }

    @Nested
    class GetExpressionResultTests {

        @Test
        void evaluatesStringConcatenation() {
            FromExpression fromExpression = mock(FromExpression.class);
            when(fromExpression.getValue()).thenReturn("'hello' + ' world'");
            CommandResult result = new CommandResult();

            String value = variableHelper.getExpressionResult(fromExpression, "exprVar", result);

            assertEquals("hello world", value);
            verify(resultUtil).addVariableMetaData(
                    eq("Expression"), eq("exprVar"), eq("'hello' + ' world'"), eq("hello world"), eq(result));
        }

        @Test
        void evaluatesNumericExpression() {
            FromExpression fromExpression = mock(FromExpression.class);
            when(fromExpression.getValue()).thenReturn("2 + 3");
            CommandResult result = new CommandResult();

            String value = variableHelper.getExpressionResult(fromExpression, "numExprVar", result);

            assertEquals("5", value);
        }

        @Test
        void evaluatesMethodCallExpression() {
            FromExpression fromExpression = mock(FromExpression.class);
            when(fromExpression.getValue()).thenReturn("'HELLO'.toLowerCase()");
            CommandResult result = new CommandResult();

            String value = variableHelper.getExpressionResult(fromExpression, "methodVar", result);

            assertEquals("hello", value);
        }
    }

    @Nested
    class GetPathResultTests {

        @Test
        void evaluatesJsonPathFromContextBody() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("$.name");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", "{\"name\":\"testValue\"}");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "jPathVar", context, result, s -> s);

            assertEquals("testValue", value);
            verify(resultUtil).addVariableMetaData(
                    eq("JSON path"), eq("jPathVar"), eq("$.name"), eq("testValue"), eq(result));
        }

        @Test
        void evaluatesJsonPathWithNestedObject() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("$.data.id");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", "{\"data\":{\"id\":42}}");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "nestedVar", context, result, s -> s);

            assertEquals("42", value);
        }

        @Test
        void evaluatesXPathFromContextBody() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("/root/name");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            String xmlBody = "<root><name>xmlValue</name></root>";
            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", xmlBody);
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "xPathVar", context, result, s -> s);

            assertEquals("xmlValue", value);
            verify(resultUtil).addVariableMetaData(
                    eq("Xml path"), eq("xPathVar"), eq("/root/name"), eq("xmlValue"), eq(result));
        }

        @Test
        void throwsForUnsupportedPath() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("unsupported-path");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", "some body");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> variableHelper.getPathResult(fromPath, "var", context, result, s -> s));
            assertTrue(ex.getMessage().contains("unsupported-path"));
        }
    }

    @Nested
    class ResolveBodySourceTests {

        @Test
        void usesContextBodyWhenFromFileAndFromVarAreNull() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("$.key");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", "{\"key\":\"fromContext\"}");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "ctxVar", context, result, s -> s);

            assertEquals("fromContext", value);
        }

        @Test
        void usesContextGetWhenFromFileStartsWithExpected() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("$.key");
            fromPath.setFromFile("expected_response.json");
            fromPath.setFromVar(null);

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("expected_response.json", "{\"key\":\"fromExpected\"}");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "expectedVar", context, result, s -> s);

            assertEquals("fromExpected", value);
        }

        @Test
        void usesFileToStringWhenFromFileDoesNotStartWithExpected() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("$.key");
            fromPath.setFromFile("data.json");
            fromPath.setFromVar(null);

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", "unused");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();
            UnaryOperator<String> fileToString = name -> "{\"key\":\"fromFile\"}";

            String value = variableHelper.getPathResult(fromPath, "fileVar", context, result, fileToString);

            assertEquals("fromFile", value);
        }

        @Test
        void usesContextGetWhenFromVarIsSet() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("$.key");
            fromPath.setFromFile(null);
            fromPath.setFromVar("myVar");

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("myVar", "{\"key\":\"fromVar\"}");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "varVar", context, result, s -> s);

            assertEquals("fromVar", value);
        }
    }

    @Nested
    class GetSQLResultTests {

        @SuppressWarnings("unchecked")
        @Test
        void executesSqlQueryAndReturnsResult() {
            FromSQL fromSQL = new FromSQL();
            fromSQL.setDbType(RelationalDB.POSTGRES);
            fromSQL.setAlias("myAlias");
            fromSQL.setQuery("SELECT name FROM users");

            AbstractStorageOperation storageOperation = mock(AbstractStorageOperation.class);
            when(aliasToStorageOperation.getByNameOrThrow("POSTGRES_myAlias")).thenReturn(storageOperation);

            LinkedCaseInsensitiveMap<String> row = new LinkedCaseInsensitiveMap<>();
            row.put("name", "John");
            List<LinkedCaseInsensitiveMap<String>> content = new ArrayList<>();
            content.add(row);

            AbstractStorageOperation.QueryResult<Object> queryResult =
                    new AbstractStorageOperation.QueryResult<>("SELECT name FROM users");
            queryResult.setContent(content);
            List<AbstractStorageOperation.QueryResult<?>> rawList = new ArrayList<>();
            rawList.add(queryResult);

            AbstractStorageOperation.StorageOperationResult operationResult =
                    new AbstractStorageOperation.StorageOperationResult(rawList);
            when(storageOperation.apply(any(ListSource.class), eq("myAlias"))).thenReturn(operationResult);

            CommandResult result = new CommandResult();

            String value = variableHelper.getSQLResult(fromSQL, "sqlVar", result);

            assertEquals("John", value);
            verify(resultUtil).addVariableMetaData(
                    eq("Relational DB query"), eq(fromSQL), eq("sqlVar"), eq("John"), eq(result));
        }

        @SuppressWarnings("unchecked")
        @Test
        void setsDefaultAliasWhenNull() {
            FromSQL fromSQL = new FromSQL();
            fromSQL.setDbType(RelationalDB.MYSQL);
            fromSQL.setAlias(null);
            fromSQL.setQuery("SELECT id FROM items");

            AbstractStorageOperation storageOperation = mock(AbstractStorageOperation.class);
            when(aliasToStorageOperation.getByNameOrThrow("MYSQL_DEFAULT")).thenReturn(storageOperation);

            LinkedCaseInsensitiveMap<String> row = new LinkedCaseInsensitiveMap<>();
            row.put("id", "99");
            List<LinkedCaseInsensitiveMap<String>> content = new ArrayList<>();
            content.add(row);

            AbstractStorageOperation.QueryResult<Object> queryResult =
                    new AbstractStorageOperation.QueryResult<>("SELECT id FROM items");
            queryResult.setContent(content);
            List<AbstractStorageOperation.QueryResult<?>> rawList = new ArrayList<>();
            rawList.add(queryResult);

            AbstractStorageOperation.StorageOperationResult operationResult =
                    new AbstractStorageOperation.StorageOperationResult(rawList);
            when(storageOperation.apply(any(ListSource.class), eq("DEFAULT"))).thenReturn(operationResult);

            CommandResult result = new CommandResult();

            String value = variableHelper.getSQLResult(fromSQL, "sqlDefaultVar", result);

            assertEquals("DEFAULT", fromSQL.getAlias());
            assertEquals("99", value);
        }

        @SuppressWarnings("unchecked")
        @Test
        void throwsWhenQueryResultContentIsEmpty() {
            FromSQL fromSQL = new FromSQL();
            fromSQL.setDbType(RelationalDB.ORACLE);
            fromSQL.setAlias("oraAlias");
            fromSQL.setQuery("SELECT name FROM empty_table");

            AbstractStorageOperation storageOperation = mock(AbstractStorageOperation.class);
            when(aliasToStorageOperation.getByNameOrThrow("ORACLE_oraAlias")).thenReturn(storageOperation);

            List<LinkedCaseInsensitiveMap<String>> emptyContent = new ArrayList<>();

            AbstractStorageOperation.QueryResult<Object> queryResult =
                    new AbstractStorageOperation.QueryResult<>("SELECT name FROM empty_table");
            queryResult.setContent(emptyContent);
            List<AbstractStorageOperation.QueryResult<?>> rawList = new ArrayList<>();
            rawList.add(queryResult);

            AbstractStorageOperation.StorageOperationResult operationResult =
                    new AbstractStorageOperation.StorageOperationResult(rawList);
            when(storageOperation.apply(any(ListSource.class), eq("oraAlias"))).thenReturn(operationResult);

            CommandResult result = new CommandResult();

            assertThrows(DefaultFrameworkException.class,
                    () -> variableHelper.getSQLResult(fromSQL, "emptyVar", result));
        }

        @SuppressWarnings("unchecked")
        @Test
        void throwsWhenQueryHasFewerThanTwoParts() {
            FromSQL fromSQL = new FromSQL();
            fromSQL.setDbType(RelationalDB.CLICKHOUSE);
            fromSQL.setAlias("chAlias");
            fromSQL.setQuery("SINGLEWORD");

            AbstractStorageOperation storageOperation = mock(AbstractStorageOperation.class);
            when(aliasToStorageOperation.getByNameOrThrow("CLICKHOUSE_chAlias")).thenReturn(storageOperation);

            LinkedCaseInsensitiveMap<String> row = new LinkedCaseInsensitiveMap<>();
            row.put("col", "val");
            List<LinkedCaseInsensitiveMap<String>> content = new ArrayList<>();
            content.add(row);

            AbstractStorageOperation.QueryResult<Object> queryResult =
                    new AbstractStorageOperation.QueryResult<>("SINGLEWORD");
            queryResult.setContent(content);
            List<AbstractStorageOperation.QueryResult<?>> rawList = new ArrayList<>();
            rawList.add(queryResult);

            AbstractStorageOperation.StorageOperationResult operationResult =
                    new AbstractStorageOperation.StorageOperationResult(rawList);
            when(storageOperation.apply(any(ListSource.class), eq("chAlias"))).thenReturn(operationResult);

            CommandResult result = new CommandResult();

            assertThrows(DefaultFrameworkException.class,
                    () -> variableHelper.getSQLResult(fromSQL, "badQueryVar", result));
        }
    }

    @Nested
    class CheckAliasTests {

        @SuppressWarnings("unchecked")
        @Test
        void setsDefaultWhenAliasIsNull() {
            FromSQL fromSQL = new FromSQL();
            fromSQL.setAlias(null);
            fromSQL.setDbType(RelationalDB.POSTGRES);
            fromSQL.setQuery("SELECT x FROM y");

            AbstractStorageOperation storageOperation = mock(AbstractStorageOperation.class);
            when(aliasToStorageOperation.getByNameOrThrow("POSTGRES_DEFAULT")).thenReturn(storageOperation);

            LinkedCaseInsensitiveMap<String> row = new LinkedCaseInsensitiveMap<>();
            row.put("x", "value");
            List<LinkedCaseInsensitiveMap<String>> content = new ArrayList<>();
            content.add(row);

            AbstractStorageOperation.QueryResult<Object> qr =
                    new AbstractStorageOperation.QueryResult<>("SELECT x FROM y");
            qr.setContent(content);
            List<AbstractStorageOperation.QueryResult<?>> rawList = new ArrayList<>();
            rawList.add(qr);

            AbstractStorageOperation.StorageOperationResult opResult =
                    new AbstractStorageOperation.StorageOperationResult(rawList);
            when(storageOperation.apply(any(ListSource.class), eq("DEFAULT"))).thenReturn(opResult);

            CommandResult result = new CommandResult();
            variableHelper.getSQLResult(fromSQL, "v", result);

            assertEquals("DEFAULT", fromSQL.getAlias());
            verify(aliasToStorageOperation).getByNameOrThrow("POSTGRES_DEFAULT");
        }

        @SuppressWarnings("unchecked")
        @Test
        void preservesExistingAlias() {
            FromSQL fromSQL = new FromSQL();
            fromSQL.setAlias("customAlias");
            fromSQL.setDbType(RelationalDB.MYSQL);
            fromSQL.setQuery("SELECT a FROM b");

            AbstractStorageOperation storageOperation = mock(AbstractStorageOperation.class);
            when(aliasToStorageOperation.getByNameOrThrow("MYSQL_customAlias")).thenReturn(storageOperation);

            LinkedCaseInsensitiveMap<String> row = new LinkedCaseInsensitiveMap<>();
            row.put("a", "val");
            List<LinkedCaseInsensitiveMap<String>> content = new ArrayList<>();
            content.add(row);

            AbstractStorageOperation.QueryResult<Object> qr =
                    new AbstractStorageOperation.QueryResult<>("SELECT a FROM b");
            qr.setContent(content);
            List<AbstractStorageOperation.QueryResult<?>> rawList = new ArrayList<>();
            rawList.add(qr);

            AbstractStorageOperation.StorageOperationResult opResult =
                    new AbstractStorageOperation.StorageOperationResult(rawList);
            when(storageOperation.apply(any(ListSource.class), eq("customAlias"))).thenReturn(opResult);

            CommandResult result = new CommandResult();
            variableHelper.getSQLResult(fromSQL, "v", result);

            assertEquals("customAlias", fromSQL.getAlias());
            verify(aliasToStorageOperation).getByNameOrThrow("MYSQL_customAlias");
        }
    }

    @Nested
    class EvaluateJPathTests {

        @Test
        void evaluatesJsonPathWithArray() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("$.items[0]");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", "{\"items\":[\"first\",\"second\"]}");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "arrayVar", context, result, s -> s);

            assertEquals("first", value);
        }

        @Test
        void evaluatesDeepJsonPath() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("$..name");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", "{\"person\":{\"name\":\"Alice\"}}");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "deepVar", context, result, s -> s);

            assertNotNull(value);
            assertTrue(value.contains("Alice"));
        }
    }

    @Nested
    class EvaluateXPathTests {

        @Test
        void evaluatesXPathWithAttributes() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("/root/item/@id");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            String xmlBody = "<root><item id=\"123\">content</item></root>";
            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", xmlBody);
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "attrVar", context, result, s -> s);

            assertEquals("123", value);
        }

        @Test
        void evaluatesXPathReturningEmptyForNonExistentElement() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("/root/missing");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            String xmlBody = "<root><item>value</item></root>";
            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", xmlBody);
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "missingXml", context, result, s -> s);

            assertNotNull(value);
            assertEquals("", value);
        }

        @Test
        void evaluatesNestedXPath() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("/root/parent/child");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            String xmlBody = "<root><parent><child>nestedValue</child></parent></root>";
            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", xmlBody);
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "nestedXPath", context, result, s -> s);

            assertEquals("nestedValue", value);
        }
    }

    @Nested
    class GetKeyOfQueryResultValueTests {

        @SuppressWarnings("unchecked")
        @Test
        void extractsKeyFromSelectQuery() {
            FromSQL fromSQL = new FromSQL();
            fromSQL.setDbType(RelationalDB.POSTGRES);
            fromSQL.setAlias("alias");
            fromSQL.setQuery("SELECT email FROM users WHERE id = 1");

            AbstractStorageOperation storageOperation = mock(AbstractStorageOperation.class);
            when(aliasToStorageOperation.getByNameOrThrow("POSTGRES_alias")).thenReturn(storageOperation);

            LinkedCaseInsensitiveMap<String> row = new LinkedCaseInsensitiveMap<>();
            row.put("email", "test@example.com");
            List<LinkedCaseInsensitiveMap<String>> content = new ArrayList<>();
            content.add(row);

            AbstractStorageOperation.QueryResult<Object> qr =
                    new AbstractStorageOperation.QueryResult<>("SELECT email FROM users WHERE id = 1");
            qr.setContent(content);
            List<AbstractStorageOperation.QueryResult<?>> rawList = new ArrayList<>();
            rawList.add(qr);

            AbstractStorageOperation.StorageOperationResult opResult =
                    new AbstractStorageOperation.StorageOperationResult(rawList);
            when(storageOperation.apply(any(ListSource.class), eq("alias"))).thenReturn(opResult);

            CommandResult result = new CommandResult();

            String value = variableHelper.getSQLResult(fromSQL, "emailVar", result);

            assertEquals("test@example.com", value);
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void getExpressionResultWithTernary() {
            FromExpression fromExpression = mock(FromExpression.class);
            when(fromExpression.getValue()).thenReturn("true ? 'yes' : 'no'");
            CommandResult result = new CommandResult();

            String value = variableHelper.getExpressionResult(fromExpression, "ternaryVar", result);

            assertEquals("yes", value);
        }

        @Test
        void jsonPathWithIntegerValue() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("$.count");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", "{\"count\":100}");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "intVar", context, result, s -> s);

            assertEquals("100", value);
        }

        @Test
        void jsonPathWithBooleanValue() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("$.active");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", "{\"active\":true}");
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "boolVar", context, result, s -> s);

            assertEquals("true", value);
        }

        @Test
        void xPathWithMultipleElements() {
            FromPath fromPath = new FromPath();
            fromPath.setValue("/root/item[2]");
            fromPath.setFromFile(null);
            fromPath.setFromVar(null);

            String xmlBody = "<root><item>first</item><item>second</item></root>";
            Map<String, String> contextMap = new LinkedHashMap<>();
            contextMap.put("body", xmlBody);
            ScenarioContext context = new ScenarioContext(contextMap);
            CommandResult result = new CommandResult();

            String value = variableHelper.getPathResult(fromPath, "secondItem", context, result, s -> s);

            assertEquals("second", value);
        }
    }
}
