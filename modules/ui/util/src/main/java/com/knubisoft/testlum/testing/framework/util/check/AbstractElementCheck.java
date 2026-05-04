package com.knubisoft.testlum.testing.framework.util.check;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class AbstractElementCheck {

    private AbstractElementCheck next;

    public final AbstractElementCheck linkWith(final AbstractElementCheck next) {
        this.next = next;
        return next;
    }

    public final void check(final WebDriver driver, final WebElement element) {
        doCheck(driver, element);
        if (next != null) {
            next.check(driver, element);
        }
    }

    protected abstract void doCheck(WebDriver driver, WebElement element);
}
