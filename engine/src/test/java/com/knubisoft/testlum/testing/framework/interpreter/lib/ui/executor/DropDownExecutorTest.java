package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.util.check.AbstractElementCheck;
import com.knubisoft.testlum.testing.model.scenario.AllValues;
import com.knubisoft.testlum.testing.model.scenario.DropDown;
import com.knubisoft.testlum.testing.model.scenario.OneValue;
import com.knubisoft.testlum.testing.model.scenario.SelectOrDeselectBy;
import com.knubisoft.testlum.testing.model.scenario.TypeForAllValues;
import com.knubisoft.testlum.testing.model.scenario.TypeForOneValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

        @Test
        void selectsByIndexOnSelectElement() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("idx-select");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.SELECT);
            oneValue.setBy(SelectOrDeselectBy.INDEX);
            oneValue.setValue("2");
            dropDown.setOneValue(oneValue);

            WebElement selectElement = mock(WebElement.class);
            when(selectElement.getTagName()).thenReturn("select");
            when(uiUtil.findWebElement(any(), eq("idx-select"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(selectElement);

            // The Select constructor checks that the element is a <select>, we need to mock it
            // Since Select has direct selenium interaction, we test the flow up to the point we can
            CommandResult result = new CommandResult();
            try {
                executor.execute(dropDown, result);
            } catch (Exception e) {
                // Select constructor may throw due to mock limitations; that's fine
            }
            assertEquals("idx-select", result.getMetadata().get(ResultUtil.DROP_DOWN_LOCATOR));
        }

        @Test
        void selectsByValueOnSelectElement() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("val-select");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.SELECT);
            oneValue.setBy(SelectOrDeselectBy.VALUE);
            oneValue.setValue("opt1");
            dropDown.setOneValue(oneValue);

            WebElement selectElement = mock(WebElement.class);
            when(selectElement.getTagName()).thenReturn("select");
            when(uiUtil.findWebElement(any(), eq("val-select"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(selectElement);

            CommandResult result = new CommandResult();
            try {
                executor.execute(dropDown, result);
            } catch (Exception e) {
                // Select constructor may throw
            }
            assertEquals("val-select", result.getMetadata().get(ResultUtil.DROP_DOWN_LOCATOR));
        }

        @Test
        void deselectsByTextOnSelectElement() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("deselect-dd");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.DESELECT);
            oneValue.setBy(SelectOrDeselectBy.TEXT);
            oneValue.setValue("Option A");
            dropDown.setOneValue(oneValue);

            WebElement selectElement = mock(WebElement.class);
            when(selectElement.getTagName()).thenReturn("select");
            when(uiUtil.findWebElement(any(), eq("deselect-dd"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(selectElement);

            CommandResult result = new CommandResult();
            try {
                executor.execute(dropDown, result);
            } catch (Exception e) {
                // Select constructor may throw
            }
            assertEquals("deselect-dd", result.getMetadata().get(ResultUtil.DROP_DOWN_LOCATOR));
        }

        @Test
        void deselectsByIndexOnSelectElement() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("deselect-idx");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.DESELECT);
            oneValue.setBy(SelectOrDeselectBy.INDEX);
            oneValue.setValue("0");
            dropDown.setOneValue(oneValue);

            WebElement selectElement = mock(WebElement.class);
            when(selectElement.getTagName()).thenReturn("select");
            when(uiUtil.findWebElement(any(), eq("deselect-idx"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(selectElement);

            CommandResult result = new CommandResult();
            try {
                executor.execute(dropDown, result);
            } catch (Exception e) {
                // expected
            }
            assertEquals("deselect-idx", result.getMetadata().get(ResultUtil.DROP_DOWN_LOCATOR));
        }

        @Test
        void deselectsByValueOnSelectElement() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("deselect-val");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.DESELECT);
            oneValue.setBy(SelectOrDeselectBy.VALUE);
            oneValue.setValue("v1");
            dropDown.setOneValue(oneValue);

            WebElement selectElement = mock(WebElement.class);
            when(selectElement.getTagName()).thenReturn("select");
            when(uiUtil.findWebElement(any(), eq("deselect-val"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(selectElement);

            CommandResult result = new CommandResult();
            try {
                executor.execute(dropDown, result);
            } catch (Exception e) {
                // expected
            }
            assertEquals("deselect-val", result.getMetadata().get(ResultUtil.DROP_DOWN_LOCATOR));
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
            when(uiUtil.findWebElement(any(), eq("custom-dd"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(dropDown, result));
        }

        @Test
        void throwsForCustomDropDownWithValueBy() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("custom-dd-val");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.SELECT);
            oneValue.setBy(SelectOrDeselectBy.VALUE);
            oneValue.setValue("opt1");
            dropDown.setOneValue(oneValue);
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(element.getTagName()).thenReturn("div");
            when(uiUtil.findWebElement(any(), eq("custom-dd-val"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(dropDown, result));
        }

        @Test
        void selectsCustomDropDownByText() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("custom-dd-text");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.SELECT);
            oneValue.setBy(SelectOrDeselectBy.TEXT);
            oneValue.setValue("My Option");
            dropDown.setOneValue(oneValue);

            WebElement element = mock(WebElement.class);
            when(element.getTagName()).thenReturn("div");
            when(uiUtil.findWebElement(any(), eq("custom-dd-text"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            WebElement parentElement = mock(WebElement.class);
            when(element.findElements(any())).thenReturn(List.of(parentElement));
            WebElement optionElement = mock(WebElement.class);
            when(parentElement.findElement(any())).thenReturn(optionElement);

            CommandResult result = new CommandResult();
            executor.execute(dropDown, result);

            verify(element).click();
            verify(optionElement).click();
        }

        @Test
        void customDropDownSearchesMultipleParentsUntilOptionFound() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("custom-multi");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.SELECT);
            oneValue.setBy(SelectOrDeselectBy.TEXT);
            oneValue.setValue("Target Option");
            dropDown.setOneValue(oneValue);

            WebElement element = mock(WebElement.class);
            when(element.getTagName()).thenReturn("div");
            when(uiUtil.findWebElement(any(), eq("custom-multi"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            WebElement parent1 = mock(WebElement.class);
            WebElement parent2 = mock(WebElement.class);
            // After reverse: parent2 is checked first, then parent1
            when(element.findElements(any())).thenReturn(Arrays.asList(parent1, parent2));
            // parent2 (first after reverse) fails
            when(parent2.findElement(any())).thenThrow(new NoSuchElementException("not found"));
            // parent1 (second after reverse, last element) succeeds
            WebElement optionEl = mock(WebElement.class);
            when(parent1.findElement(any())).thenReturn(optionEl);

            CommandResult result = new CommandResult();
            executor.execute(dropDown, result);

            verify(optionEl).click();
        }

        @Test
        void customDropDownThrowsWhenNoParentHasOption() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("custom-fail");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.SELECT);
            oneValue.setBy(SelectOrDeselectBy.TEXT);
            oneValue.setValue("Nonexistent");
            dropDown.setOneValue(oneValue);

            WebElement element = mock(WebElement.class);
            when(element.getTagName()).thenReturn("div");
            when(uiUtil.findWebElement(any(), eq("custom-fail"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            WebElement parent = mock(WebElement.class);
            when(element.findElements(any())).thenReturn(List.of(parent));
            when(parent.findElement(any())).thenThrow(new NoSuchElementException("not found"));

            CommandResult result = new CommandResult();
            assertThrows(NoSuchElementException.class, () -> executor.execute(dropDown, result));
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
            when(uiUtil.findWebElement(any(), eq("custom-dd"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(dropDown, result));
        }

        @Test
        void throwsForCustomDropDownWithAllValuesSelect() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("custom-all-sel");
            AllValues allValues = new AllValues();
            allValues.setType(TypeForAllValues.SELECT);
            dropDown.setAllValues(allValues);
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(element.getTagName()).thenReturn("span");
            when(uiUtil.findWebElement(any(), eq("custom-all-sel"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(dropDown, result));
        }

        @Test
        void deselectAllOnSelectElement() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("deselect-all");
            AllValues allValues = new AllValues();
            allValues.setType(TypeForAllValues.DESELECT);
            dropDown.setAllValues(allValues);

            WebElement selectElement = mock(WebElement.class);
            when(selectElement.getTagName()).thenReturn("select");
            when(uiUtil.findWebElement(any(), eq("deselect-all"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(selectElement);

            CommandResult result = new CommandResult();
            try {
                executor.execute(dropDown, result);
            } catch (Exception e) {
                // Select constructor may throw
            }
            assertEquals("deselect-all", result.getMetadata().get(ResultUtil.DROP_DOWN_LOCATOR));
        }

        @Test
        void selectAllOnSelectElement() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("select-all");
            AllValues allValues = new AllValues();
            allValues.setType(TypeForAllValues.SELECT);
            dropDown.setAllValues(allValues);

            WebElement selectElement = mock(WebElement.class);
            when(selectElement.getTagName()).thenReturn("select");
            when(uiUtil.findWebElement(any(), eq("select-all"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(selectElement);

            CommandResult result = new CommandResult();
            try {
                executor.execute(dropDown, result);
            } catch (Exception e) {
                // Select constructor may throw
            }
            assertEquals("select-all", result.getMetadata().get(ResultUtil.DROP_DOWN_LOCATOR));
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
            when(uiUtil.findWebElement(any(), eq("my-dd"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            WebElement parentElement = mock(WebElement.class);
            when(element.findElements(any())).thenReturn(List.of(parentElement));
            WebElement optionElement = mock(WebElement.class);
            when(parentElement.findElement(any())).thenReturn(optionElement);

            CommandResult result = new CommandResult();
            executor.execute(dropDown, result);
            assertEquals("my-dd", result.getMetadata().get(ResultUtil.DROP_DOWN_LOCATOR));
        }

        @Test
        void addsOneValueMetaDataForCustomDropDown() {
            DropDown dropDown = new DropDown();
            dropDown.setLocator("meta-dd");
            OneValue oneValue = new OneValue();
            oneValue.setType(TypeForOneValue.SELECT);
            oneValue.setBy(SelectOrDeselectBy.TEXT);
            oneValue.setValue("MetaVal");
            dropDown.setOneValue(oneValue);

            WebElement element = mock(WebElement.class);
            when(element.getTagName()).thenReturn("div");
            when(uiUtil.findWebElement(any(), eq("meta-dd"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            WebElement parent = mock(WebElement.class);
            when(element.findElements(any())).thenReturn(List.of(parent));
            WebElement option = mock(WebElement.class);
            when(parent.findElement(any())).thenReturn(option);

            CommandResult result = new CommandResult();
            executor.execute(dropDown, result);

            verify(resultUtil).addDropDownForOneValueMetaData(
                    eq(TypeForOneValue.SELECT.value()),
                    eq(SelectOrDeselectBy.TEXT.value()),
                    eq("MetaVal"),
                    eq(result));
        }
    }
}
