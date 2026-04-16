package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

class MockDriverTest {

    private static final String ERROR_MESSAGE = "Driver is not available";
    private MockDriver mockDriver;

    @BeforeEach
    void setUp() {
        mockDriver = new MockDriver(ERROR_MESSAGE);
    }

    @Test
    void getShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.get("http://example.com"));
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void getCurrentUrlShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.getCurrentUrl());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void getTitleShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.getTitle());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void findElementsShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.findElements(By.id("test")));
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void findElementShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.findElement(By.id("test")));
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void getPageSourceShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.getPageSource());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void closeShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.close());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void getWindowHandlesShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.getWindowHandles());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void getWindowHandleShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.getWindowHandle());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void switchToShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.switchTo());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void navigateShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.navigate());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void manageShouldThrowDefaultFrameworkException() {
        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> mockDriver.manage());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void quitShouldNotThrowException() {
        assertDoesNotThrow(() -> mockDriver.quit());
    }
}
