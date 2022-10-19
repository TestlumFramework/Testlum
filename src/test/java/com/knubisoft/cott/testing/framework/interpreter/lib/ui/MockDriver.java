package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class MockDriver implements WebDriver {
    private final String exceptionMessage;

    @Override
    public void get(String url) {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public String getCurrentUrl() {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public String getTitle() {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public List<WebElement> findElements(By by) {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public WebElement findElement(By by) {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public String getPageSource() {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public void close() {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public void quit() {

    }

    @Override
    public Set<String> getWindowHandles() {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public String getWindowHandle() {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public TargetLocator switchTo() {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public Navigation navigate() {
        throw new DefaultFrameworkException(exceptionMessage);
    }

    @Override
    public Options manage() {
        throw new DefaultFrameworkException(exceptionMessage);
    }
}
