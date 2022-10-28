package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.HotKeyAction;
import com.knubisoft.cott.testing.model.scenario.HotKeyCommand;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@UtilityClass
public class HotKeyCommandUtil {

    public void hotKeyCommand (final HotKeyCommand hotKeyCommand, final WebDriver driver) {
        Actions action = new Actions(driver);
        Locator locator = GlobalLocators.getLocator(hotKeyCommand.getLocator());
        WebElement element = WebElementFinder.find(locator, driver);
        if (HotKeyAction.HIGH_LIGHT_ALL.equals(hotKeyCommand.getHotKeyAction())) {
            action.keyDown(element, Keys.COMMAND).sendKeys("a").build().perform();
        } else if (HotKeyAction.COPY.equals(hotKeyCommand.getHotKeyAction())) {
            action.keyDown(element, Keys.COMMAND).sendKeys("a").sendKeys("c").build().perform(); //have a questions
        } else if (HotKeyAction.CUT.equals(hotKeyCommand.getHotKeyAction())) {
            action.keyDown(element, Keys.COMMAND).sendKeys("a").sendKeys("x").build().perform(); //have a questions
        } else if (HotKeyAction.PASTE.equals(hotKeyCommand.getHotKeyAction())) {
            action.keyDown(element, Keys.COMMAND).sendKeys("v").build().perform();
        }
    }
}
