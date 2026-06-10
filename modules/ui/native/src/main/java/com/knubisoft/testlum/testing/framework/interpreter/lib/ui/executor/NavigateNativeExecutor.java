package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.NavigateNative;
import com.knubisoft.testlum.testing.model.scenario.NavigateNativeDestination;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.ios.IOSDriver;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ExecutorForClass(NavigateNative.class)
@Slf4j
public class NavigateNativeExecutor extends AbstractUiExecutor<NavigateNative> {

    private final Map<NavigateNativeDestination, AndroidKey> navigateToKeyMap;

    public NavigateNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.navigateToKeyMap = createNavigationMap();
    }

    private static @NotNull Map<NavigateNativeDestination, AndroidKey> createNavigationMap() {
        Map<NavigateNativeDestination, AndroidKey> navigateMap = new HashMap<>();
        navigateMap.put(NavigateNativeDestination.HOME, AndroidKey.HOME);
        navigateMap.put(NavigateNativeDestination.BACK, AndroidKey.BACK);
        navigateMap.put(NavigateNativeDestination.OVERVIEW, AndroidKey.APP_SWITCH);
        return Collections.unmodifiableMap(navigateMap);
    }

    @Override
    public void execute(final NavigateNative navigateNative, final CommandResult result) {
        result.put(ResultUtil.NATIVE_NAVIGATE_TO, navigateNative.getDestination());
        log.info(LogMessage.NATIVE_NAVIGATION_LOG, navigateNative.getDestination());
        if (dependencies.getDriver() instanceof AndroidDriver androidDriver) {
            performAndroidNavigation(navigateNative, androidDriver);
        }
        if (dependencies.getDriver() instanceof IOSDriver iosDriver) {
            performIOSNavigation(navigateNative, iosDriver);
        }
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performIOSNavigation(final NavigateNative navigateNative, final IOSDriver driver) {
        switch (navigateNative.getDestination()) {
            case HOME -> driver.executeScript("mobile: pressButton", Collections.singletonMap("name", "home"));
            case BACK -> driver.navigate().back();
            case OVERVIEW -> throw new DefaultFrameworkException("Overview unfortunately is not supported in IOS");
            default -> throw new DefaultFrameworkException(ExceptionMessage.NAVIGATE_NOT_SUPPORTED,
                    navigateNative.getDestination().value());
        }
    }

    private void performAndroidNavigation(final NavigateNative navigateNative,
                                          final AndroidDriver driver) {
        driver.pressKey(new KeyEvent(
                navigateToKeyMap.get(navigateNative.getDestination())));
    }
}
