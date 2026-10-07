package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.config.BrowserConfig;
import com.testlum.reporting.sdk.model.config.BrowserTypeConfig;
import com.testlum.reporting.sdk.model.config.BrowserTypeKind;
import com.testlum.reporting.sdk.model.config.ConnectionConfig;
import com.testlum.reporting.sdk.model.config.ConnectionKind;
import com.testlum.reporting.sdk.model.config.DeviceConfig;
import com.testlum.reporting.sdk.model.config.MobileBrowserConfig;
import com.testlum.reporting.sdk.model.config.NativeConfig;
import com.testlum.reporting.sdk.model.config.ScreenRecordingConfig;
import com.testlum.reporting.sdk.model.config.WebConfig;
import com.testlum.testing.model.global_config.AbstractBrowser;
import com.testlum.testing.model.global_config.AppiumCapabilities;
import com.testlum.testing.model.global_config.AppiumNativeCapabilities;
import com.testlum.testing.model.global_config.AppiumServer;
import com.testlum.testing.model.global_config.AutoHealing;
import com.testlum.testing.model.global_config.AutoHealingMode;
import com.testlum.testing.model.global_config.BrowserInDocker;
import com.testlum.testing.model.global_config.BrowserOptionsArguments;
import com.testlum.testing.model.global_config.BrowserSettings;
import com.testlum.testing.model.global_config.BrowserStackCapabilities;
import com.testlum.testing.model.global_config.BrowserStackNativeCapabilities;
import com.testlum.testing.model.global_config.BrowserStackServer;
import com.testlum.testing.model.global_config.BrowserStackWeb;
import com.testlum.testing.model.global_config.BrowserType;
import com.testlum.testing.model.global_config.Browsers;
import com.testlum.testing.model.global_config.Capabilities;
import com.testlum.testing.model.global_config.Capability;
import com.testlum.testing.model.global_config.Chrome;
import com.testlum.testing.model.global_config.ConnectionType;
import com.testlum.testing.model.global_config.Edge;
import com.testlum.testing.model.global_config.ElementAutowait;
import com.testlum.testing.model.global_config.Firefox;
import com.testlum.testing.model.global_config.GooglePlayLogin;
import com.testlum.testing.model.global_config.LocalBrowser;
import com.testlum.testing.model.global_config.Mobilebrowser;
import com.testlum.testing.model.global_config.MobilebrowserDevice;
import com.testlum.testing.model.global_config.MobilebrowserDevices;
import com.testlum.testing.model.global_config.Native;
import com.testlum.testing.model.global_config.NativeDevice;
import com.testlum.testing.model.global_config.NativeDevices;
import com.testlum.testing.model.global_config.Platform;
import com.testlum.testing.model.global_config.RemoteBrowser;
import com.testlum.testing.model.global_config.Safari;
import com.testlum.testing.model.global_config.ScreenRecording;
import com.testlum.testing.model.global_config.TakeScreenshot;
import com.testlum.testing.model.global_config.UiConfig;
import com.testlum.testing.model.global_config.Web;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link UiConfigMapper} verifying the typed mapping of web browsers, mobile-browser and native
 * connections and devices.
 */
class UiConfigMapperTest {

    private final UiConfigMapper mapper = new UiConfigMapper();

    private static Web web(final AbstractBrowser... browsers) {
        Browsers list = new Browsers();
        list.getChromeOrFirefoxOrSafari().addAll(List.of(browsers));
        BrowserSettings settings = new BrowserSettings();
        settings.setTakeScreenshots(takeScreenshots(true));
        settings.setElementAutowait(autowait(5));
        settings.setBrowsers(list);
        AutoHealing autoHealing = new AutoHealing();
        autoHealing.setEnabled(true);
        autoHealing.setMode(AutoHealingMode.PERSISTENT);
        Web web = new Web();
        web.setEnabled(true);
        web.setBaseUrl("https://app.test");
        web.setAutoHealing(autoHealing);
        web.setBrowserSettings(settings);
        return web;
    }

    private static TakeScreenshot takeScreenshots(final boolean enabled) {
        TakeScreenshot takeScreenshot = new TakeScreenshot();
        takeScreenshot.setEnabled(enabled);
        return takeScreenshot;
    }

    private static ElementAutowait autowait(final int seconds) {
        ElementAutowait autowait = new ElementAutowait();
        autowait.setSeconds(seconds);
        return autowait;
    }

    private static <B extends AbstractBrowser> B browser(final B browser, final String alias, final BrowserType type) {
        browser.setAlias(alias);
        browser.setEnabled(true);
        browser.setMaximizedBrowserWindow(true);
        browser.setBrowserType(type);
        return browser;
    }

