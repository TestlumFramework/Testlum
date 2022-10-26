package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.HotKeyAction;
import com.knubisoft.cott.testing.model.scenario.HotKeyCommand;
import com.knubisoft.cott.testing.model.scenario.SingleKeyAction;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@UtilityClass
public class HotKeyCommandUtil {

    public void singleKeyCommand (final HotKeyCommand hotKeyCommand, final WebDriver driver) {
        Actions action = new Actions(driver);
        if (SingleKeyAction.TAB.equals(hotKeyCommand.getSingleKeyAction())) {
            action.sendKeys(Keys.TAB).perform();
        } else if (SingleKeyAction.ENTER.equals(hotKeyCommand.getSingleKeyAction())) {
            action.sendKeys(Keys.ENTER).perform();
        } else if (SingleKeyAction.ESCAPE.equals(hotKeyCommand.getSingleKeyAction())) {
            action.sendKeys(Keys.ESCAPE).perform();
        } else if (SingleKeyAction.DELETE.equals(hotKeyCommand.getSingleKeyAction())) {
            action.sendKeys(Keys.DELETE).perform();
        }
    }

    public void hotKeyCommand (final HotKeyCommand hotKeyCommand, final WebDriver driver) {
        if (HotKeyAction.HIGH_LIGHT_ALL.equals(hotKeyCommand.getHotKeyAction())) {
            highlightCommand(hotKeyCommand, driver);
        } else if (HotKeyAction.COPY.equals(hotKeyCommand.getHotKeyAction())) {
            copyCommand(hotKeyCommand, driver);
        } else if (HotKeyAction.CUT.equals(hotKeyCommand.getHotKeyAction())) {
            cutCommand(hotKeyCommand, driver);
        }
    }

    private void highlightCommand (final HotKeyCommand hotKeyCommand, final WebDriver driver) {
        Actions action = new Actions(driver);
        Locator locator = GlobalLocators.getLocator(hotKeyCommand.getLocator());
        WebElement element = WebElementFinder.find(locator, driver);
        action.keyDown(element, Keys.COMMAND).sendKeys("a").build().perform();
    }

    private void copyCommand (final HotKeyCommand hotKeyCommand, final WebDriver driver) {
        Actions action = new Actions(driver);
        Locator locator = GlobalLocators.getLocator(hotKeyCommand.getLocator());
        WebElement element = WebElementFinder.find(locator, driver);
        action.keyDown(element, Keys.COMMAND).sendKeys("c").build().perform();
    }

    private void cutCommand (final HotKeyCommand hotKeyCommand, final WebDriver driver) {
        Actions action = new Actions(driver);
        Locator locator = GlobalLocators.getLocator(hotKeyCommand.getLocator());
        WebElement element = WebElementFinder.find(locator, driver);
        action.keyDown(element, Keys.COMMAND).sendKeys("x").build().perform();
    }
}
