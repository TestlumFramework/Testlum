package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.config.AutoHealingConfig;
import com.testlum.reporting.sdk.model.config.AutoHealingMode;
import com.testlum.reporting.sdk.model.config.BrowserConfig;
import com.testlum.reporting.sdk.model.config.BrowserKind;
import com.testlum.reporting.sdk.model.config.BrowserTypeConfig;
import com.testlum.reporting.sdk.model.config.BrowserTypeKind;
import com.testlum.reporting.sdk.model.config.Capability;
import com.testlum.reporting.sdk.model.config.ConnectionConfig;
import com.testlum.reporting.sdk.model.config.ConnectionKind;
import com.testlum.reporting.sdk.model.config.DeviceConfig;
import com.testlum.reporting.sdk.model.config.MobileBrowserConfig;
import com.testlum.reporting.sdk.model.config.NativeConfig;
import com.testlum.reporting.sdk.model.config.Platform;
import com.testlum.reporting.sdk.model.config.ScreenRecordingConfig;
import com.testlum.reporting.sdk.model.config.UiSettings;
import com.testlum.reporting.sdk.model.config.WebConfig;
import com.testlum.testing.model.global_config.AbstractBrowser;
import com.testlum.testing.model.global_config.AbstractCapabilities;
import com.testlum.testing.model.global_config.AbstractDevice;
import com.testlum.testing.model.global_config.AppiumCapabilities;
import com.testlum.testing.model.global_config.AppiumNativeCapabilities;
import com.testlum.testing.model.global_config.AutoHealing;
import com.testlum.testing.model.global_config.BrowserInDocker;
import com.testlum.testing.model.global_config.BrowserOptionsArguments;
import com.testlum.testing.model.global_config.BrowserSettings;
import com.testlum.testing.model.global_config.BrowserStackLogin;
import com.testlum.testing.model.global_config.BrowserStackNativeCapabilities;
import com.testlum.testing.model.global_config.BrowserStackWeb;
import com.testlum.testing.model.global_config.BrowserType;
import com.testlum.testing.model.global_config.Capabilities;
import com.testlum.testing.model.global_config.Chrome;
import com.testlum.testing.model.global_config.ConnectionType;
import com.testlum.testing.model.global_config.Edge;
import com.testlum.testing.model.global_config.Firefox;
import com.testlum.testing.model.global_config.LocalBrowser;
import com.testlum.testing.model.global_config.Mobilebrowser;
import com.testlum.testing.model.global_config.MobilebrowserDevice;
import com.testlum.testing.model.global_config.MobilebrowserDevices;
import com.testlum.testing.model.global_config.Native;
import com.testlum.testing.model.global_config.NativeDevice;
import com.testlum.testing.model.global_config.NativeDevices;
import com.testlum.testing.model.global_config.RemoteBrowser;
import com.testlum.testing.model.global_config.ScreenRecording;
import com.testlum.testing.model.global_config.Settings;
import com.testlum.testing.model.global_config.UiConfig;
import com.testlum.testing.model.global_config.Web;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps an environment's JAXB {@code ui.xml} config onto the typed reporting-SDK UI config: web browsers with their
 * type (local / docker / remote / BrowserStack), and mobile-browser / native connections and devices.
 */
@Component
public class UiConfigMapper {

    public com.testlum.reporting.sdk.model.config.UiConfig map(final UiConfig ui) {
        if (ui == null) {
            return null;
        }
        return com.testlum.reporting.sdk.model.config.UiConfig.builder()
                .web(web(ui.getWeb()))
                .mobilebrowser(mobilebrowser(ui.getMobilebrowser()))
                .nativeConfig(nativeConfig(ui.getNative()))
                .browserStackLogin(browserStackLogin(ui.getBrowserStackLogin()))
                .build();
    }

    private WebConfig web(final Web web) {
        if (web == null) {
            return null;
        }
        return WebConfig.builder()
                .enabled(web.isEnabled())
                .baseUrl(web.getBaseUrl())
                .autoHealing(autoHealing(web.getAutoHealing()))
                .settings(settings(web.getBrowserSettings()))
                .browsers(browsers(web.getBrowserSettings()))
                .build();
    }

    private AutoHealingConfig autoHealing(final AutoHealing autoHealing) {
        if (autoHealing == null) {
            return null;
        }
        return AutoHealingConfig.builder()
                .enabled(autoHealing.isEnabled())
                .mode(autoHealing.getMode() == null ? null : AutoHealingMode.valueOf(autoHealing.getMode().name()))
                .build();
    }

    private UiSettings settings(final Settings settings) {
        if (settings == null) {
            return null;
        }
        return UiSettings.builder()
                .takeScreenshots(settings.getTakeScreenshots() != null && settings.getTakeScreenshots().isEnabled())
                .elementAutowaitSeconds(settings.getElementAutowait() == null
                        ? null : settings.getElementAutowait().getSeconds())
                .build();
    }

