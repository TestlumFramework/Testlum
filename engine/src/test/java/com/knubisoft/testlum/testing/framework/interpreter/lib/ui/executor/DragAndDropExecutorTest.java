package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.UiLogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.util.check.AbstractElementCheck;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    private UiLogUtil uiLogUtil;
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
        ReflectionTestUtils.setField(executor, "uiLogUtil", uiLogUtil);
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
            when(uiUtil.findWebElement(any(), eq("target-box"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(target);
            when(uiUtil.findWebElement(any(), eq("source-box"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(source);

            executor.execute(dragAndDrop, result);

            verify(uiLogUtil).logDragAndDropInfo(eq(dragAndDrop));
            verify(resultUtil).addDragAndDropMetaDada(eq(dragAndDrop), eq(result));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }

        @Test
        void addsMetadataBeforeDragElement() {
            DragAndDrop dragAndDrop = new DragAndDrop();
            dragAndDrop.setToLocator("to");
            dragAndDrop.setFromLocator("from");
            WebElement target = mock(WebElement.class);
            WebElement source = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("to"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(target);
            when(uiUtil.findWebElement(any(), eq("from"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(source);
            CommandResult result = new CommandResult();

            executor.execute(dragAndDrop, result);

            verify(resultUtil).addDragAndDropMetaDada(eq(dragAndDrop), eq(result));
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
            when(uiUtil.findWebElement(any(), eq("drop-zone"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(target);
            File fakeFile = mock(File.class);
            when(fakeFile.exists()).thenReturn(false);
            when(fakeFile.getName()).thenReturn("test.png");
            when(fileSearcher.searchFileFromDir(any(), eq("test.png"))).thenReturn(fakeFile);

            CommandResult result = new CommandResult();
            assertThrows(DefaultFrameworkException.class, () -> executor.execute(dragAndDrop, result));
        }

        @Test
        void throwsWhenFileExistsButIsNotAFile() {
            DragAndDrop dragAndDrop = new DragAndDrop();
            dragAndDrop.setToLocator("drop-zone");
            dragAndDrop.setFileName("dir-name");

            WebElement target = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("drop-zone"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(target);
            File fakeFile = mock(File.class);
            when(fakeFile.exists()).thenReturn(true);
            when(fakeFile.isFile()).thenReturn(false);
            when(fakeFile.getName()).thenReturn("dir-name");
            when(fileSearcher.searchFileFromDir(any(), eq("dir-name"))).thenReturn(fakeFile);

            CommandResult result = new CommandResult();
            assertThrows(DefaultFrameworkException.class, () -> executor.execute(dragAndDrop, result));
        }

        @Test
        void dropsFileToInputElement() {
            DragAndDrop dragAndDrop = new DragAndDrop();
            dragAndDrop.setToLocator("file-input");
            dragAndDrop.setFileName("upload.txt");

            WebElement inputTarget = mock(WebElement.class);
            when(inputTarget.getTagName()).thenReturn("input");
            when(uiUtil.findWebElement(any(), eq("file-input"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(inputTarget);

            File realFile = mock(File.class);
            when(realFile.exists()).thenReturn(true);
            when(realFile.isFile()).thenReturn(true);
            when(realFile.getAbsolutePath()).thenReturn("/tmp/upload.txt");
            when(fileSearcher.searchFileFromDir(any(), eq("upload.txt"))).thenReturn(realFile);

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(dragAndDrop, result));
            verify(inputTarget).sendKeys("/tmp/upload.txt");
        }

        @Test
        void dropsFileToNonInputElementViaJavascript() {
            DragAndDrop dragAndDrop = new DragAndDrop();
            dragAndDrop.setToLocator("drop-div");
            dragAndDrop.setFileName("image.png");

            WebElement divTarget = mock(WebElement.class);
            when(divTarget.getTagName()).thenReturn("div");
            when(uiUtil.findWebElement(any(), eq("drop-div"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(divTarget);

            WebElement inputCreated = mock(WebElement.class);
            when(javascriptUtil.executeJsScript(any(String.class), eq(driver), eq(divTarget))).thenReturn(inputCreated);

            File realFile = mock(File.class);
            when(realFile.exists()).thenReturn(true);
            when(realFile.isFile()).thenReturn(true);
            when(realFile.getAbsolutePath()).thenReturn("/tmp/image.png");
            when(fileSearcher.searchFileFromDir(any(), eq("image.png"))).thenReturn(realFile);

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(dragAndDrop, result));
            verify(inputCreated).sendKeys("/tmp/image.png");
        }

        @Test
        void retriesWithLocalFileDetectorOnInvalidArgument() {
            DragAndDrop dragAndDrop = new DragAndDrop();
            dragAndDrop.setToLocator("remote-input");
            dragAndDrop.setFileName("doc.pdf");

            WebElement inputTarget = mock(org.openqa.selenium.remote.RemoteWebElement.class);
            when(inputTarget.getTagName()).thenReturn("input");
            when(uiUtil.findWebElement(any(), eq("remote-input"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(inputTarget);
            doThrow(new InvalidArgumentException("invalid"))
                    .doNothing()
                    .when(inputTarget).sendKeys(any(CharSequence[].class));

            File realFile = mock(File.class);
            when(realFile.exists()).thenReturn(true);
            when(realFile.isFile()).thenReturn(true);
            when(realFile.getAbsolutePath()).thenReturn("/tmp/doc.pdf");
            when(fileSearcher.searchFileFromDir(any(), eq("doc.pdf"))).thenReturn(realFile);

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(dragAndDrop, result));
            verify(inputTarget, times(2)).sendKeys(eq("/tmp/doc.pdf"));
        }
    }
}
