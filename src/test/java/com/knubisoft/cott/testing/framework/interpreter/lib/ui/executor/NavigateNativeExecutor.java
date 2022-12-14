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
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.NATIVE_NAVIGATION_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NATIVE_MOVE_TO;

@ExecutorForClass(NavigateNative.class)
@Slf4j
public class NavigateNativeExecutor extends AbstractUiExecutor<NavigateNative> {

    private final Map<NavigateNativeDestination, KeyEvent> navigateMap;

    public NavigateNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        navigateMap = new HashMap<>();
        navigateMap.put(NavigateNativeDestination.HOME, new KeyEvent(AndroidKey.HOME));
        navigateMap.put(NavigateNativeDestination.BACK, new KeyEvent(AndroidKey.BACK));
        navigateMap.put(NavigateNativeDestination.OVERVIEW, new KeyEvent(AndroidKey.APP_SWITCH));
    }

    @Override
    public void execute(final NavigateNative navigateNative, final CommandResult result) {
        result.put(NATIVE_MOVE_TO, navigateNative.getDestination());
        log.info(NATIVE_NAVIGATION_LOG, navigateNative.getDestination());
        if (dependencies.getDriver() instanceof AndroidDriver) {
            performAndroidNavigation(navigateNative);
        }
        if (dependencies.getDriver() instanceof IOSDriver) {
            IOSDriver driver = (IOSDriver) dependencies.getDriver();
            //TODO performIosNavigation
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performAndroidNavigation(final NavigateNative navigateNative) {
        AndroidDriver driver = (AndroidDriver) dependencies.getDriver();
        driver.pressKey(navigateMap.get(navigateNative.getDestination()));
    }
}
