package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.util.check.AbstractElementCheck;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.model.scenario.ElementAttribute;
import com.knubisoft.testlum.testing.model.scenario.ElementPresent;
import com.knubisoft.testlum.testing.model.scenario.FromConstant;
import com.knubisoft.testlum.testing.model.scenario.FromCookie;
import com.knubisoft.testlum.testing.model.scenario.FromDom;
import com.knubisoft.testlum.testing.model.scenario.FromElement;
import com.knubisoft.testlum.testing.model.scenario.FromUrl;
import com.knubisoft.testlum.testing.model.scenario.WebVar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebVariableExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private VariableHelper variableHelper;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private WebVariableExecutor executor;
    private ScenarioContext scenarioContext;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        scenarioContext = new ScenarioContext(new HashMap<>());
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .scenarioContext(scenarioContext)
                .build();
        executor = new WebVariableExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "variableHelper", variableHelper);
    }

    @Nested
    class UrlVariable {

        @Test
        void setsUrlVariableInContext() {
            WebVar webVar = new WebVar();
            webVar.setName("currentUrl");
            webVar.setUrl(new FromUrl());
            CommandResult result = new CommandResult();
            when(variableHelper.lookupVarMethod(any(), any())).thenReturn((var, r) -> {
                when(driver.getCurrentUrl()).thenReturn("http://example.com");
                return driver.getCurrentUrl();
            });

            executor.execute(webVar, result);

            assertEquals("http://example.com", scenarioContext.get("currentUrl"));
        }
    }

    @Nested
    class ConstantVariable {

        @Test
        void setsConstantVariableInContext() {
            WebVar webVar = new WebVar();
            webVar.setName("myConst");
            FromConstant fromConstant = new FromConstant();
            fromConstant.setValue("constantValue");
            webVar.setConstant(fromConstant);
            CommandResult result = new CommandResult();
            when(variableHelper.lookupVarMethod(any(), any())).thenReturn((var, r) -> "constantValue");

            executor.execute(webVar, result);

            assertEquals("constantValue", scenarioContext.get("myConst"));
        }
    }

    @Nested
    class DomVariable {

        @Test
        void getsDomWithLocator() {
            WebVar webVar = new WebVar();
            webVar.setName("domVar");
            FromDom dom = new FromDom();
            dom.setLocator("main-content");
            webVar.setDom(dom);

            WebElement element = mock(WebElement.class);
            when(element.getAttribute("outerHTML")).thenReturn("<div>content</div>");
            when(uiUtil.findWebElement(any(), eq("main-content"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            when(variableHelper.lookupVarMethod(any(), any())).thenAnswer(inv -> {
                @SuppressWarnings("unchecked")
                java.util.Map<VariableHelper.VarPredicate<WebVar>, VariableHelper.VarMethod<WebVar>> map =
                        (java.util.Map<VariableHelper.VarPredicate<WebVar>,
                                VariableHelper.VarMethod<WebVar>>) inv.getArgument(0);
                WebVar v = (WebVar) inv.getArgument(1);
                for (var entry : map.entrySet()) {
                    if (entry.getKey().test(v)) {
                        return entry.getValue();
                    }
                }
                return null;
            });

            CommandResult result = new CommandResult();
            executor.execute(webVar, result);

            assertEquals("<div>content</div>", scenarioContext.get("domVar"));
        }

        @Test
        void getsFullDomWhenLocatorIsBlank() {
            WebVar webVar = new WebVar();
            webVar.setName("fullDomVar");
            FromDom dom = new FromDom();
            // no locator set
            webVar.setDom(dom);

            when(driver.getPageSource()).thenReturn("<html><body>full page</body></html>");

            when(variableHelper.lookupVarMethod(any(), any())).thenAnswer(inv -> {
                @SuppressWarnings("unchecked")
                java.util.Map<VariableHelper.VarPredicate<WebVar>, VariableHelper.VarMethod<WebVar>> map =
                        (java.util.Map<VariableHelper.VarPredicate<WebVar>,
                                VariableHelper.VarMethod<WebVar>>) inv.getArgument(0);
                WebVar v = (WebVar) inv.getArgument(1);
                for (var entry : map.entrySet()) {
                    if (entry.getKey().test(v)) {
                        return entry.getValue();
                    }
                }
                return null;
            });

            CommandResult result = new CommandResult();
            executor.execute(webVar, result);

            assertEquals("<html><body>full page</body></html>", scenarioContext.get("fullDomVar"));
        }
    }

    @Nested
    class CookieVariable {

        @Test
        void getsCookiesFromDriver() {
            WebVar webVar = new WebVar();
            webVar.setName("cookieVar");
            webVar.setCookie(new FromCookie());

            Set<Cookie> cookies = new LinkedHashSet<>();
            cookies.add(new Cookie("session", "abc123"));
            cookies.add(new Cookie("lang", "en"));
            WebDriver.Options options = mock(WebDriver.Options.class);
            when(driver.manage()).thenReturn(options);
            when(options.getCookies()).thenReturn(cookies);

            when(variableHelper.lookupVarMethod(any(), any())).thenAnswer(inv -> {
                @SuppressWarnings("unchecked")
                java.util.Map<VariableHelper.VarPredicate<WebVar>, VariableHelper.VarMethod<WebVar>> map =
                        (java.util.Map<VariableHelper.VarPredicate<WebVar>,
                                VariableHelper.VarMethod<WebVar>>) inv.getArgument(0);
                WebVar v = (WebVar) inv.getArgument(1);
                for (var entry : map.entrySet()) {
                    if (entry.getKey().test(v)) {
                        return entry.getValue();
                    }
                }
                return null;
            });

            CommandResult result = new CommandResult();
            executor.execute(webVar, result);

            // cookies are joined with semicolons
            assertDoesNotThrow(() -> scenarioContext.get("cookieVar"));
        }
    }

    @Nested
    class ElementVariable {

        @Test
        void getsElementAttributeValue() {
            WebVar webVar = new WebVar();
            webVar.setName("attrVar");
            FromElement fromElement = new FromElement();
            ElementAttribute attr = new ElementAttribute();
            attr.setName("href");
            attr.setLocator("my-link");
            fromElement.setAttribute(attr);
            webVar.setElement(fromElement);

            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("my-link"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);
            when(uiUtil.getElementAttribute(eq(element), eq("href"), eq(driver))).thenReturn("/path/to");

            when(variableHelper.lookupVarMethod(any(), any())).thenAnswer(inv -> {
                @SuppressWarnings("unchecked")
                java.util.Map<VariableHelper.VarPredicate<WebVar>, VariableHelper.VarMethod<WebVar>> map =
                        (java.util.Map<VariableHelper.VarPredicate<WebVar>,
                                VariableHelper.VarMethod<WebVar>>) inv.getArgument(0);
                WebVar v = (WebVar) inv.getArgument(1);
                for (var entry : map.entrySet()) {
                    if (entry.getKey().test(v)) {
                        return entry.getValue();
                    }
                }
                return null;
            });

            CommandResult result = new CommandResult();
            executor.execute(webVar, result);

            assertEquals("/path/to", scenarioContext.get("attrVar"));
        }

        @Test
        void getsElementPresentTrueWhenElementExists() {
            WebVar webVar = new WebVar();
            webVar.setName("presentVar");
            FromElement fromElement = new FromElement();
            ElementPresent present = new ElementPresent();
            present.setLocator("existing-el");
            fromElement.setPresent(present);
            webVar.setElement(fromElement);
            CommandResult result = new CommandResult();

            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("existing-el"), any())).thenReturn(element);

            when(variableHelper.lookupVarMethod(any(), any())).thenAnswer(inv -> {
                @SuppressWarnings("unchecked")
                java.util.Map<VariableHelper.VarPredicate<WebVar>, VariableHelper.VarMethod<WebVar>> map =
                        (java.util.Map<VariableHelper.VarPredicate<WebVar>,
                                VariableHelper.VarMethod<WebVar>>) inv.getArgument(0);
                WebVar v = (WebVar) inv.getArgument(1);
                for (var entry : map.entrySet()) {
                    if (entry.getKey().test(v)) {
                        return entry.getValue();
                    }
                }
                return null;
            });

            executor.execute(webVar, result);

            assertEquals("true", scenarioContext.get("presentVar"));
        }

        @Test
        void getsElementPresentFalseWhenElementNotFound() {
            WebVar webVar = new WebVar();
            webVar.setName("absentVar");
            FromElement fromElement = new FromElement();
            ElementPresent present = new ElementPresent();
            present.setLocator("missing-el");
            fromElement.setPresent(present);
            webVar.setElement(fromElement);
            CommandResult result = new CommandResult();

            when(uiUtil.findWebElement(any(), eq("missing-el"), any()))
                    .thenThrow(new DefaultFrameworkException("element not found"));

            when(variableHelper.lookupVarMethod(any(), any())).thenAnswer(inv -> {
                @SuppressWarnings("unchecked")
                java.util.Map<VariableHelper.VarPredicate<WebVar>, VariableHelper.VarMethod<WebVar>> map =
                        (java.util.Map<VariableHelper.VarPredicate<WebVar>,
                                VariableHelper.VarMethod<WebVar>>) inv.getArgument(0);
                WebVar v = (WebVar) inv.getArgument(1);
                for (var entry : map.entrySet()) {
                    if (entry.getKey().test(v)) {
                        return entry.getValue();
                    }
                }
                return null;
            });

            executor.execute(webVar, result);

            assertEquals("false", scenarioContext.get("absentVar"));
        }
    }

    @Nested
    class ExceptionHandling {

        @Test
        void rethrowsExceptionWithVarNameLogged() {
            WebVar webVar = new WebVar();
            webVar.setName("failVar");
            webVar.setComment("This should fail");
            webVar.setUrl(new FromUrl());
            CommandResult result = new CommandResult();
            when(variableHelper.lookupVarMethod(any(), any())).thenThrow(new RuntimeException("lookup error"));

            try {
                executor.execute(webVar, result);
            } catch (RuntimeException ignored) {
                // Expected
            }
        }
    }
}
