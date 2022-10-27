package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.ExceptionMessage;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@UtilityClass
public class HotKeyCommandUtil {

    private static final Map<SingleKeyActionValue, Keys> SINGLE_KEYS;

    static {
        final Map<SingleKeyActionValue, Keys> map = new HashMap<>();
        map.put(act -> SingleKeyAction.TAB.value().equals(act.value()), Keys.TAB);
        map.put(act -> SingleKeyAction.ENTER.value().equals(act.value()), Keys.ENTER);
        map.put(act -> SingleKeyAction.ESCAPE.value().equals(act.value()), Keys.ESCAPE);
        map.put(act -> SingleKeyAction.DELETE.value().equals(act.value()), Keys.BACK_SPACE);
        map.put(act -> SingleKeyAction.SPACE.value().equals(act.value()), Keys.SPACE);
        map.put(act -> SingleKeyAction.ARROW_LEFT.value().equals(act.value()), Keys.ARROW_LEFT);
        map.put(act -> SingleKeyAction.ARROW_RIGHT.value().equals(act.value()), Keys.ARROW_RIGHT);
        map.put(act -> SingleKeyAction.ARROW_DOWN.value().equals(act.value()), Keys.ARROW_DOWN);
        map.put(act -> SingleKeyAction.ARROW_UP.value().equals(act.value()), Keys.ARROW_UP);
        SINGLE_KEYS = Collections.unmodifiableMap(map);
    }

    private Keys findKeysByEnumValue (final SingleKeyAction singleKeyAction) {
        return SINGLE_KEYS.entrySet().stream()
                .filter(act -> act.getKey().test(singleKeyAction))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.KEY_NOT_SUPPORTED));
    }

    public void singleKeyCommand (final HotKeyCommand hotKeyCommand, final WebDriver driver) {
        Actions action = new Actions(driver);
        action.sendKeys(findKeysByEnumValue(hotKeyCommand.getSingleKeyAction())).perform();
    }

    public void hotKeyCommand (final HotKeyCommand hotKeyCommand, final WebDriver driver) {
        if (HotKeyAction.HIGH_LIGHT_ALL.equals(hotKeyCommand.getHotKeyAction())) {
            highlightCommand(hotKeyCommand, driver);
        } else if (HotKeyAction.COPY.equals(hotKeyCommand.getHotKeyAction())) {
            copyCommand(hotKeyCommand, driver);
        } else if (HotKeyAction.CUT.equals(hotKeyCommand.getHotKeyAction())) {
            cutCommand(hotKeyCommand, driver);
        } else if (HotKeyAction.PASTE.equals(hotKeyCommand.getHotKeyAction())) {
            pasteCommand(hotKeyCommand, driver);
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

    private void pasteCommand (final HotKeyCommand hotKeyCommand, final WebDriver driver) {
        Actions action = new Actions(driver);
        Locator locator = GlobalLocators.getLocator(hotKeyCommand.getLocator());
        WebElement element = WebElementFinder.find(locator, driver);
        action.keyDown(element, Keys.COMMAND).sendKeys("v").build().perform();
    }

    private interface SingleKeyActionValue extends Predicate<SingleKeyAction> { }

}
