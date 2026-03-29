package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.scenario.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompareImageExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private FileSearcher fileSearcher;
    @Mock
    private ImageComparisonUtil imageComparisonUtil;
    @Mock
    private ImageComparator imageComparator;
    @Mock
    private EnvironmentLoader environmentLoader;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private CompareImageExecutor executor;
    private File scenarioFile;

    @BeforeEach
    void setUp() {
        scenarioFile = mock(File.class);
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .file(scenarioFile)
                .build();
        executor = new CompareImageExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "fileSearcher", fileSearcher);
        ReflectionTestUtils.setField(executor, "imageComparisonUtil", imageComparisonUtil);
        ReflectionTestUtils.setField(executor, "imageComparator", imageComparator);
        ReflectionTestUtils.setField(executor, "currentEnvironmentLoader", environmentLoader);
    }

    @Nested
    class Execute {

        @Test
        void logsAndAddsMetadata() {
            Image image = new Image();
            image.setFile("expected.png");
            CommandResult result = new CommandResult();

            // The execute method reads a file and compares images, which requires
            // real file I/O. We verify the initial logging and metadata calls.
            File expectedFile = mock(File.class);
            when(fileSearcher.searchFileFromDir(any(), any())).thenReturn(expectedFile);

            try {
                executor.execute(image, result);
            } catch (Exception ignored) {
                // Expected since we cannot provide real image files in unit tests
            }

            verify(logUtil).logImageComparisonInfo(image);
            verify(resultUtil).addImageComparisonMetaData(image, result);
        }
    }
}
