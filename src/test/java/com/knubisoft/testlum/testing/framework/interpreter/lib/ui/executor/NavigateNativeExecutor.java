package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;

import com.knubisoft.testlum.testing.model.scenario.NavigateNative;
import com.knubisoft.testlum.testing.model.scenario.NavigateNativeDestination;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.ios.IOSDriver;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NAVIGATE_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.NATIVE_NAVIGATION_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.NATIVE_NAVIGATE_TO;

@ExecutorForClass(NavigateNative.class)
@Slf4j
public class NavigateNativeExecutor extends AbstractUiExecutor<NavigateNative> {

    private final Map<NavigateNativeDestination, AndroidKey> navigateToKeyMap;

    public NavigateNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        Map<NavigateNativeDestination, AndroidKey> navigateMap = new HashMap<>();
        navigateMap.put(NavigateNativeDestination.HOME, AndroidKey.HOME);
        navigateMap.put(NavigateNativeDestination.BACK, AndroidKey.BACK);
        navigateMap.put(NavigateNativeDestination.OVERVIEW, AndroidKey.APP_SWITCH);
        this.navigateToKeyMap = Collections.unmodifiableMap(navigateMap);
    }

    @Override
    public void execute(final NavigateNative navigateNative, final CommandResult result) {
        result.put(NATIVE_NAVIGATE_TO, navigateNative.getDestination());
        log.info(NATIVE_NAVIGATION_LOG, navigateNative.getDestination());
        if (dependencies.getDriver() instanceof AndroidDriver) {
            performAndroidNavigation(navigateNative, (AndroidDriver) dependencies.getDriver());
        }
        if (dependencies.getDriver() instanceof IOSDriver) {
            performIOSNavigation(navigateNative, (IOSDriver) dependencies.getDriver());
        }
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performIOSNavigation(final NavigateNative navigateNative, final IOSDriver driver) {
        switch (navigateNative.getDestination()) {
            case HOME:
                driver.executeScript("mobile: pressButton", Collections.singletonMap("name", "home"));
                break;
            case BACK:
                driver.navigate().back();
                break;
            case OVERVIEW:
                throw new DefaultFrameworkException("Overview unfortunately is not supported in IOS");
            default:
                throw new DefaultFrameworkException(NAVIGATE_NOT_SUPPORTED,
                        navigateNative.getDestination().value());
        }
    }

    private void performAndroidNavigation(final NavigateNative navigateNative,
                                          final AndroidDriver driver) {
        driver.pressKey(new KeyEvent(
                navigateToKeyMap.get(navigateNative.getDestination())));
    }
}