    private static BrowserOptionsArguments arguments(final String... values) {
        BrowserOptionsArguments arguments = new BrowserOptionsArguments();
        arguments.getArgument().addAll(List.of(values));
        return arguments;
    }

    private static Capabilities capabilities(final String name, final String value) {
        Capability capability = new Capability();
        capability.setName(name);
        capability.setValue(value);
        Capabilities capabilities = new Capabilities();
        capabilities.getCapability().add(capability);
        return capabilities;
    }

    private static BrowserType localType() {
        LocalBrowser local = new LocalBrowser();
        local.setDriverVersion("120.0");
        BrowserType type = new BrowserType();
        type.setLocalBrowser(local);
        return type;
    }

    private static BrowserType dockerType() {
        ScreenRecording recording = new ScreenRecording();
        recording.setEnabled(true);
        recording.setOutputFolder("/videos");
        BrowserInDocker docker = new BrowserInDocker();
        docker.setBrowserVersion("121.0");
        docker.setEnableVNC(true);
        docker.setDockerNetwork("grid");
        docker.setScreenRecording(recording);
        BrowserType type = new BrowserType();
        type.setBrowserInDocker(docker);
        return type;
    }

    private static BrowserType remoteType() {
        RemoteBrowser remote = new RemoteBrowser();
        remote.setBrowserVersion("122.0");
        remote.setRemoteBrowserURL("http://grid:4444");
        BrowserType type = new BrowserType();
        type.setRemoteBrowser(remote);
        return type;
    }

    private static BrowserType browserStackType() {
        BrowserStackWeb browserStack = new BrowserStackWeb();
        browserStack.setBrowserVersion("latest");
        browserStack.setOs("Windows");
        browserStack.setOsVersion("11");
        BrowserType type = new BrowserType();
        type.setBrowserStack(browserStack);
        return type;
    }

    private static ConnectionType appiumConnection() {
        AppiumServer server = new AppiumServer();
        server.setServerUrl("http://appium:4723");
        ConnectionType connection = new ConnectionType();
        connection.setAppiumServer(server);
        return connection;
    }

    private static ConnectionType browserStackConnection() {
        ConnectionType connection = new ConnectionType();
        connection.setBrowserStack(new BrowserStackServer());
        return connection;
    }

    private static MobilebrowserDevice appiumMobileDevice() {
        AppiumCapabilities capabilities = new AppiumCapabilities();
        capabilities.setDeviceName("Pixel 7");
        capabilities.setPlatformVersion("14");
        capabilities.setUdid("emulator-5554");
        MobilebrowserDevice device = new MobilebrowserDevice();
        device.setAlias("pixel");
        device.setEnabled(true);
        device.setPlatformName(Platform.ANDROID);
        device.setCapabilities(capabilities("autoGrantPermissions", "true"));
        device.setAppiumCapabilities(capabilities);
        return device;
    }

    private static MobilebrowserDevice browserStackMobileDevice() {
        BrowserStackCapabilities capabilities = new BrowserStackCapabilities();
        capabilities.setDeviceName("iPhone 15");
        capabilities.setPlatformVersion("17");
        MobilebrowserDevice device = new MobilebrowserDevice();
        device.setAlias("iphone");
        device.setPlatformName(Platform.IOS);
        device.setBrowserStackCapabilities(capabilities);
        return device;
    }

    private static NativeDevice appiumNativeDevice() {
        AppiumNativeCapabilities capabilities = new AppiumNativeCapabilities();
        capabilities.setDeviceName("Pixel 7");
        capabilities.setPlatformVersion("14");
        capabilities.setUdid("emulator-5554");
        capabilities.setAppPackage("com.example");
        capabilities.setAppActivity(".MainActivity");
        NativeDevice device = new NativeDevice();
        device.setAlias("pixel");
        device.setEnabled(true);
        device.setPlatformName(Platform.ANDROID);
        device.setAppiumCapabilities(capabilities);
        return device;
    }

    private static NativeDevice browserStackNativeDevice() {
        GooglePlayLogin login = new GooglePlayLogin();
        login.setEmail("qa@example.com");
        login.setPassword("secret");
        BrowserStackNativeCapabilities capabilities = new BrowserStackNativeCapabilities();
        capabilities.setDeviceName("Galaxy S23");
        capabilities.setPlatformVersion("13");
        capabilities.setApp("bs://app-id");
        capabilities.setGooglePlayLogin(login);
        NativeDevice device = new NativeDevice();
        device.setAlias("galaxy");
        device.setPlatformName(Platform.ANDROID);
        device.setBrowserStackCapabilities(capabilities);
        return device;
    }

