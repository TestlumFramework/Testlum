package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.WebElementFinder;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.cott.testing.model.scenario.BackSpace;
import com.knubisoft.cott.testing.model.scenario.CommandWithLocator;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.knubisoft.cott.testing.framework.util.ResultUtil.HOTKEY_LOCATOR;

@ExecutorForClass(HotKey.class)
public class HotKeyExecutor extends AbstractUiExecutor<HotKey> {

    private final Map<HotKeyCommandPredicate, HotKeyCommand> hotKeyCmdMethods;

    private final Actions action = new Actions(dependencies.getDriver());

    public HotKeyExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        Map<HotKeyCommandPredicate, HotKeyCommand> commands = new HashMap<>();
        commands.put(hotKey -> hotKey instanceof Copy, (hotKey, result) -> copyCommand((Copy) hotKey, result));
        commands.put(hotKey -> hotKey instanceof Paste, (hotKey, result) -> pasteCommand((Paste) hotKey, result));
        commands.put(hotKey -> hotKey instanceof Cut, (hotKey, result) -> cutCommand((Cut) hotKey, result));
        commands.put(hotKey -> hotKey instanceof Highlight, (hotKey, result) ->
                highlightCommand((Highlight) hotKey, result));
        commands.put(hotKey -> hotKey instanceof Tab, (hotKey, result) -> singleKeyCommand(Keys.TAB));
        commands.put(hotKey -> hotKey instanceof Enter, (hotKey, result) -> singleKeyCommand(Keys.ENTER));
        commands.put(hotKey -> hotKey instanceof BackSpace, (hotKey, result) -> singleKeyCommand(Keys.BACK_SPACE));
        commands.put(hotKey -> hotKey instanceof Escape, (hotKey, result) -> singleKeyCommand(Keys.ESCAPE));
        commands.put(hotKey -> hotKey instanceof Space, (hotKey, result) -> singleKeyCommand(Keys.SPACE));
        hotKeyCmdMethods = Collections.unmodifiableMap(commands);
    }

    @Override
    public void execute(final HotKey hotKey, final CommandResult result) {
        runHotKeyCommand(hotKey.getCopyOrPasteOrCut(), result);
    }

    private void runHotKeyCommand(final List<AbstractUiCommand> hotKeyCommandList,
                                   final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        hotKeyCommandList.forEach(command -> hotKeyCmdMethods.keySet().stream()
                .filter(key -> key.test(command))
                .map(hotKeyCmdMethods::get)
                .forEach(method -> executeHotKeyCommands(command, method, subCommandsResult)));
    }

    private void executeHotKeyCommands(final AbstractUiCommand command,
                                       final HotKeyExecutor.HotKeyCommand hotKeyCmdMethods,
                                       final List<CommandResult> subCommandsResult) {
        CommandResult subCommandResult = ResultUtil.createCommandResultForUiSubCommand(
                dependencies.getPosition().intValue(),
                command.getClass().getSimpleName(),
                command.getComment());
        LogUtil.logHotKeyInfo(command);
        hotKeyCmdMethods.accept(command, subCommandResult);
        subCommandsResult.add(subCommandResult);
    }

    private void singleKeyCommand(final Keys key) {
        action.sendKeys(key).perform();
    }

    private void highlightCommand(final Highlight highlight, final CommandResult result) {
        action.keyDown(getElementForHotKey(highlight, result), Keys.COMMAND).sendKeys("a").build().perform();
    }

    private void cutCommand(final Cut cut, final CommandResult result) {
        action.keyDown(getElementForHotKey(cut, result), Keys.COMMAND).sendKeys("a").sendKeys("x").build().perform();
    }

    private void pasteCommand(final Paste paste, final CommandResult result) {
        action.keyDown(getElementForHotKey(paste, result), Keys.COMMAND).sendKeys("v").build().perform();
    }

    private void copyCommand(final Copy copy, final CommandResult result) {
        action.keyDown(getElementForHotKey(copy, result), Keys.COMMAND).sendKeys("a").sendKeys("c").build().perform();
    }

    private WebElement getElementForHotKey(final CommandWithLocator command, final CommandResult result) {
        Locator locator = GlobalLocators.getLocator(command.getLocatorId());
        result.put(HOTKEY_LOCATOR, locator.getLocatorId());
        return WebElementFinder.find(locator, dependencies.getDriver());
    }

    private interface HotKeyCommandPredicate extends Predicate<AbstractUiCommand> { }
    private interface HotKeyCommand extends BiConsumer<AbstractUiCommand, CommandResult> { }
}