    private List<BrowserConfig> browsers(final BrowserSettings settings) {
        if (settings == null || settings.getBrowsers() == null) {
            return List.of();
        }
        return settings.getBrowsers().getChromeOrFirefoxOrSafari().stream().map(this::browser).toList();
    }

    private BrowserConfig browser(final AbstractBrowser browser) {
        BrowserConfig.BrowserConfigBuilder builder = BrowserConfig.builder()
                .kind(BrowserKind.SAFARI)
                .enabled(browser.isEnabled())
                .maximizedBrowserWindow(browser.isMaximizedBrowserWindow())
                .browserWindowSize(browser.getBrowserWindowSize())
                .alias(browser.getAlias())
                .keepDownloadedFiles(browser.isKeepDownloadedFiles())
                .capabilities(capabilities(browser.getCapabilities()))
                .type(browserType(browser.getBrowserType()));
        engine(builder, browser);
        return builder.build();
    }

    private void engine(final BrowserConfig.BrowserConfigBuilder builder, final AbstractBrowser browser) {
        if (browser instanceof Chrome chrome) {
            engine(builder, BrowserKind.CHROME, chrome.isHeadlessMode(), chrome.getChromeOptionsArguments());
        } else if (browser instanceof Firefox firefox) {
            engine(builder, BrowserKind.FIREFOX, firefox.isHeadlessMode(), firefox.getFirefoxOptionsArguments());
        } else if (browser instanceof Edge edge) {
            engine(builder, BrowserKind.EDGE, edge.isHeadlessMode(), edge.getEdgeOptionsArguments());
        }
    }

    private void engine(final BrowserConfig.BrowserConfigBuilder builder, final BrowserKind kind,
                        final boolean headlessMode, final BrowserOptionsArguments arguments) {
        builder.kind(kind)
                .headlessMode(headlessMode)
                .arguments(arguments == null ? List.of() : List.copyOf(arguments.getArgument()));
    }

    private BrowserTypeConfig browserType(final BrowserType type) {
        if (type == null) {
            return null;
        }
        if (type.getLocalBrowser() != null) {
            return localBrowser(type.getLocalBrowser());
        }
        if (type.getBrowserInDocker() != null) {
            return browserInDocker(type.getBrowserInDocker());
        }
        if (type.getRemoteBrowser() != null) {
            return remoteBrowser(type.getRemoteBrowser());
        }
        return type.getBrowserStack() == null ? null : browserStack(type.getBrowserStack());
    }

    private BrowserTypeConfig localBrowser(final LocalBrowser local) {
        return BrowserTypeConfig.builder()
                .kind(BrowserTypeKind.LOCAL)
                .driverVersion(local.getDriverVersion())
                .build();
    }

    private BrowserTypeConfig browserInDocker(final BrowserInDocker docker) {
        return BrowserTypeConfig.builder()
                .kind(BrowserTypeKind.DOCKER)
                .browserVersion(docker.getBrowserVersion())
                .enableVnc(docker.isEnableVNC())
                .dockerNetwork(docker.getDockerNetwork())
                .screenRecording(screenRecording(docker.getScreenRecording()))
                .build();
    }

    private ScreenRecordingConfig screenRecording(final ScreenRecording recording) {
        if (recording == null) {
            return null;
        }
        return ScreenRecordingConfig.builder()
                .enabled(recording.isEnabled())
                .outputFolder(recording.getOutputFolder())
                .build();
    }

    private BrowserTypeConfig remoteBrowser(final RemoteBrowser remote) {
        return BrowserTypeConfig.builder()
                .kind(BrowserTypeKind.REMOTE)
                .browserVersion(remote.getBrowserVersion())
                .remoteBrowserUrl(remote.getRemoteBrowserURL())
                .build();
    }

    private BrowserTypeConfig browserStack(final BrowserStackWeb browserStack) {
        return BrowserTypeConfig.builder()
                .kind(BrowserTypeKind.BROWSER_STACK)
                .browserVersion(browserStack.getBrowserVersion())
                .os(browserStack.getOs())
                .osVersion(browserStack.getOsVersion())
                .build();
    }

    private MobileBrowserConfig mobilebrowser(final Mobilebrowser mobilebrowser) {
        if (mobilebrowser == null) {
            return null;
        }
        return MobileBrowserConfig.builder()
                .enabled(mobilebrowser.isEnabled())
                .baseUrl(mobilebrowser.getBaseUrl())
                .settings(settings(mobilebrowser))
                .connection(connection(mobilebrowser.getConnection()))
                .devices(mobilebrowserDevices(mobilebrowser.getDevices()))
                .build();
    }

