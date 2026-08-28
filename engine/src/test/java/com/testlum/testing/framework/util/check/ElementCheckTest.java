package com.testlum.testing.framework.util.check;

import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.UiType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElementCheckTest {

    private ExecutorDependencies deps(final WebDriver driver, final UiType uiType) {
        return ExecutorDependencies.builder().driver(driver).uiType(uiType).build();
    }

    private WebElement visibleElement() {
        WebElement element = mock(WebElement.class);
        when(element.isDisplayed()).thenReturn(true);
        when(element.getSize()).thenReturn(new Dimension(10, 20));
        return element;
    }

    private interface JsWebDriver extends WebDriver, JavascriptExecutor {
    }

    @Nested
    class Visibility {

        @Test
        void passesForDisplayedElementWithSize() {
            assertDoesNotThrow(() -> ElementCheck.VISIBILITY.check(
                    deps(mock(WebDriver.class), UiType.WEB), visibleElement()));
        }

        @Test
        void failsWhenElementIsNotDisplayed() {
            WebElement element = mock(WebElement.class);
            when(element.isDisplayed()).thenReturn(false);

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> ElementCheck.VISIBILITY.check(deps(mock(WebDriver.class), UiType.WEB), element));
            assertEquals(LogMessage.UI_ELEMENT_IS_NOT_VISIBLE_EXCEPTION_MESSAGE, ex.getMessage());
        }

        @Test
        void failsWhenElementHasZeroWidth() {
            WebElement element = mock(WebElement.class);
            when(element.isDisplayed()).thenReturn(true);
            when(element.getSize()).thenReturn(new Dimension(0, 20));

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> ElementCheck.VISIBILITY.check(deps(mock(WebDriver.class), UiType.WEB), element));
            assertEquals(LogMessage.UI_ELEMENT_HAS_ZERO_SIZE_EXCEPTION_MESSAGE, ex.getMessage());
        }

        @Test
        void failsWhenElementHasZeroHeight() {
            WebElement element = mock(WebElement.class);
            when(element.isDisplayed()).thenReturn(true);
            when(element.getSize()).thenReturn(new Dimension(10, 0));

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> ElementCheck.VISIBILITY.check(deps(mock(WebDriver.class), UiType.WEB), element));
            assertEquals(LogMessage.UI_ELEMENT_HAS_ZERO_SIZE_EXCEPTION_MESSAGE, ex.getMessage());
        }

        @Test
        void appliesToEveryUiType() {
            for (UiType uiType : UiType.values()) {
                assertTrue(ElementCheck.VISIBILITY.supports(uiType));
            }
        }
    }

    @Nested
    class ScrolledIntoViewAndInteractable {

        @Test
        void passesWhenElementIsTheTopmostOneAtItsCenter() {
            JsWebDriver driver = mock(JsWebDriver.class);
            when(driver.executeScript(anyString(), any())).thenReturn(Boolean.TRUE);

            assertDoesNotThrow(() -> ElementCheck.SCROLLED_INTO_VIEW_AND_INTERACTABLE.check(
                    deps(driver, UiType.WEB), mock(WebElement.class)));
        }

        @Test
        void failsWhenElementIsCovered() {
            JsWebDriver driver = mock(JsWebDriver.class);
            when(driver.executeScript(anyString(), any())).thenReturn(Boolean.FALSE);

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> ElementCheck.SCROLLED_INTO_VIEW_AND_INTERACTABLE.check(
                            deps(driver, UiType.WEB), mock(WebElement.class)));
            assertEquals(LogMessage.UI_ELEMENT_IS_NOT_INTERACTABLE_EXCEPTION_MESSAGE, ex.getMessage());
        }

        @Test
        void failsWhenScriptReturnsNothingUsable() {
            JsWebDriver driver = mock(JsWebDriver.class);
            when(driver.executeScript(anyString(), any())).thenReturn(null);

            assertThrows(DefaultFrameworkException.class,
                    () -> ElementCheck.SCROLLED_INTO_VIEW_AND_INTERACTABLE.check(
                            deps(driver, UiType.WEB), mock(WebElement.class)));
        }

        @Test
        void isNotApplicableToNative() {
            assertFalse(ElementCheck.SCROLLED_INTO_VIEW_AND_INTERACTABLE.supports(UiType.NATIVE));
            assertTrue(ElementCheck.SCROLLED_INTO_VIEW_AND_INTERACTABLE.supports(UiType.WEB));
            assertTrue(ElementCheck.SCROLLED_INTO_VIEW_AND_INTERACTABLE.supports(UiType.MOBILE_BROWSER));
        }
    }

    @Nested
    class Enabled {

        @Test
        void passesForEnabledElementWithoutAriaDisabled() {
            WebElement element = mock(WebElement.class);
            when(element.isEnabled()).thenReturn(true);
            when(element.getAttribute("aria-disabled")).thenReturn(null);

            assertDoesNotThrow(() -> ElementCheck.ENABLED.check(
                    deps(mock(WebDriver.class), UiType.WEB), element));
        }

        @Test
        void failsWhenDisabledAttributeIsSet() {
            WebElement element = mock(WebElement.class);
            when(element.isEnabled()).thenReturn(false);

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> ElementCheck.ENABLED.check(deps(mock(WebDriver.class), UiType.WEB), element));
            assertTrue(ex.getMessage().contains("disabled"));
        }

        @Test
        void failsWhenAriaDisabledIsTrue() {
            WebElement element = mock(WebElement.class);
            when(element.isEnabled()).thenReturn(true);
            when(element.getAttribute("aria-disabled")).thenReturn("true");

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> ElementCheck.ENABLED.check(deps(mock(WebDriver.class), UiType.WEB), element));
            assertTrue(ex.getMessage().contains("aria-disabled"));
        }

        @Test
        void passesWhenAriaDisabledIsFalse() {
            WebElement element = mock(WebElement.class);
            when(element.isEnabled()).thenReturn(true);
            when(element.getAttribute("aria-disabled")).thenReturn("false");

            assertDoesNotThrow(() -> ElementCheck.ENABLED.check(
                    deps(mock(WebDriver.class), UiType.WEB), element));
        }
    }

    @Nested
    class Editable {

        @Test
        void passesWhenNeitherAttributeIsPresent() {
            WebElement element = mock(WebElement.class);
            when(element.getAttribute("readonly")).thenReturn(null);
            when(element.getAttribute("aria-readonly")).thenReturn(null);

            assertDoesNotThrow(() -> ElementCheck.EDITABLE.check(
                    deps(mock(WebDriver.class), UiType.WEB), element));
        }

        @Test
        void failsWhenReadonlyAttributeIsSet() {
            WebElement element = mock(WebElement.class);
            when(element.getAttribute("readonly")).thenReturn("true");

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> ElementCheck.EDITABLE.check(deps(mock(WebDriver.class), UiType.WEB), element));
            assertTrue(ex.getMessage().contains("readonly"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"true", "", "readonly", "TRUE"})
        void failsForEveryDriverSpellingOfTheBooleanAttribute(final String attributeValue) {
            WebElement element = mock(WebElement.class);
            when(element.getAttribute("readonly")).thenReturn(attributeValue);

            assertThrows(DefaultFrameworkException.class,
                    () -> ElementCheck.EDITABLE.check(deps(mock(WebDriver.class), UiType.WEB), element));
        }

        @Test
        void passesWhenReadonlyIsExplicitlyFalse() {
            WebElement element = mock(WebElement.class);
            when(element.getAttribute("readonly")).thenReturn("false");
            when(element.getAttribute("aria-readonly")).thenReturn(null);

            assertDoesNotThrow(() -> ElementCheck.EDITABLE.check(
                    deps(mock(WebDriver.class), UiType.WEB), element));
        }

        @Test
        void failsWhenAriaReadonlyIsTrue() {
            WebElement element = mock(WebElement.class);
            when(element.getAttribute("readonly")).thenReturn(null);
            when(element.getAttribute("aria-readonly")).thenReturn("true");

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> ElementCheck.EDITABLE.check(deps(mock(WebDriver.class), UiType.WEB), element));
            assertTrue(ex.getMessage().contains("aria-readonly"));
        }

        @Test
        void passesWhenAriaReadonlyIsFalse() {
            WebElement element = mock(WebElement.class);
            when(element.getAttribute("readonly")).thenReturn(null);
            when(element.getAttribute("aria-readonly")).thenReturn("false");

            assertDoesNotThrow(() -> ElementCheck.EDITABLE.check(
                    deps(mock(WebDriver.class), UiType.WEB), element));
        }

        @Test
        void isNotApplicableToNative() {
            assertFalse(ElementCheck.EDITABLE.supports(UiType.NATIVE));
            assertTrue(ElementCheck.EDITABLE.supports(UiType.WEB));
            assertTrue(ElementCheck.EDITABLE.supports(UiType.MOBILE_BROWSER));
        }
    }
}
