package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.BackSpace;
import com.knubisoft.cott.testing.model.scenario.Copy;
import com.knubisoft.cott.testing.model.scenario.Cut;
import com.knubisoft.cott.testing.model.scenario.Enter;
import com.knubisoft.cott.testing.model.scenario.Escape;
import com.knubisoft.cott.testing.model.scenario.Highlight;
import com.knubisoft.cott.testing.model.scenario.Paste;
import com.knubisoft.cott.testing.model.scenario.Space;
import com.knubisoft.cott.testing.model.scenario.Tab;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@UtilityClass
public class HotKeyUtil {

    private final Map<HotKeyCommandPredicate, HotKeyCommand> hotKeyCommands;

    static  {
        Map<HotKeyCommandPredicate, HotKeyCommand> commands = new HashMap<>();
        commands.put(hotKey -> hotKey instanceof Copy, (hotKey, driver) -> copyCommand((Copy) hotKey, driver));
        commands.put(hotKey -> hotKey instanceof Paste, (hotKey, driver) -> pasteCommand((Paste) hotKey, driver));
        commands.put(hotKey -> hotKey instanceof Cut, (hotKey, driver) -> cutCommand((Cut) hotKey, driver));
        commands.put(hotKey -> hotKey instanceof Highlight, (hotKey, driver) -> highlightCommand((Highlight) hotKey, driver));
        commands.put(hotKey -> hotKey instanceof Tab, (hotKey, driver) -> tabCommand((Tab) hotKey, driver));
        commands.put(hotKey -> hotKey instanceof Enter, (hotKey, driver) -> enterCommand((Enter) hotKey, driver));
        commands.put(hotKey -> hotKey instanceof BackSpace, (hotKey, driver) -> backSpaceCommand((BackSpace) hotKey, driver));
        commands.put(hotKey -> hotKey instanceof Escape, (hotKey, driver) -> escapeCommand((Escape) hotKey, driver));
        commands.put(hotKey -> hotKey instanceof Space, (hotKey, driver) -> spaceCommand((Space) hotKey, driver));
        hotKeyCommands = Collections.unmodifiableMap(commands);
    }

    public void runHotKeyCommands(final List<AbstractCommand> hotKeyCommandList, final WebDriver driver) {
        hotKeyCommandList.forEach(command -> hotKeyCommands.keySet().stream()
                .filter(key -> key.test(command))
                .map(hotKeyCommands::get)
                .forEach(method -> executeHotKeyCommands(command, method, driver)));
    }

    private void executeHotKeyCommands(final AbstractCommand command,
                                      final HotKeyUtil.HotKeyCommand hotKeyCommand,
                                      final WebDriver driver) {
        hotKeyCommand.accept(command, driver);
    }

    private static void escapeCommand(Escape escape, WebDriver driver) {
        Actions action = new Actions(driver);
        action.sendKeys(Keys.ESCAPE).perform();
    }

    private static void backSpaceCommand(BackSpace backSpace, WebDriver driver) {
        Actions action = new Actions(driver);
        action.sendKeys(Keys.BACK_SPACE).perform();
    }

    private static void spaceCommand(Space space, WebDriver driver) {
        Actions action = new Actions(driver);
        action.sendKeys(Keys.SPACE).perform();
    }

    private static void enterCommand(Enter enter, WebDriver driver) {
        Actions action = new Actions(driver);
        action.sendKeys(Keys.ENTER).perform();
    }

    private static void tabCommand(Tab tab, WebDriver driver) {
        Actions action = new Actions(driver);
        action.sendKeys(Keys.TAB).perform();
    }

    private static void highlightCommand(final Highlight highlight, final WebDriver driver) {
        Actions action = new Actions(driver);
        Locator locator = GlobalLocators.getLocator(highlight.getLocatorId());
        WebElement element = WebElementFinder.find(locator, driver);
        action.keyDown(element, Keys.COMMAND).sendKeys("a").build().perform();
    }

    private static void cutCommand(final Cut cut, final WebDriver driver) {
        Actions action = new Actions(driver);
        Locator locator = GlobalLocators.getLocator(cut.getLocatorId());
        WebElement element = WebElementFinder.find(locator, driver);
        action.keyDown(element, Keys.COMMAND).sendKeys("a").sendKeys("x").build().perform();
    }

    private static void pasteCommand(final Paste paste, final WebDriver driver) {
        Actions action = new Actions(driver);
        Locator locator = GlobalLocators.getLocator(paste.getLocatorId());
        WebElement element = WebElementFinder.find(locator, driver);
        action.keyDown(element, Keys.COMMAND).sendKeys("v").build().perform();
    }

    private static void copyCommand(final Copy copy, final WebDriver driver) {
        Actions action = new Actions(driver);
        Locator locator = GlobalLocators.getLocator(copy.getLocatorId());
        WebElement element = WebElementFinder.find(locator, driver);
        action.keyDown(element, Keys.COMMAND).sendKeys("a").sendKeys("c").build().perform();
    }

    private interface HotKeyCommandPredicate extends Predicate<AbstractCommand> { }
    private interface HotKeyCommand extends BiConsumer<AbstractCommand, WebDriver> { }
}
