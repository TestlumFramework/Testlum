package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UiConfigValidatorTest {

    private UiConfigValidator validator;
    private FileSearcher fileSearcher;
    private TestResourceSettings settings;

    @BeforeEach
    void setUp() {
        fileSearcher = mock(FileSearcher.class);
        settings = mock(TestResourceSettings.class);
        when(settings.getEnvConfigFolder()).thenReturn(new File("/test"));
        when(settings.getTestResourcesFolder()).thenReturn(new File("/test"));
        when(fileSearcher.searchFileFromEnvFolder(anyString(), anyString()))
                .thenReturn(Optional.of(new File("/test/ui.xml")));
        validator = new UiConfigValidator(settings, fileSearcher);
    }

    @Nested
    class ValidateEmptyConfig {
        @Test
        void emptyMapDoesNotThrow() {
            assertDoesNotThrow(() -> validator.validate(Map.of()));
        }

        @Test
        void emptyUiConfigsDoNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", new UiConfig());
            map.put("staging", new UiConfig());
            assertDoesNotThrow(() -> validator.validate(map));
        }
    }

    @Nested
    class ValidateWebPresence {
        @Test
        void webInAllEnvsDoesNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithWeb("http://dev.example.com"));
            map.put("staging", createUiConfigWithWeb("http://staging.example.com"));
            assertDoesNotThrow(() -> validator.validate(map));
        }

        @Test
        void webInOneEnvOnlyThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithWeb("http://dev.example.com"));
            map.put("staging", new UiConfig());
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }
    }

    @Nested
    class ValidateBaseUrls {
        @Test
        void sameBaseUrlsAcrossEnvsThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithWeb("http://same.example.com"));
            map.put("staging", createUiConfigWithWeb("http://same.example.com"));
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void differentBaseUrlsDoNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithWeb("http://dev.example.com"));
            map.put("staging", createUiConfigWithWeb("http://staging.example.com"));
            assertDoesNotThrow(() -> validator.validate(map));
        }
    }

    @Nested
    class ValidateConnection {
        @Test
        void matchingAppiumConnectionsDoNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithNativeAppium("http://appium1:4723", "dev-device"));
            map.put("staging", createUiConfigWithNativeAppium("http://appium2:4723", "dev-device"));
            assertDoesNotThrow(() -> validator.validate(map));
        }

        @Test
        void sameAppiumServerUrlsThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithNativeAppium("http://appium:4723", "dev-device"));
            map.put("staging", createUiConfigWithNativeAppium("http://appium:4723", "dev-device"));
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void mixedConnectionTypesThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithNativeAppium("http://appium:4723", "dev-device"));
            map.put("staging", createUiConfigWithNativeBrowserStack("dev-device"));
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void browserStackWithoutLoginThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            UiConfig config1 = createUiConfigWithNativeBrowserStack("device1");
            UiConfig config2 = createUiConfigWithNativeBrowserStack("device1");
            config1.setBrowserStackLogin(new BrowserStackLogin());
            map.put("dev", config1);
            map.put("staging", config2);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void browserStackWithLoginInAllEnvsDoesNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            UiConfig config1 = createUiConfigWithNativeBrowserStack("device1");
            UiConfig config2 = createUiConfigWithNativeBrowserStack("device1");
            config1.setBrowserStackLogin(new BrowserStackLogin());
            config2.setBrowserStackLogin(new BrowserStackLogin());
            map.put("dev", config1);
            map.put("staging", config2);
            assertDoesNotThrow(() -> validator.validate(map));
        }
    }

    @Nested
    class ValidateDeviceAliases {
        @Test
        void matchingDeviceAliasesDoNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithNativeAppium("http://appium1:4723", "myDevice"));
            map.put("staging", createUiConfigWithNativeAppium("http://appium2:4723", "myDevice"));
            assertDoesNotThrow(() -> validator.validate(map));
        }

        @Test
        void mismatchedDeviceAliasesThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithNativeAppium("http://appium1:4723", "device1"));
            map.put("staging", createUiConfigWithNativeAppium("http://appium2:4723", "device2"));
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }
    }

    @Nested
    class ValidateBrowserAliases {
        @Test
        void matchingBrowserAliasesDoNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithWebAndBrowser("http://dev.example.com", "chrome1"));
            map.put("staging", createUiConfigWithWebAndBrowser("http://staging.example.com", "chrome1"));
            assertDoesNotThrow(() -> validator.validate(map));
        }

        @Test
        void duplicateBrowserAliasInSameEnvThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithDuplicateBrowsers("http://dev.example.com"));
            assertThrows(Exception.class,
                    () -> validator.validate(map));
        }
    }

    @Nested
    class ValidateSingleEnv {
        @Test
        void singleEnvWithWebDoesNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithWeb("http://dev.example.com"));
            assertDoesNotThrow(() -> validator.validate(map));
        }

        @Test
        void singleEnvWithNativeAppiumDoesNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithNativeAppium("http://appium:4723", "device1"));
            assertDoesNotThrow(() -> validator.validate(map));
        }
    }

    @Nested
    class ValidateMobilebrowser {
        @Test
        void mobilebrowserInAllEnvsDoesNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithMobilebrowserAppium(
                    "http://dev.example.com", "http://appium1:4723", "device1"));
            map.put("staging", createUiConfigWithMobilebrowserAppium(
                    "http://staging.example.com", "http://appium2:4723", "device1"));
            assertDoesNotThrow(() -> validator.validate(map));
        }

        @Test
        void mobilebrowserInOneEnvOnlyThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithMobilebrowserAppium(
                    "http://dev.example.com", "http://appium1:4723", "device1"));
            map.put("staging", new UiConfig());
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void sameBaseUrlsForMobilebrowserThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithMobilebrowserAppium(
                    "http://same.example.com", "http://appium1:4723", "device1"));
            map.put("staging", createUiConfigWithMobilebrowserAppium(
                    "http://same.example.com", "http://appium2:4723", "device1"));
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void mobilebrowserBrowserStackWithLoginDoesNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            UiConfig config1 = createUiConfigWithMobilebrowserBrowserStack(
                    "http://dev.example.com", "device1");
            UiConfig config2 = createUiConfigWithMobilebrowserBrowserStack(
                    "http://staging.example.com", "device1");
            config1.setBrowserStackLogin(new BrowserStackLogin());
            config2.setBrowserStackLogin(new BrowserStackLogin());
            map.put("dev", config1);
            map.put("staging", config2);
            assertDoesNotThrow(() -> validator.validate(map));
        }

        @Test
        void mobilebrowserAppiumMissingCapabilitiesThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            UiConfig config = createUiConfigWithMobilebrowserAppiumNoCapabilities(
                    "http://dev.example.com", "http://appium1:4723", "device1");
            map.put("dev", config);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }
    }

    @Nested
    class ValidatePlatformName {
        @Test
        void mismatchedPlatformNamesThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithNativeAppiumAndPlatform(
                    "http://appium1:4723", "device1", Platform.ANDROID));
            map.put("staging", createUiConfigWithNativeAppiumAndPlatform(
                    "http://appium2:4723", "device1", Platform.IOS));
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void matchingPlatformNamesDoNotThrow() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithNativeAppiumAndPlatform(
                    "http://appium1:4723", "device1", Platform.ANDROID));
            map.put("staging", createUiConfigWithNativeAppiumAndPlatform(
                    "http://appium2:4723", "device1", Platform.ANDROID));
            assertDoesNotThrow(() -> validator.validate(map));
        }
    }

    @Nested
    class ValidateNativeCapabilities {
        @Test
        void nativeAppiumMissingCapabilitiesThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithNativeAppiumNoCapabilities(
                    "http://appium1:4723", "device1"));
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void nativeBrowserStackMissingCapabilitiesThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            UiConfig config = createUiConfigWithNativeBrowserStackNoCapabilities("device1");
            config.setBrowserStackLogin(new BrowserStackLogin());
            map.put("dev", config);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void nativeDuplicateDeviceAliasInSameEnvThrows() {
            Map<String, UiConfig> map = new LinkedHashMap<>();
            map.put("dev", createUiConfigWithDuplicateNativeDevices(
                    "http://appium1:4723"));
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }
    }

    private UiConfig createUiConfigWithWeb(final String baseUrl) {
        Web web = new Web();
        web.setEnabled(true);
        web.setBaseUrl(baseUrl);
        BrowserSettings browserSettings = new BrowserSettings();
        Browsers browsers = new Browsers();
        browserSettings.setBrowsers(browsers);
        web.setBrowserSettings(browserSettings);

        UiConfig config = new UiConfig();
        config.setWeb(web);
        return config;
    }

    private UiConfig createUiConfigWithWebAndBrowser(final String baseUrl, final String alias) {
        Web web = new Web();
        web.setEnabled(true);
        web.setBaseUrl(baseUrl);
        BrowserSettings browserSettings = getBrowserSettings(alias);
        web.setBrowserSettings(browserSettings);
        UiConfig config = new UiConfig();
        config.setWeb(web);
        return config;
    }

    private static @NotNull BrowserSettings getBrowserSettings(final String alias) {
        Browsers browsers = new Browsers();
        Chrome chrome = new Chrome();
        chrome.setAlias(alias);
        chrome.setEnabled(true);
        browsers.getChromeOrFirefoxOrSafari().add(chrome);

        BrowserSettings browserSettings = new BrowserSettings();
        browserSettings.setBrowsers(browsers);
        return browserSettings;
    }

    private UiConfig createUiConfigWithDuplicateBrowsers(final String baseUrl) {
        Web web = new Web();
        web.setEnabled(true);
        web.setBaseUrl(baseUrl);
        Chrome chrome1 = new Chrome();
        chrome1.setAlias("dup");
        chrome1.setEnabled(true);
        Chrome chrome2 = new Chrome();
        chrome2.setAlias("dup");
        chrome2.setEnabled(true);
        Browsers browsers = new Browsers();
        browsers.getChromeOrFirefoxOrSafari().add(chrome1);
        browsers.getChromeOrFirefoxOrSafari().add(chrome2);
        BrowserSettings browserSettings = new BrowserSettings();
        browserSettings.setBrowsers(browsers);
        web.setBrowserSettings(browserSettings);

        UiConfig config = new UiConfig();
        config.setWeb(web);
        return config;
    }

    private UiConfig createUiConfigWithNativeAppium(final String serverUrl, final String alias) {
        AppiumServer appiumServer = new AppiumServer();
        appiumServer.setServerUrl(serverUrl);

        ConnectionType connectionType = new ConnectionType();
        connectionType.setAppiumServer(appiumServer);

        NativeDevice device = new NativeDevice();
        device.setAlias(alias);
        device.setEnabled(true);
        device.setPlatformName(Platform.ANDROID);
        AppiumNativeCapabilities appiumCaps = new AppiumNativeCapabilities();
        device.setAppiumCapabilities(appiumCaps);

        NativeDevices devices = new NativeDevices();
        devices.getDevice().add(device);

        Native nativeConfig = new Native();
        nativeConfig.setEnabled(true);
        nativeConfig.setConnection(connectionType);
        nativeConfig.setDevices(devices);

        UiConfig config = new UiConfig();
        config.setNative(nativeConfig);
        return config;
    }

    private UiConfig createUiConfigWithNativeBrowserStack(final String alias) {
        BrowserStackServer bsServer = new BrowserStackServer();

        ConnectionType connectionType = new ConnectionType();
        connectionType.setBrowserStack(bsServer);

        NativeDevice device = new NativeDevice();
        device.setAlias(alias);
        device.setEnabled(true);
        device.setPlatformName(Platform.ANDROID);
        BrowserStackNativeCapabilities bsCaps = new BrowserStackNativeCapabilities();
        device.setBrowserStackCapabilities(bsCaps);

        NativeDevices devices = new NativeDevices();
        devices.getDevice().add(device);

        Native nativeConfig = new Native();
        nativeConfig.setEnabled(true);
        nativeConfig.setConnection(connectionType);
        nativeConfig.setDevices(devices);

        UiConfig config = new UiConfig();
        config.setNative(nativeConfig);
        return config;
    }

    private UiConfig createUiConfigWithNativeAppiumAndPlatform(final String serverUrl,
                                                               final String alias,
                                                               final Platform platform) {
        AppiumServer appiumServer = new AppiumServer();
        appiumServer.setServerUrl(serverUrl);

        ConnectionType connectionType = new ConnectionType();
        connectionType.setAppiumServer(appiumServer);

        NativeDevice device = new NativeDevice();
        device.setAlias(alias);
        device.setEnabled(true);
        device.setPlatformName(platform);
        device.setAppiumCapabilities(new AppiumNativeCapabilities());

        NativeDevices devices = new NativeDevices();
        devices.getDevice().add(device);

        Native nativeConfig = new Native();
        nativeConfig.setEnabled(true);
        nativeConfig.setConnection(connectionType);
        nativeConfig.setDevices(devices);

        UiConfig config = new UiConfig();
        config.setNative(nativeConfig);
        return config;
    }

    private UiConfig createUiConfigWithNativeAppiumNoCapabilities(final String serverUrl, final String alias) {
        AppiumServer appiumServer = new AppiumServer();
        appiumServer.setServerUrl(serverUrl);

        ConnectionType connectionType = new ConnectionType();
        connectionType.setAppiumServer(appiumServer);

        NativeDevice device = new NativeDevice();
        device.setAlias(alias);
        device.setEnabled(true);
        device.setPlatformName(Platform.ANDROID);

        NativeDevices devices = new NativeDevices();
        devices.getDevice().add(device);

        Native nativeConfig = new Native();
        nativeConfig.setEnabled(true);
        nativeConfig.setConnection(connectionType);
        nativeConfig.setDevices(devices);

        UiConfig config = new UiConfig();
        config.setNative(nativeConfig);
        return config;
    }

    private UiConfig createUiConfigWithNativeBrowserStackNoCapabilities(final String alias) {
        BrowserStackServer bsServer = new BrowserStackServer();

        ConnectionType connectionType = new ConnectionType();
        connectionType.setBrowserStack(bsServer);

        NativeDevice device = new NativeDevice();
        device.setAlias(alias);
        device.setEnabled(true);
        device.setPlatformName(Platform.ANDROID);

        NativeDevices devices = new NativeDevices();
        devices.getDevice().add(device);

        Native nativeConfig = new Native();
        nativeConfig.setEnabled(true);
        nativeConfig.setConnection(connectionType);
        nativeConfig.setDevices(devices);

        UiConfig config = new UiConfig();
        config.setNative(nativeConfig);
        return config;
    }

    private UiConfig createUiConfigWithDuplicateNativeDevices(final String serverUrl) {
        AppiumServer appiumServer = new AppiumServer();
        appiumServer.setServerUrl(serverUrl);

        ConnectionType connectionType = new ConnectionType();
        connectionType.setAppiumServer(appiumServer);

        NativeDevice device1 = new NativeDevice();
        device1.setAlias("dup");
        device1.setEnabled(true);
        device1.setPlatformName(Platform.ANDROID);
        device1.setAppiumCapabilities(new AppiumNativeCapabilities());

        NativeDevice device2 = new NativeDevice();
        device2.setAlias("dup");
        device2.setEnabled(true);
        device2.setPlatformName(Platform.ANDROID);
        device2.setAppiumCapabilities(new AppiumNativeCapabilities());

        NativeDevices devices = new NativeDevices();
        devices.getDevice().add(device1);
        devices.getDevice().add(device2);

        Native nativeConfig = new Native();
        nativeConfig.setEnabled(true);
        nativeConfig.setConnection(connectionType);
        nativeConfig.setDevices(devices);

        UiConfig config = new UiConfig();
        config.setNative(nativeConfig);
        return config;
    }

    private UiConfig createUiConfigWithMobilebrowserAppium(final String baseUrl,
                                                           final String serverUrl,
                                                           final String alias) {
        AppiumServer appiumServer = new AppiumServer();
        appiumServer.setServerUrl(serverUrl);

        ConnectionType connectionType = new ConnectionType();
        connectionType.setAppiumServer(appiumServer);

        MobilebrowserDevice device = new MobilebrowserDevice();
        device.setAlias(alias);
        device.setEnabled(true);
        device.setPlatformName(Platform.ANDROID);
        device.setAppiumCapabilities(new AppiumCapabilities());

        MobilebrowserDevices devices = new MobilebrowserDevices();
        devices.getDevice().add(device);

        Mobilebrowser mobilebrowser = new Mobilebrowser();
        mobilebrowser.setEnabled(true);
        mobilebrowser.setBaseUrl(baseUrl);
        mobilebrowser.setConnection(connectionType);
        mobilebrowser.setDevices(devices);

        UiConfig config = new UiConfig();
        config.setMobilebrowser(mobilebrowser);
        return config;
    }

    private UiConfig createUiConfigWithMobilebrowserAppiumNoCapabilities(final String baseUrl,
                                                                         final String serverUrl,
                                                                         final String alias) {
        AppiumServer appiumServer = new AppiumServer();
        appiumServer.setServerUrl(serverUrl);

        ConnectionType connectionType = new ConnectionType();
        connectionType.setAppiumServer(appiumServer);

        MobilebrowserDevice device = new MobilebrowserDevice();
        device.setAlias(alias);
        device.setEnabled(true);
        device.setPlatformName(Platform.ANDROID);

        MobilebrowserDevices devices = new MobilebrowserDevices();
        devices.getDevice().add(device);

        Mobilebrowser mobilebrowser = new Mobilebrowser();
        mobilebrowser.setEnabled(true);
        mobilebrowser.setBaseUrl(baseUrl);
        mobilebrowser.setConnection(connectionType);
        mobilebrowser.setDevices(devices);

        UiConfig config = new UiConfig();
        config.setMobilebrowser(mobilebrowser);
        return config;
    }

    private UiConfig createUiConfigWithMobilebrowserBrowserStack(final String baseUrl, final String alias) {
        BrowserStackServer bsServer = new BrowserStackServer();

        ConnectionType connectionType = new ConnectionType();
        connectionType.setBrowserStack(bsServer);

        MobilebrowserDevice device = new MobilebrowserDevice();
        device.setAlias(alias);
        device.setEnabled(true);
        device.setPlatformName(Platform.ANDROID);
        device.setBrowserStackCapabilities(new BrowserStackCapabilities());

        MobilebrowserDevices devices = new MobilebrowserDevices();
        devices.getDevice().add(device);

        Mobilebrowser mobilebrowser = new Mobilebrowser();
        mobilebrowser.setEnabled(true);
        mobilebrowser.setBaseUrl(baseUrl);
        mobilebrowser.setConnection(connectionType);
        mobilebrowser.setDevices(devices);

        UiConfig config = new UiConfig();
        config.setMobilebrowser(mobilebrowser);
        return config;
    }
}