    private List<DeviceConfig> mobilebrowserDevices(final MobilebrowserDevices devices) {
        if (devices == null) {
            return List.of();
        }
        return devices.getDevice().stream().map(this::mobilebrowserDevice).toList();
    }

    private DeviceConfig mobilebrowserDevice(final MobilebrowserDevice device) {
        DeviceConfig.DeviceConfigBuilder builder = device(device);
        if (device.getAppiumCapabilities() != null) {
            appium(builder, device.getAppiumCapabilities());
        } else if (device.getBrowserStackCapabilities() != null) {
            capabilities(builder, ConnectionKind.BROWSER_STACK, device.getBrowserStackCapabilities());
        }
        return builder.build();
    }

    private NativeConfig nativeConfig(final Native nativeConfig) {
        if (nativeConfig == null) {
            return null;
        }
        return NativeConfig.builder()
                .enabled(nativeConfig.isEnabled())
                .settings(settings(nativeConfig))
                .connection(connection(nativeConfig.getConnection()))
                .devices(nativeDevices(nativeConfig.getDevices()))
                .build();
    }

    private List<DeviceConfig> nativeDevices(final NativeDevices devices) {
        if (devices == null) {
            return List.of();
        }
        return devices.getDevice().stream().map(this::nativeDevice).toList();
    }

    private DeviceConfig nativeDevice(final NativeDevice device) {
        DeviceConfig.DeviceConfigBuilder builder = device(device);
        if (device.getAppiumCapabilities() != null) {
            appiumNative(builder, device.getAppiumCapabilities());
        } else if (device.getBrowserStackCapabilities() != null) {
            browserStackNative(builder, device.getBrowserStackCapabilities());
        }
        return builder.build();
    }

    private void appiumNative(final DeviceConfig.DeviceConfigBuilder builder, final AppiumNativeCapabilities appium) {
        appium(builder, appium)
                .app(appium.getApp())
                .appPackage(appium.getAppPackage())
                .appActivity(appium.getAppActivity());
    }

    private void browserStackNative(final DeviceConfig.DeviceConfigBuilder builder,
                                    final BrowserStackNativeCapabilities browserStack) {
        capabilities(builder, ConnectionKind.BROWSER_STACK, browserStack)
                .app(browserStack.getApp())
                .googlePlayLogin(googlePlayLogin(browserStack.getGooglePlayLogin()));
    }

    private DeviceConfig.DeviceConfigBuilder device(final AbstractDevice device) {
        return DeviceConfig.builder()
                .alias(device.getAlias())
                .enabled(device.isEnabled())
                .platform(device.getPlatformName() == null ? null : Platform.valueOf(device.getPlatformName().name()))
                .capabilities(capabilities(device.getCapabilities()));
    }

    private DeviceConfig.DeviceConfigBuilder appium(final DeviceConfig.DeviceConfigBuilder builder,
                                                    final AppiumCapabilities appium) {
        return capabilities(builder, ConnectionKind.APPIUM_SERVER, appium).udid(appium.getUdid());
    }

    private DeviceConfig.DeviceConfigBuilder capabilities(final DeviceConfig.DeviceConfigBuilder builder,
                                                          final ConnectionKind kind,
                                                          final AbstractCapabilities capabilities) {
        return builder.capabilitiesKind(kind)
                .deviceName(capabilities.getDeviceName())
                .platformVersion(capabilities.getPlatformVersion());
    }

    private List<Capability> capabilities(final Capabilities capabilities) {
        if (capabilities == null) {
            return List.of();
        }
        return capabilities.getCapability().stream()
                .map(capability -> Capability.builder()
                        .name(capability.getName())
                        .value(capability.getValue())
                        .build())
                .toList();
    }

    private ConnectionConfig connection(final ConnectionType connection) {
        if (connection == null) {
            return null;
        }
        if (connection.getAppiumServer() != null) {
            return ConnectionConfig.builder()
                    .kind(ConnectionKind.APPIUM_SERVER)
                    .serverUrl(connection.getAppiumServer().getServerUrl())
                    .build();
        }
        return connection.getBrowserStack() == null
                ? null : ConnectionConfig.builder().kind(ConnectionKind.BROWSER_STACK).build();
    }

    private com.testlum.reporting.sdk.model.config.GooglePlayLogin googlePlayLogin(
            final com.testlum.testing.model.global_config.GooglePlayLogin login) {
        if (login == null) {
            return null;
        }
        return com.testlum.reporting.sdk.model.config.GooglePlayLogin.builder()
                .email(login.getEmail())
                .password(login.getPassword())
                .build();
    }

    private com.testlum.reporting.sdk.model.config.BrowserStackLogin browserStackLogin(final BrowserStackLogin login) {
        if (login == null) {
            return null;
        }
        return com.testlum.reporting.sdk.model.config.BrowserStackLogin.builder()
                .username(login.getUsername())
                .accessKey(login.getAccessKey())
                .build();
    }
}
