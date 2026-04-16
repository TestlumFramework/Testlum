package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Javascript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsExecutorTest {

    @Mock
    private JavascriptUtil javascriptUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private JavascriptExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new JavascriptExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "javascriptUtil", javascriptUtil);
    }

    @Nested
    class Execute {

        @Test
        void readsAndExecutesJsFileCommands() {
            Javascript javascript = new Javascript();
            javascript.setFile("scroll.js");
            CommandResult result = new CommandResult();
            when(javascriptUtil.readCommands("scroll.js")).thenReturn("window.scrollTo(0, 100);");

            executor.execute(javascript, result);

            verify(javascriptUtil).readCommands("scroll.js");
            verify(javascriptUtil).executeJsScript("window.scrollTo(0, 100);", driver);
            assertEquals("scroll.js", result.getMetadata().get(ResultUtil.JS_FILE));
        }
    }
}
