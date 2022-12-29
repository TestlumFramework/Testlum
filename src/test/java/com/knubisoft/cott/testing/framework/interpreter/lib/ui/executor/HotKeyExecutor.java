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
import com.knubisoft.cott.testing.model.scenario.Copy;
import com.knubisoft.cott.testing.model.scenario.Cut;
import com.knubisoft.cott.testing.model.scenario.Enter;
import com.knubisoft.cott.testing.model.scenario.Escape;
import com.knubisoft.cott.testing.model.scenario.Highlight;
import com.knubisoft.cott.testing.model.scenario.HotKey;
import com.knubisoft.cott.testing.model.scenario.Paste;
import com.knubisoft.cott.testing.model.scenario.Space;
import com.knubisoft.cott.testing.model.scenario.Tab;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.HOTKEY_COMMAND_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.HOTKEY_LOCATOR;

@Slf4j
@ExecutorForClass(HotKey.class)
public class HotKeyExecutor extends AbstractUiExecutor<HotKey> {

    private final Map<HotKeyCommandPredicate, HotKeyCommand> hotKeyCmdMethods;
    private final Actions action;
    private final Keys ctrlKey;

    public HotKeyExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        Map<HotKeyCommandPredicate, HotKeyCommand> commands = new HashMap<>();
        commands.put(key -> key instanceof Copy, (key, result) -> copyCommand((Copy) key, result));
        commands.put(key -> key instanceof Paste, (key, result) -> pasteCommand((Paste) key, result));
        commands.put(key -> key instanceof Cut, (key, result) -> cutCommand((Cut) key, result));
        commands.put(key -> key instanceof Highlight, (key, result) -> highlightCommand((Highlight) key, result));
        commands.put(key -> key instanceof Tab, (key, result) -> singleKeyCommand(Keys.TAB));
        commands.put(key -> key instanceof Enter, (key, result) -> singleKeyCommand(Keys.ENTER));
        commands.put(key -> key instanceof BackSpace, (key, result) -> singleKeyCommand(Keys.BACK_SPACE));
        commands.put(key -> key instanceof Escape, (key, result) -> singleKeyCommand(Keys.ESCAPE));
        commands.put(key -> key instanceof Space, (key, result) -> singleKeyCommand(Keys.SPACE));
        hotKeyCmdMethods = Collections.unmodifiableMap(commands);
        action = new Actions(dependencies.getDriver());
        ctrlKey = chooseKeyForOperatingSystem();
    }

    @Override
    public void execute(final HotKey hotKey, final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        hotKey.getCopyOrPasteOrCut().forEach(command -> hotKeyCmdMethods.keySet().stream()
                .filter(key -> key.test(command))
                .map(hotKeyCmdMethods::get)
                .forEach(method -> executeHotKeyCommand(command, method, subCommandsResult)));
    }

    private void executeHotKeyCommand(final AbstractUiCommand command,
                                      final HotKeyExecutor.HotKeyCommand hotKeyCmdMethod,
                                      final List<CommandResult> subCommandsResult) {
        CommandResult subCommandResult = ResultUtil.createCommandResultForUiSubCommand(
                dependencies.getPosition().intValue(),
                command.getClass().getSimpleName(),
                command.getComment());
        LogUtil.logHotKeyInfo(command);
        hotKeyCmdMethod.accept(command, subCommandResult);
        subCommandsResult.add(subCommandResult);
    }

    private void singleKeyCommand(final Keys key) {
        action.sendKeys(key).perform();
    }

    private void highlightCommand(final Highlight highlight, final CommandResult result) {
        action.keyDown(getWebElement(highlight.getLocatorId(), result), ctrlKey)
                .sendKeys("a").keyUp(ctrlKey).build().perform();
    }

    private void cutCommand(final Cut cut, final CommandResult result) {
        action.keyDown(getWebElement(cut.getLocatorId(), result), ctrlKey)
                .sendKeys("a").sendKeys("x").keyUp(ctrlKey).build().perform();
    }

    private void pasteCommand(final Paste paste, final CommandResult result) {
        action.keyDown(getWebElement(paste.getLocatorId(), result), ctrlKey)
                .sendKeys("v").keyUp(ctrlKey).build().perform();
    }

    private void copyCommand(final Copy copy, final CommandResult result) {
        action.keyDown(getWebElement(copy.getLocatorId(), result), ctrlKey)
                .sendKeys("a").sendKeys("c").keyUp(ctrlKey).build().perform();
    }


    private WebElement getWebElement(final String locator, final CommandResult result) {
        return StringUtils.isBlank(locator)
                ? getActiveElement()
                : getElementForHotKey(locator, result);
    }

    private WebElement getActiveElement() {
        return dependencies.getDriver().switchTo().activeElement();
    }

    private WebElement getElementForHotKey(final String locatorId, final CommandResult result) {
        Locator locator = GlobalLocators.getLocator(locatorId);
        result.put(HOTKEY_LOCATOR, locator.getLocatorId());
        log.info(HOTKEY_COMMAND_LOCATOR, locator.getLocatorId());
        return WebElementFinder.find(locator, dependencies.getDriver());
    }

    private Keys chooseKeyForOperatingSystem() {
        return SystemUtils.IS_OS_MAC_OSX ? Keys.COMMAND : Keys.CONTROL;
    }

    private interface HotKeyCommandPredicate extends Predicate<AbstractUiCommand> { }

    private interface HotKeyCommand extends BiConsumer<AbstractUiCommand, CommandResult> { }
}
