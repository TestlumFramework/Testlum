package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.WebElementFinder;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.cott.testing.model.scenario.BackSpace;
import com.knubisoft.cott.testing.model.scenario.Copy;
import com.knubisoft.cott.testing.model.scenario.Cut;
import com.knubisoft.cott.testing.model.scenario.Enter;
import com.knubisoft.cott.testing.model.scenario.Escape;
import com.knubisoft.cott.testing.model.scenario.Highlight;
import com.knubisoft.cott.testing.model.scenario.HotKey;
import com.knubisoft.cott.testing.model.scenario.Paste;
import com.knubisoft.cott.testing.model.scenario.Space;
import com.knubisoft.cott.testing.model.scenario.Tab;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@ExecutorForClass(HotKey.class)
public class HotKeyExecutor extends AbstractUiExecutor<HotKey> {

    private final Map<HotKeyCommandPredicate, HotKeyCommand> hotKeyCommands;

    protected HotKeyExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        Map<HotKeyCommandPredicate, HotKeyCommand> commands = new HashMap<>();
        commands.put(hotKey -> hotKey instanceof Copy, (hotKey, action) -> copyCommand((Copy) hotKey, action));
        commands.put(hotKey -> hotKey instanceof Paste, (hotKey, action) -> pasteCommand((Paste) hotKey, action));
        commands.put(hotKey -> hotKey instanceof Cut, (hotKey, action) -> cutCommand((Cut) hotKey, action));
        commands.put(hotKey -> hotKey instanceof Highlight, (hotKey, action) ->
                highlightCommand((Highlight) hotKey, action));
        commands.put(hotKey -> hotKey instanceof Tab, (hotKey, action) -> tabCommand((Tab) hotKey, action));
        commands.put(hotKey -> hotKey instanceof Enter, (hotKey, action) -> enterCommand((Enter) hotKey, action));
        commands.put(hotKey -> hotKey instanceof BackSpace, (hotKey, action) ->
                backSpaceCommand((BackSpace) hotKey, action));
        commands.put(hotKey -> hotKey instanceof Escape, (hotKey, action) -> escapeCommand((Escape) hotKey, action));
        commands.put(hotKey -> hotKey instanceof Space, (hotKey, action) -> spaceCommand((Space) hotKey, action));
        hotKeyCommands = Collections.unmodifiableMap(commands);
    }

    @Override
    public void execute(final HotKey hotKey, final CommandResult result) {
        Actions action = new Actions(dependencies.getDriver());
        runHotKeyCommands(hotKey.getCopyOrPasteOrCut(), action);
    }

    private void runHotKeyCommands(final List<AbstractUiCommand> hotKeyCommandList, final Actions action) {
        hotKeyCommandList.forEach(command -> hotKeyCommands.keySet().stream()
                .filter(key -> key.test(command))
                .map(hotKeyCommands::get)
                .forEach(method -> executeHotKeyCommands(command, method, action)));
    }

    private void executeHotKeyCommands(final AbstractUiCommand command,
                                       final HotKeyExecutor.HotKeyCommand hotKeyCommand,
                                       final Actions action) {
        LogUtil.logHotKeyInfo(command);
        hotKeyCommand.accept(command, action);
    }

    private void escapeCommand(final Escape escape, final Actions action) {
        action.sendKeys(Keys.ESCAPE).perform();
    }

    private void backSpaceCommand(final BackSpace backSpace, final Actions action) {
        action.sendKeys(Keys.BACK_SPACE).perform();
    }

    private void spaceCommand(final Space space, final Actions action) {
        action.sendKeys(Keys.SPACE).perform();
    }

    private void enterCommand(final Enter enter, final Actions action) {
        action.sendKeys(Keys.ENTER).perform();
    }

    private void tabCommand(final Tab tab, final Actions action) {
        action.sendKeys(Keys.TAB).perform();
    }

    private void highlightCommand(final Highlight highlight, final Actions action) {
        Locator locator = GlobalLocators.getLocator(highlight.getLocatorId());
        WebElement element = WebElementFinder.find(locator, dependencies.getDriver());
        action.keyDown(element, Keys.COMMAND).sendKeys("a").build().perform();
    }

    private void cutCommand(final Cut cut, final Actions action) {
        Locator locator = GlobalLocators.getLocator(cut.getLocatorId());
        WebElement element = WebElementFinder.find(locator, dependencies.getDriver());
        action.keyDown(element, Keys.COMMAND).sendKeys("a").sendKeys("x").build().perform();
    }

    private void pasteCommand(final Paste paste, final Actions action) {
        Locator locator = GlobalLocators.getLocator(paste.getLocatorId());
        WebElement element = WebElementFinder.find(locator, dependencies.getDriver());
        action.keyDown(element, Keys.COMMAND).sendKeys("v").build().perform();
    }

    private void copyCommand(final Copy copy, final Actions action) {
        Locator locator = GlobalLocators.getLocator(copy.getLocatorId());
        WebElement element = WebElementFinder.find(locator, dependencies.getDriver());
        action.keyDown(element, Keys.COMMAND).sendKeys("a").sendKeys("c").build().perform();
    }

    private interface HotKeyCommandPredicate extends Predicate<AbstractUiCommand> { }
    private interface HotKeyCommand extends BiConsumer<AbstractUiCommand, Actions> { }
}