    private WebConfig mapWeb(final Web web) {
        UiConfig ui = new UiConfig();
        ui.setWeb(web);
        return mapper.map(ui).getWeb();
    }

    @Test
    void mapsNullUiToNull() {
        assertNull(mapper.map(null));
    }

    @Test
    void mapsWebSettingsAndAutoHealing() {
        WebConfig web = mapWeb(web());

        assertTrue(web.isEnabled());
        assertEquals("https://app.test", web.getBaseUrl());
        assertTrue(web.getAutoHealing().isEnabled());
        assertEquals(com.testlum.reporting.sdk.model.config.AutoHealingMode.PERSISTENT,
                web.getAutoHealing().getMode());
        assertTrue(web.getSettings().isTakeScreenshots());
        assertEquals(5, web.getSettings().getElementAutowaitSeconds());
        assertTrue(web.getBrowsers().isEmpty());
    }

    @Test
    void splitsBrowsersByKindWithTheirArguments() {
        Chrome chrome = browser(new Chrome(), "chrome", localType());
        chrome.setHeadlessMode(true);
        chrome.setChromeOptionsArguments(arguments("--disable-gpu", "--no-sandbox"));
        chrome.setCapabilities(capabilities("acceptInsecureCerts", "true"));
        Firefox firefox = browser(new Firefox(), "firefox", localType());
        firefox.setFirefoxOptionsArguments(arguments("-private"));
        Edge edge = browser(new Edge(), "edge", localType());
        edge.setHeadlessMode(true);
        edge.setKeepDownloadedFiles(true);
        Safari safari = browser(new Safari(), "safari", localType());

        List<BrowserConfig> browsers = mapWeb(web(chrome, firefox, safari, edge)).getBrowsers();

        assertEquals(List.of(com.testlum.reporting.sdk.model.config.BrowserType.CHROME,
                        com.testlum.reporting.sdk.model.config.BrowserType.FIREFOX,
                        com.testlum.reporting.sdk.model.config.BrowserType.SAFARI,
                        com.testlum.reporting.sdk.model.config.BrowserType.EDGE),
                browsers.stream().map(BrowserConfig::getKind).toList());
        BrowserConfig chromeConfig = browsers.get(0);
        assertEquals("chrome", chromeConfig.getAlias());
        assertTrue(chromeConfig.isEnabled());
        assertTrue(chromeConfig.isMaximizedBrowserWindow());
        assertTrue(chromeConfig.getHeadlessMode());
        assertEquals(List.of("--disable-gpu", "--no-sandbox"), chromeConfig.getArguments());
        assertEquals("acceptInsecureCerts", chromeConfig.getCapabilities().get(0).getName());
        assertEquals("true", chromeConfig.getCapabilities().get(0).getValue());
        assertFalse(browsers.get(1).getHeadlessMode());
        assertEquals(List.of("-private"), browsers.get(1).getArguments());
        assertFalse(browsers.get(1).isKeepDownloadedFiles());
        assertNull(browsers.get(2).getHeadlessMode());
        assertNull(browsers.get(2).getArguments());
        assertTrue(browsers.get(3).getHeadlessMode());
        assertTrue(browsers.get(3).getArguments().isEmpty());
        assertTrue(browsers.get(3).isKeepDownloadedFiles());
    }

    @Test
    void mapsEachBrowserTypeKind() {
        List<BrowserConfig> browsers = mapWeb(web(
                browser(new Chrome(), "local", localType()),
                browser(new Chrome(), "docker", dockerType()),
                browser(new Chrome(), "remote", remoteType()),
                browser(new Chrome(), "browserstack", browserStackType()))).getBrowsers();

        BrowserTypeConfig local = browsers.get(0).getType();
        assertEquals(BrowserTypeKind.LOCAL, local.getKind());
        assertEquals("120.0", local.getDriverVersion());
        BrowserTypeConfig docker = browsers.get(1).getType();
        assertEquals(BrowserTypeKind.DOCKER, docker.getKind());
        assertEquals("121.0", docker.getBrowserVersion());
        assertTrue(docker.getEnableVnc());
        assertEquals("grid", docker.getDockerNetwork());
        assertEquals(ScreenRecordingConfig.builder().enabled(true).outputFolder("/videos").build(),
                docker.getScreenRecording());
        BrowserTypeConfig remote = browsers.get(2).getType();
        assertEquals(BrowserTypeKind.REMOTE, remote.getKind());
        assertEquals("122.0", remote.getBrowserVersion());
        assertEquals("http://grid:4444", remote.getRemoteBrowserUrl());
        BrowserTypeConfig browserStack = browsers.get(3).getType();
        assertEquals(BrowserTypeKind.BROWSER_STACK, browserStack.getKind());
        assertEquals("latest", browserStack.getBrowserVersion());
        assertEquals("Windows", browserStack.getOs());
        assertEquals("11", browserStack.getOsVersion());
    }

