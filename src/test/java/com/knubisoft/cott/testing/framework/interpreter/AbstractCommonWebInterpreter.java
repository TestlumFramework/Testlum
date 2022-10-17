package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.JavascriptUtil;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.CloseSecondTab;
import com.knubisoft.cott.testing.model.scenario.CommonWeb;
import com.knubisoft.cott.testing.model.scenario.Hovers;
import com.knubisoft.cott.testing.model.scenario.Javascript;
import com.knubisoft.cott.testing.model.scenario.Navigate;
import com.knubisoft.cott.testing.model.scenario.NavigateCommand;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NAVIGATE_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SECOND_TAB_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.BY_URL_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.COMMAND_TYPE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.JS_FILE_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLOSE_COMMAND;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.JS_FILE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NAVIGATE_TYPE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NAVIGATE_URL;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.SECOND_TAB;
import static java.lang.String.format;

@Slf4j
public abstract class AbstractCommonWebInterpreter<T extends CommonWeb> extends AbstractUiInterpreter<T> {

    private static final Pattern HTTP_PATTERN = Pattern.compile("https?://.+");
    private static final String MOVE_TO_EMPTY_SPACE = "//html";

    public AbstractCommonWebInterpreter(InterpreterDependencies dependencies) {
        super(dependencies);
    }

    private void execJsCommands(final Javascript o, final CommandResult result) {
        String fileName = o.getFile();
        result.put(JS_FILE, fileName);
        log.info(JS_FILE_LOG, fileName);
        String command = JavascriptUtil.readCommands(fileName);
        JavascriptUtil.executeJsScript(command, driver);
    }

    private void hover(final Hovers hovers, final CommandResult result) {
        ResultUtil.addHoversMetaData(hovers, result);
        Actions actions = new Actions(driver);
        List<WebElement> webElements = hovers.getHover().stream()
                .peek(hover -> LogUtil.logHover(dependencies.getPosition().incrementAndGet(), hover))
                .map(hover -> UiUtil.findWebElement(driver, hover.getLocatorId()))
                .collect(Collectors.toList());
        webElements.forEach(webElement -> {
            actions.moveToElement(webElement);
            actions.perform();
        });
        moveToEmptySpace(hovers.isMoveToEmptySpace(), actions, driver);
    }

    private void moveToEmptySpace(final Boolean isMoveToEmptySpace, final Actions actions, final WebDriver webDriver) {
        if (Objects.nonNull(isMoveToEmptySpace) && isMoveToEmptySpace) {
            WebElement element = webDriver.findElement(By.xpath(MOVE_TO_EMPTY_SPACE));
            actions.moveToElement(element);
            actions.perform();
        }
    }

    private void closeSecondTab(final CloseSecondTab closeSecondTab, final CommandResult result) {
        result.put(CLOSE_COMMAND, SECOND_TAB);
        List<String> windowHandles = new ArrayList<>(driver.getWindowHandles());
        if (windowHandles.size() <= 1) {
            throw new DefaultFrameworkException(SECOND_TAB_NOT_FOUND);
        }
        driver.switchTo().window(windowHandles.get(1));
        driver.close();
        driver.switchTo().window(windowHandles.get(0));
    }

    private void navigate(final Navigate navigate, final CommandResult result) {
        NavigateCommand navigateCommand = navigate.getCommand();
        log.info(COMMAND_TYPE_LOG, navigateCommand.name());
        result.put(NAVIGATE_TYPE, navigateCommand.value());
        switch (navigateCommand) {
            case BACK: driver.navigate().back();
                break;
            case RELOAD: driver.navigate().refresh();
                break;
            case TO: navigateTo(navigate.getPath(), result);
                break;
            default: throw new DefaultFrameworkException(format(NAVIGATE_NOT_SUPPORTED, navigateCommand.value()));
        }
        takeScreenshotAndSaveIfRequired(result, settings, driver);
    }

    private void navigateTo(final String path, final CommandResult result) {
        String url = inject(getUrl(path));
        result.put(NAVIGATE_URL, url);
        log.info(BY_URL_LOG, url);
        driver.navigate().to(url);
    }

    private String getUrl(final String path) {
        if (HTTP_PATTERN.matcher(path).matches()) {
            return path;
        }
        return dependencies.getGlobalTestConfiguration().getWeb().getBaseUrl() + path;
    }
}
