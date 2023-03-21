package com.knubisoft.cott.testing.framework.configuration.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.env.EnvManager;
import com.knubisoft.cott.testing.model.global_config.DesktopType;
import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.options.WindowsOptions;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

@UtilityClass
public class DesktopDriverFactory {

    @SneakyThrows
    public WebDriver createDriver(final DesktopType desktopType) {
        DesiredCapabilities options = new DesiredCapabilities();
//        options.setNewCommandTimeout(Duration.ofSeconds(25));
        options.setCapability("appium:app", "Microsoft.WindowsCalculator_8wekyb3d8bbwe!App");
//        options.setCapability("app", "Microsoft.WindowsCalculator_8wekyb3d8bbwe!App");
        options.setCapability("ms:experimental-webdriver", true);
        options.setCapability("winAppDriver:experimental-w3c", true);
//        options.setApp("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
//        MutableCapabilities options = new DesiredCapabilities();
//        options.setCapability("appTopLevelWindow", "2f0c10");
//        options.setCapability("platformVersion", "10");
        options.setCapability("appium:deviceName", "WindowsPC");
//        options.setCapability("deviceName", "WindowsPC");
        options.setCapability("platformName", "windows");
        String serverUrl = GlobalTestConfigurationProvider.getUiConfigs().get(EnvManager.currentEnv()).getDesktop()
                .getConnection().getAppiumServer().getServerUrl();
        return new WindowsDriver(new URL(serverUrl), options);
    }
}