    @Test
    void mapsMobilebrowserConnectionAndDevicesByCapabilitiesKind() {
        MobilebrowserDevices devices = new MobilebrowserDevices();
        devices.getDevice().addAll(List.of(appiumMobileDevice(), browserStackMobileDevice()));
        Mobilebrowser mobilebrowser = new Mobilebrowser();
        mobilebrowser.setEnabled(true);
        mobilebrowser.setBaseUrl("https://m.app.test");
        mobilebrowser.setTakeScreenshots(takeScreenshots(false));
        mobilebrowser.setElementAutowait(autowait(3));
        mobilebrowser.setConnection(appiumConnection());
        mobilebrowser.setDevices(devices);
        UiConfig ui = new UiConfig();
        ui.setMobilebrowser(mobilebrowser);

        MobileBrowserConfig config = mapper.map(ui).getMobilebrowser();

        assertTrue(config.isEnabled());
        assertEquals("https://m.app.test", config.getBaseUrl());
        assertFalse(config.getSettings().isTakeScreenshots());
        assertEquals(3, config.getSettings().getElementAutowaitSeconds());
        assertEquals(ConnectionConfig.builder().kind(ConnectionKind.APPIUM_SERVER).serverUrl("http://appium:4723")
                .build(), config.getConnection());
        DeviceConfig appium = config.getDevices().get(0);
        assertEquals("pixel", appium.getAlias());
        assertTrue(appium.isEnabled());
        assertEquals(com.testlum.reporting.sdk.model.config.Platform.ANDROID, appium.getPlatform());
        assertEquals(ConnectionKind.APPIUM_SERVER, appium.getCapabilitiesKind());
        assertEquals("Pixel 7", appium.getDeviceName());
        assertEquals("14", appium.getPlatformVersion());
        assertEquals("emulator-5554", appium.getUdid());
        assertEquals("autoGrantPermissions", appium.getCapabilities().get(0).getName());
        DeviceConfig browserStack = config.getDevices().get(1);
        assertEquals(com.testlum.reporting.sdk.model.config.Platform.IOS, browserStack.getPlatform());
        assertEquals(ConnectionKind.BROWSER_STACK, browserStack.getCapabilitiesKind());
        assertEquals("iPhone 15", browserStack.getDeviceName());
        assertNull(browserStack.getUdid());
        assertTrue(browserStack.getCapabilities().isEmpty());
    }

    @Test
    void mapsNativeConnectionAndAppFieldsPerCapabilitiesKind() {
        NativeDevices devices = new NativeDevices();
        devices.getDevice().addAll(List.of(appiumNativeDevice(), browserStackNativeDevice()));
        Native nativeConfig = new Native();
        nativeConfig.setEnabled(true);
        nativeConfig.setConnection(browserStackConnection());
        nativeConfig.setDevices(devices);
        UiConfig ui = new UiConfig();
        ui.setNative(nativeConfig);

        NativeConfig config = mapper.map(ui).getNativeConfig();

        assertTrue(config.isEnabled());
        assertFalse(config.getSettings().isTakeScreenshots());
        assertNull(config.getSettings().getElementAutowaitSeconds());
        assertEquals(ConnectionConfig.builder().kind(ConnectionKind.BROWSER_STACK).build(), config.getConnection());
        DeviceConfig appium = config.getDevices().get(0);
        assertEquals(ConnectionKind.APPIUM_SERVER, appium.getCapabilitiesKind());
        assertEquals("emulator-5554", appium.getUdid());
        assertEquals("com.example", appium.getAppPackage());
        assertEquals(".MainActivity", appium.getAppActivity());
        assertNull(appium.getApp());
        assertNull(appium.getGooglePlayLogin());
        DeviceConfig browserStack = config.getDevices().get(1);
        assertEquals(ConnectionKind.BROWSER_STACK, browserStack.getCapabilitiesKind());
        assertEquals("Galaxy S23", browserStack.getDeviceName());
        assertEquals("bs://app-id", browserStack.getApp());
        assertEquals("qa@example.com", browserStack.getGooglePlayLogin().getEmail());
        assertEquals("secret", browserStack.getGooglePlayLogin().getPassword());
        assertNull(browserStack.getAppPackage());
    }
}
