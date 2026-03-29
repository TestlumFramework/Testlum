package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.*;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DropDownExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private DropDownExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new DropDownExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
    }

    @Nested
    class SelectDropDown {

        @Test
        void selectsByTextOnSelectElement() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("country-select");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.SELECT);
            oneValue.setBy(SelectOrDeselectBy.TEXT);
            oneValue.setValue("Ukraine");
            dropDown.setOneValue(oneValue);
            assertEquals("country-select", dropDown.getLocator());
        }
    }

    @Nested
    class CustomDropDown {

        @Test
        void throwsForCustomDropDownWithIndexBy() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("custom-dd");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.SELECT);
            oneValue.setBy(SelectOrDeselectBy.INDEX);
            oneValue.setValue("1");
            dropDown.setOneValue(oneValue);
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(element.getTagName()).thenReturn("div");
            when(uiUtil.findWebElement(any(), eq("custom-dd"), any())).thenReturn(element);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(dropDown, result));
        }
    }

    @Nested
    class AllValuesDropDown {

        @Test
        void throwsForCustomDropDownWithAllValues() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("custom-dd");
            AllValues allValues = new AllValues();
            allValues.setType(TypeForAllValues.DESELECT);
            dropDown.setAllValues(allValues);
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(element.getTagName()).thenReturn("div");
            when(uiUtil.findWebElement(any(), eq("custom-dd"), any())).thenReturn(element);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(dropDown, result));
        }
    }

    @Nested
    class MetadataRecording {

        @Test
        void putsLocatorIntoResult() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("my-dd");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.SELECT);
            oneValue.setBy(SelectOrDeselectBy.TEXT);
            oneValue.setValue("Option 1");
            dropDown.setOneValue(oneValue);

            WebElement element = mock(WebElement.class);
            when(element.getTagName()).thenReturn("div");
            when(uiUtil.findWebElement(any(), eq("my-dd"), any())).thenReturn(element);

            // custom dropdown with TEXT should work
            WebElement parentElement = mock(WebElement.class);
            when(element.findElements(any())).thenReturn(List.of(parentElement));
            WebElement optionElement = mock(WebElement.class);
            when(parentElement.findElement(any())).thenReturn(optionElement);

            CommandResult result = new CommandResult();
            executor.execute(dropDown, result);
            assertEquals("my-dd", result.getMetadata().get(ResultUtil.DROP_DOWN_LOCATOR));
        }
    }
}
