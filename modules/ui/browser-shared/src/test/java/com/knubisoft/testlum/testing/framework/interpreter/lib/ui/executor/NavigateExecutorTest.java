package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Navigate;
import com.knubisoft.testlum.testing.model.scenario.NavigateCommand;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NavigateExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private WebDriver.Navigation navigation;
    @Mock
    private ApplicationContext context;

    private NavigateExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        when(driver.navigate()).thenReturn(navigation);
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .environment("dev")
                .uiType(UiType.WEB)
                .build();
        executor = new NavigateExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
    }

    @Nested
    class BackCommand {

        @Test
        void navigatesBack() {
            Navigate navigate = new Navigate();
            navigate.setCommand(NavigateCommand.BACK);
            CommandResult result = new CommandResult();

            executor.execute(navigate, result);

            verify(navigation).back();
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
            assertEquals(NavigateCommand.BACK.value(), result.getMetadata().get(ResultUtil.NAVIGATE_TYPE));
        }
    }

    @Nested
    class ReloadCommand {

        @Test
        void navigatesRefresh() {
            Navigate navigate = new Navigate();
            navigate.setCommand(NavigateCommand.RELOAD);
            CommandResult result = new CommandResult();

            executor.execute(navigate, result);

            verify(navigation).refresh();
        }

        @Test
        void putsReloadTypeInResult() {
            Navigate navigate = new Navigate();
            navigate.setCommand(NavigateCommand.RELOAD);
            CommandResult result = new CommandResult();

            executor.execute(navigate, result);

            assertEquals(NavigateCommand.RELOAD.value(), result.getMetadata().get(ResultUtil.NAVIGATE_TYPE));
        }
    }

    @Nested
    class ToCommand {

        @Test
        void navigatesToUrl() {
            Navigate navigate = new Navigate();
            navigate.setCommand(NavigateCommand.TO);
            navigate.setPath("/home");
            CommandResult result = new CommandResult();
            when(uiUtil.getUrl(eq("/home"), eq("dev"), eq(UiType.WEB))).thenReturn("http://localhost/home");

            executor.execute(navigate, result);

            verify(navigation).to("http://localhost/home");
            assertEquals("/home", result.getMetadata().get(ResultUtil.NAVIGATE_URL));
        }

        @Test
        void putsToTypeInResult() {
            Navigate navigate = new Navigate();
            navigate.setCommand(NavigateCommand.TO);
            navigate.setPath("/dashboard");
            CommandResult result = new CommandResult();
            when(uiUtil.getUrl(eq("/dashboard"), eq("dev"), eq(UiType.WEB))).thenReturn("http://localhost/dashboard");

            executor.execute(navigate, result);

            assertEquals(NavigateCommand.TO.value(), result.getMetadata().get(ResultUtil.NAVIGATE_TYPE));
            assertEquals("/dashboard", result.getMetadata().get(ResultUtil.NAVIGATE_URL));
        }

        @Test
        void takesScreenshotAfterNavigation() {
            Navigate navigate = new Navigate();
            navigate.setCommand(NavigateCommand.TO);
            navigate.setPath("/page");
            CommandResult result = new CommandResult();
            when(uiUtil.getUrl(any(), any(), any())).thenReturn("http://localhost/page");

            executor.execute(navigate, result);

            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }
    }
}
