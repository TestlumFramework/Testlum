package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DragAndDropExecutorTest {

    interface InteractiveWebDriver extends WebDriver, Interactive { }

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private FileSearcher fileSearcher;
    @Mock
    private JavascriptUtil javascriptUtil;
    @Mock
    private InteractiveWebDriver driver;
    @Mock
    private ApplicationContext context;

    private DragAndDropExecutor executor;
    private File scenarioFile;

    @BeforeEach
    void setUp() {
        scenarioFile = mock(File.class);
        File parentDir = mock(File.class);
        lenient().when(scenarioFile.getParentFile()).thenReturn(parentDir);
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .file(scenarioFile)
                .build();
        executor = new DragAndDropExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "fileSearcher", fileSearcher);
        ReflectionTestUtils.setField(executor, "javascriptUtil", javascriptUtil);
    }

    @Nested
    class ElementDragAndDrop {

        @Test
        void dragsElementToTarget() {
            DragAndDrop dragAndDrop = new DragAndDrop();
            dragAndDrop.setToLocator("target-box");
            dragAndDrop.setFromLocator("source-box");
            CommandResult result = new CommandResult();
            WebElement target = mock(WebElement.class);
            WebElement source = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("target-box"), any())).thenReturn(target);
            when(uiUtil.findWebElement(any(), eq("source-box"), any())).thenReturn(source);

            executor.execute(dragAndDrop, result);

            verify(logUtil).logDragAndDropInfo(eq(dragAndDrop));
            verify(resultUtil).addDragAndDropMetaDada(eq(dragAndDrop), eq(result));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }
    }

    @Nested
    class FileDragAndDrop {

        @Test
        void throwsWhenFileDoesNotExist() {
            DragAndDrop dragAndDrop = new DragAndDrop();
            dragAndDrop.setToLocator("drop-zone");
            dragAndDrop.setFileName("test.png");

            WebElement target = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("drop-zone"), any())).thenReturn(target);
            File fakeFile = mock(File.class);
            when(fakeFile.exists()).thenReturn(false);
            when(fakeFile.getName()).thenReturn("test.png");
            when(fileSearcher.searchFileFromDir(any(), eq("test.png"))).thenReturn(fakeFile);

            CommandResult result = new CommandResult();
            assertThrows(DefaultFrameworkException.class, () -> executor.execute(dragAndDrop, result));
        }
    }
}
