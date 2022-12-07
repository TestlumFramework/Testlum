package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
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
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NAVIGATE_DESTINATION_UNSUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.NATIVE_NAVIGATION_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NATIVE_MOVE_TO;

@ExecutorForClass(NavigateNative.class)
@Slf4j
public class NavigateNativeExecutor extends AbstractUiExecutor<NavigateNative> {

    public NavigateNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final NavigateNative navigateNative, final CommandResult result) {
        result.put(NATIVE_MOVE_TO, navigateNative.getDestination());
        if (dependencies.getDriver() instanceof AndroidDriver) {
            performAndroidNavigation(navigateNative);
        }
        if (dependencies.getDriver() instanceof IOSDriver) {
            IOSDriver driver = (IOSDriver) dependencies.getDriver();
            //TODO performIosNavigation
        }
        log.info(NATIVE_NAVIGATION_LOG, navigateNative.getDestination());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performAndroidNavigation(final NavigateNative navigateNative) {
        AndroidDriver driver = (AndroidDriver) dependencies.getDriver();
        androidNavigateMap(driver).getOrDefault(navigateNative.getDestination(), destination -> {
            throw new DefaultFrameworkException(NAVIGATE_DESTINATION_UNSUPPORTED + destination);
        }).accept(new Object());
    }

    private Map<NavigateNativeDestination, Consumer<Object>> androidNavigateMap(
            final AndroidDriver driver) {
        Map<NavigateNativeDestination, Consumer<Object>> destinationToFunc = new HashMap<>();
        destinationToFunc.put(NavigateNativeDestination.HOME,
                home -> driver.pressKey(new KeyEvent(AndroidKey.HOME)));
        destinationToFunc.put(NavigateNativeDestination.BACK,
                back -> driver.pressKey(new KeyEvent(AndroidKey.BACK)));
        destinationToFunc.put(NavigateNativeDestination.OVERVIEW,
                overview -> driver.pressKey(new KeyEvent(AndroidKey.APP_SWITCH)));
        return destinationToFunc;
    }
}
