package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.service.EmailInboxService;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.UiUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.InputEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InputEmailExecutorTest {

    private static final String DEV_ENV = "dev";
    private static final String TEST_ALIAS = "mainInbox";
    private static final String LOCATOR = "//input[@id='otp']";

    @Mock
    private EmailInboxService emailInboxService;

    @Mock
    private UiUtil uiUtil;

    @Mock
    private WebDriver driver;

    @Mock
    private ApplicationContext context;

    private ScenarioContext scenarioContext;
    private Map<AliasEnv, EmailInboxService> servicesMap;
    private InputEmailExecutor executor;

    @BeforeEach
    void setUp() {
        this.servicesMap = new HashMap<>();
        this.servicesMap.put(new AliasEnv(TEST_ALIAS, DEV_ENV), this.emailInboxService);

        when(this.context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        when(this.context.containsBean("emailInboxServices")).thenReturn(true);
        when(this.context.getBean("emailInboxServices", Map.class)).thenReturn(this.servicesMap);

        this.scenarioContext = new ScenarioContext(new HashMap<>());
        final ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(this.context)
                .scenarioContext(this.scenarioContext)
                .driver(this.driver)
                .environment(DEV_ENV)
                .build();

        this.executor = new InputEmailExecutor(dependencies);
        ReflectionTestUtils.setField(this.executor, "uiUtil", this.uiUtil);
    }

    @Nested
    class Construction {

        @Test
        void buildsSuccessfully() {
            assertNotNull(InputEmailExecutorTest.this.executor);
        }

        @Test
        void buildsWhenBeanMissing() {
            final ApplicationContext emptyCtx = mock(ApplicationContext.class);
            when(emptyCtx.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
            when(emptyCtx.containsBean("emailInboxServices")).thenReturn(false);

            final ExecutorDependencies deps = ExecutorDependencies.builder()
                    .context(emptyCtx)
                    .scenarioContext(InputEmailExecutorTest.this.scenarioContext)
                    .environment(DEV_ENV)
                    .build();

            assertNotNull(new InputEmailExecutor(deps));
        }
    }

    @Nested
    class Execution {

        @Test
        void findsElementTypesExtractedCodeHighlightsAndSetsTargetVariable() {
            final InputEmail inputEmail = new InputEmail();
            inputEmail.setLocator(LOCATOR);
            inputEmail.setAlias(TEST_ALIAS);
            inputEmail.setPattern("code: (\\d+)");
            inputEmail.setTimeout(BigInteger.valueOf(5000L));
            inputEmail.setHighlight(true);
            inputEmail.setTargetVariable("myOtp");

            when(InputEmailExecutorTest.this.emailInboxService.fetchValueByPattern("code: (\\d+)", 5000L))
                    .thenReturn("654321");

            final WebElement element = mock(WebElement.class);
            when(InputEmailExecutorTest.this.uiUtil.findWebElement(any(), eq(LOCATOR), any(),
                    eq(ElementChecks.FOR_WRITING))).thenReturn(element);

            final CommandResult result = new CommandResult();
            InputEmailExecutorTest.this.executor.execute(inputEmail, result);

            verify(element).sendKeys("654321");
            verify(InputEmailExecutorTest.this.uiUtil).highlightElementIfRequired(
                    eq(true), eq(element), eq(InputEmailExecutorTest.this.driver));
            verify(InputEmailExecutorTest.this.uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());

            assertEquals("654321", InputEmailExecutorTest.this.scenarioContext.get("myOtp"));
            assertEquals(LOCATOR, result.getMetadata().get(ResultUtil.INPUT_LOCATOR));
            assertEquals("654321", result.getMetadata().get(ResultUtil.INPUT_VALUE));
            assertEquals(TEST_ALIAS, result.getMetadata().get("Alias"));
            assertEquals("654321", result.getMetadata().get("Extracted Value"));
            assertEquals("654321", result.getActual());
        }

        @Test
        void executesWithoutTargetVariable() {
            final InputEmail inputEmail = new InputEmail();
            inputEmail.setLocator(LOCATOR);
            inputEmail.setAlias(TEST_ALIAS);
            inputEmail.setPattern("pin: (\\d+)");
            inputEmail.setTimeout(BigInteger.valueOf(3000L));

            when(InputEmailExecutorTest.this.emailInboxService.fetchValueByPattern("pin: (\\d+)", 3000L))
                    .thenReturn("1122");

            final WebElement element = mock(WebElement.class);
            when(InputEmailExecutorTest.this.uiUtil.findWebElement(any(), eq(LOCATOR), any(),
                    eq(ElementChecks.FOR_WRITING))).thenReturn(element);

            final CommandResult result = new CommandResult();
            InputEmailExecutorTest.this.executor.execute(inputEmail, result);

            verify(element).sendKeys("1122");
            assertFalse(InputEmailExecutorTest.this.scenarioContext.containsKey("myOtp"));
            assertEquals("1122", result.getMetadata().get("Extracted Value"));
            assertEquals("1122", result.getActual());
        }

        @Test
        void appliesDefaultAliasWhenNull() {
            final InputEmail inputEmail = new InputEmail();
            inputEmail.setLocator(LOCATOR);
            inputEmail.setAlias(null);
            inputEmail.setPattern(".*");
            inputEmail.setTimeout(BigInteger.valueOf(1000L));

            InputEmailExecutorTest.this.servicesMap.put(
                    new AliasEnv("DEFAULT", DEV_ENV), InputEmailExecutorTest.this.emailInboxService);
            when(InputEmailExecutorTest.this.emailInboxService.fetchValueByPattern(".*", 1000L))
                    .thenReturn("autoCode");

            final WebElement element = mock(WebElement.class);
            when(InputEmailExecutorTest.this.uiUtil.findWebElement(any(), eq(LOCATOR), any(),
                    eq(ElementChecks.FOR_WRITING))).thenReturn(element);

            final CommandResult result = new CommandResult();
            InputEmailExecutorTest.this.executor.execute(inputEmail, result);

            assertEquals("DEFAULT", result.getMetadata().get("Alias"));
            assertEquals("autoCode", result.getActual());
        }

        @Test
        void throwsExceptionWhenServiceNotFound() {
            final InputEmail inputEmail = new InputEmail();
            inputEmail.setLocator(LOCATOR);
            inputEmail.setAlias("unknownAlias");
            inputEmail.setPattern(".*");

            final CommandResult result = new CommandResult();
            final DefaultFrameworkException ex = assertThrows(
                    DefaultFrameworkException.class,
                    () -> InputEmailExecutorTest.this.executor.execute(inputEmail, result));

            assertEquals("Email inbox configuration not found for alias 'unknownAlias' and environment 'dev'",
                    ex.getMessage());
        }
    }
}
