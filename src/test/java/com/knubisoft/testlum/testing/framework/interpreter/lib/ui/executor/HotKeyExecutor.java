package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.BackSpace;
import com.knubisoft.testlum.testing.model.scenario.Copy;
import com.knubisoft.testlum.testing.model.scenario.Cut;
import com.knubisoft.testlum.testing.model.scenario.Enter;
import com.knubisoft.testlum.testing.model.scenario.Escape;
import com.knubisoft.testlum.testing.model.scenario.Highlight;
import com.knubisoft.testlum.testing.model.scenario.HotKey;
import com.knubisoft.testlum.testing.model.scenario.Paste;
import com.knubisoft.testlum.testing.model.scenario.Space;
import com.knubisoft.testlum.testing.model.scenario.Tab;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
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

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.HOT_KEY_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.HOTKEY_COMMAND_LOCATOR;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.HOTKEY_COMMAND_REPEAT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.HOTKEY_LOCATOR;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.HOTKEY_REPEAT;

@Slf4j
@ExecutorForClass(HotKey.class)
public class HotKeyExecutor extends AbstractUiExecutor<HotKey> {

    private final Map<HotKeyCommandPredicate, HotKeyCommandMethod> hotKeyCmdMethods;
    private final Actions action;
    private final Keys ctrlKey;

    public HotKeyExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        Map<HotKeyCommandPredicate, HotKeyCommandMethod> commands = new HashMap<>();
        commands.put(key -> key instanceof Cut, (key, result) -> cutCommand());
        commands.put(key -> key instanceof Copy, (key, result) -> copyCommand());
        commands.put(key -> key instanceof Paste, (key, result) -> pasteCommand((Paste) key, result));
        commands.put(key -> key instanceof Highlight, (key, result) -> highlightCommand((Highlight) key, result));
        commands.put(key -> key instanceof Tab,
                (key, result) -> singleKeyCommand(Keys.TAB, ((Tab) key).getRepeat(), result));
        commands.put(key -> key instanceof Enter,
                (key, result) -> singleKeyCommand(Keys.ENTER, ((Enter) key).getRepeat(), result));
        commands.put(key -> key instanceof BackSpace,
                (key, result) -> singleKeyCommand(Keys.BACK_SPACE, ((BackSpace) key).getRepeat(), result));
        commands.put(key -> key instanceof Escape,
                (key, result) -> singleKeyCommand(Keys.ESCAPE, ((Escape) key).getRepeat(), result));
        commands.put(key -> key instanceof Space,
                (key, result) -> singleKeyCommand(Keys.SPACE, ((Space) key).getRepeat(), result));
        hotKeyCmdMethods = Collections.unmodifiableMap(commands);
        action = new Actions(dependencies.getDriver());
        ctrlKey = chooseKeyForOperatingSystem();
    }

    @Override
    public void execute(final HotKey hotKey, final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        hotKey.getCopyOrPasteOrCut().forEach(command -> {
            CommandResult commandResult = ResultUtil.newUiCommandResultInstance(
                    dependencies.getPosition().get(), command);
            subCommandsResult.add(commandResult);
            LogUtil.logHotKeyInfo(command);
            executeHotKeyCommand(command, commandResult);
        });
    }

    private void executeHotKeyCommand(final AbstractUiCommand command, final CommandResult result) {
        hotKeyCmdMethods.entrySet().stream()
                .filter(commandMethod -> commandMethod.getKey().test(command))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(HOT_KEY_NOT_SUPPORTED,
                        command.getClass().getSimpleName()))
                .getValue().accept(command, result);
    }

    private void singleKeyCommand(final Keys key, final int repeat, final CommandResult result) {
        for (int step = 0; step < repeat; step++) {
            action.sendKeys(key).perform();
        }
        if (repeat > 1) {
            result.put(HOTKEY_REPEAT, repeat);
            log.info(HOTKEY_COMMAND_REPEAT, repeat);
        }
    }

    private void highlightCommand(final Highlight highlight, final CommandResult result) {
        action.keyDown(getWebElement(highlight.getLocatorId(), result), ctrlKey)
                .sendKeys("a").keyUp(ctrlKey).build().perform();
    }

    private void pasteCommand(final Paste paste, final CommandResult result) {
        action.keyDown(getWebElement(paste.getLocatorId(), result), ctrlKey)
                .sendKeys("v").keyUp(ctrlKey).build().perform();
    }

    private void cutCommand() {
        action.keyDown(ctrlKey).sendKeys("x").keyUp(ctrlKey).build().perform();
    }

    private void copyCommand() {
        action.keyDown(ctrlKey).sendKeys("c").keyUp(ctrlKey).build().perform();
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
        result.put(HOTKEY_LOCATOR, locatorId);
        log.info(HOTKEY_COMMAND_LOCATOR, locatorId);
        return UiUtil.findWebElement(dependencies, locatorId);
    }

    private Keys chooseKeyForOperatingSystem() {
        return SystemUtils.IS_OS_MAC_OSX ? Keys.COMMAND : Keys.CONTROL;
    }

    private interface HotKeyCommandPredicate extends Predicate<AbstractUiCommand> { }
    private interface HotKeyCommandMethod extends BiConsumer<AbstractUiCommand, CommandResult> { }
}
