package com.knubisoft.testlum.testing.framework.util.check;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

@Component
public class ElementCheckChain {

    public void verify(final WebDriver driver, final WebElement element, final AbstractElementCheck... checks) {
        if (checks.length == 0) {
            return;
        }
        AbstractElementCheck current = checks[0];
        for (int i = 1; i < checks.length; i++) {
            current = current.linkWith(checks[i]);
        }
        checks[0].check(driver, element);
    }
}
