package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.CombinedKeyActionEnum;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@UtilityClass
public class CombinedKeyCommandUtil {

    public void combinedKeyCommand(final CombinedKeyActionEnum combinedKeyAction,
                                   final WebDriver driver,
                                   final String locatorId) {
        Actions action = new Actions(driver);
        Locator locator = GlobalLocators.getLocator(locatorId);
        WebElement element = WebElementFinder.find(locator, driver);
        if (CombinedKeyActionEnum.HIGH_LIGHT_ALL.equals(combinedKeyAction)) {
            action.keyDown(element, Keys.COMMAND).sendKeys("a").build().perform();
        } else if (CombinedKeyActionEnum.COPY.equals(combinedKeyAction)) {
            action.keyDown(element, Keys.COMMAND).sendKeys("a").sendKeys("c").build().perform(); //have a questions
        } else if (CombinedKeyActionEnum.CUT.equals(combinedKeyAction)) {
            action.keyDown(element, Keys.COMMAND).sendKeys("a").sendKeys("x").build().perform(); //have a questions
        } else if (CombinedKeyActionEnum.PASTE.equals(combinedKeyAction)) {
            action.keyDown(element, Keys.COMMAND).sendKeys("v").build().perform();
        }
    }
}
