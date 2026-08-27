package com.knubisoft.testlum.testing.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DriverFailureMessage {

    public static final String HEADLINE_DEVICE = "Failed to start <%s> device '%s' (%s)";
    public static final String HEADLINE_BROWSER = "Failed to start <%s> browser '%s' (%s)";
    public static final String HEADLINE_PLAIN = "Failed to start <%s> '%s'";

    public static final String LABEL_ENV = "Env";
    public static final String LABEL_CONFIG = "Config";
    public static final String LABEL_CONNECTION = "Connection";
    public static final String LABEL_CAUSE = "Cause";
    public static final String LABEL_HOW_TO_FIX = "How to fix";

    public static final String CONNECTION_WITH_URL = "%s (%s)";
    public static final String UNKNOWN_CAUSE = "No error message was provided by the driver";
    public static final String RAW_FAILURE_DEBUG = "Raw driver failure for {}";

    public static final String SERVER_NOT_REACHABLE =
            "Server at %s did not respond. Make sure it is running and reachable, "
            + "then verify the connection settings in %s";
    public static final String SERVER_ENDPOINT_NOT_FOUND =
            "Server at %s is reachable but does not expose a WebDriver endpoint. "
            + "Check the path (usually /wd/hub) in %s";
    public static final String INVALID_SERVER_URL =
            "The configured server address is not a valid URL. Fix it in %s "
            + "(a full address is expected, for example http://127.0.0.1:4723)";

    public static final String BROWSER_STACK_AUTH_FAILED =
            "BrowserStack rejected the credentials. Check <username> and <accessKey> "
            + "inside <browserStackLogin> in %s. If the values come from Vault or system "
            + "properties, make sure the substitution actually resolved";

    public static final String DEVICE_NOT_CONNECTED =
            "The requested device is not available on the Appium host. Run 'adb devices' (Android) "
            + "or 'xcrun xctrace list devices' (iOS) and align <udid> in %s with a connected device";
    public static final String APP_NOT_FOUND =
            "The application file declared in <app> was not found. Check that the path in %s "
            + "exists and is readable by the Appium server";
    public static final String APP_ACTIVITY_NOT_STARTED =
            "The application started but the expected activity never appeared. "
            + "Verify <appPackage> and <appActivity> in %s";

    public static final String APP_ACTIVITY_NOT_FOUND =
            "The activity declared in <appActivity> does not exist in the installed application. "
            + "Check the value in %s; run 'adb shell dumpsys package <appPackage>' to list the "
            + "activities the app actually declares";
    public static final String ADB_EXEC_TIMEOUT =
            "The adb command did not finish in time. The device or emulator may be slow or busy. "
            + "Raise the limit with <capability name=\"appium:adbExecTimeout\" value=\"60000\"/> in %s, "
            + "or check that the device responds to 'adb devices'";

    public static final String BROWSER_VERSION_MISMATCH =
            "The driver does not match the installed browser. Either update the browser or pin a matching "
            + "<driverVersion> (local browser) / <browserVersion> (docker, remote, BrowserStack) in %s";
    public static final String DOCKER_NOT_AVAILABLE =
            "Docker is not reachable. Start Docker and make sure the current user can access the Docker socket, "
            + "or switch <browserType> away from <browserInDocker> in %s";
    public static final String SAFARI_REMOTE_AUTOMATION_DISABLED =
            "Safari does not allow remote automation. Enable Develop > Allow Remote Automation in Safari "
            + "and run 'safaridriver --enable' once";

    public static final String FALLBACK_WEB =
            "Check the <web> browser configuration in %s and make sure the browser and its driver "
            + "are available on this machine";
    public static final String FALLBACK_MOBILEBROWSER =
            "Check the <mobilebrowser> device configuration in %s and make sure the Appium server "
            + "and the device are available";
    public static final String FALLBACK_NATIVE =
            "Check the <native> device configuration in %s and make sure the Appium server, the device "
            + "and the application under test are available";
}
