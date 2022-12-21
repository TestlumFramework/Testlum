package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.NavigateNative;
import com.knubisoft.cott.testing.model.scenario.NavigateNativeDestination;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.ios.IOSDriver;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.NATIVE_NAVIGATION_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NATIVE_NAVIGATE_TO;

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
            IOSDriver driver = (IOSDriver) dependencies.getDriver();
            //TODO performIosNavigation
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performAndroidNavigation(final NavigateNative navigateNative,
                                          final AndroidDriver driver) {
        driver.pressKey(new KeyEvent(
                navigateToKeyMap.get(navigateNative.getDestination())));
    }
}
