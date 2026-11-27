package com.testlum.testing.framework.util;

import com.testlum.testing.framework.exception.DriverCreationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;

import static org.junit.jupiter.api.Assertions.*;

class DriverFailureDiagnosticTest {

    private static final String APPIUM_URL = "http://127.0.0.1:4723";
    private static final String CONFIG_PATH = "/dev/ui.xml";

    private static final String APPIUM_PREFIX =
            "Could not start a new session. Response code 500. Message: An unknown server-side error "
            + "occurred while processing the command. Original error: Cannot start the "
            + "'com.instagram.android' application. Consider checking the driver's troubleshooting "
            + "documentation. Original error: Error executing adbExec. Original error: ";

    private static final String ADB_COMMAND =
            "'Command '/Users/admin/Library/Android/sdk/platform-tools/adb -P 5037 -s emulator-5554 "
            + "shell am start -W -n com.instagram.android/com.instagram.android";

    private static final String ADB_TIMEOUT_FAILURE =
            APPIUM_PREFIX + ADB_COMMAND
            + ".activity.MainTabActivity -S -a android.intent.action.MAIN -c "
            + "android.intent.category.LAUNCHER -f 0x10200000' timed out after 20000ms'. Try to increase "
            + "the 20000ms adb execution timeout represented by 'adbExecTimeout' capability";

    private static final String MISSING_ACTIVITY_FAILURE =
            APPIUM_PREFIX + ADB_COMMAND
            + ".NonExistingActivity -S -a android.intent.action.MAIN -c "
            + "android.intent.category.LAUNCHER -f 0x10200000' exited with code 1'; Command output: "
            + "Stopping: com.instagram.android\n"
            + "Starting: Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] "
            + "flg=0x10200000 cmp=com.instagram.android/.NonExistingActivity }\n"
            + "Error type 3\n"
            + "Error: Activity class {com.instagram.android/com.instagram.android.NonExistingActivity} "
            + "does not exist.";

    private final DriverFailureDiagnostic diagnostic = new DriverFailureDiagnostic();

    private DriverFailureContext mobileContext(final String serverUrl) {
        return DriverFailureContext.builder()
                .kind(UiDriverKind.MOBILEBROWSER)
                .alias("pixel-7")
                .qualifier("ANDROID")
                .env("dev")
                .configPath(CONFIG_PATH)
                .connectionName("Appium Server")
                .serverUrl(serverUrl)
                .build();
    }

    private DriverFailureContext webContext() {
        return DriverFailureContext.builder()
                .kind(UiDriverKind.WEB)
                .alias("main")
                .qualifier("Chrome")
                .env("dev")
                .configPath(CONFIG_PATH)
                .connectionName("local browser")
                .build();
    }

    private DriverFailureContext dockerContext() {
        return DriverFailureContext.builder()
                .kind(UiDriverKind.WEB)
                .alias("main")
                .qualifier("Chrome")
                .env("dev")
                .configPath(CONFIG_PATH)
                .connectionName("browser in docker")
                .inDocker(true)
                .build();
    }

    private String describe(final Throwable failure, final DriverFailureContext context) {
        return diagnostic.describeForRetry(failure, context).describe();
    }

    @Nested
    class Context {
        @Test
        void namesTheFailingDeviceWithItsPlatform() {
            String description = describe(new RuntimeException("boom"), mobileContext(APPIUM_URL));

            assertTrue(description.startsWith("Failed to start <mobilebrowser> device 'pixel-7' (ANDROID)"));
        }

        @Test
        void namesTheFailingBrowser() {
            String description = describe(new RuntimeException("boom"), webContext());

            assertTrue(description.startsWith("Failed to start <web> browser 'main' (Chrome)"));
        }

        @Test
        void showsEnvironmentAndConfigFile() {
            String description = describe(new RuntimeException("boom"), mobileContext(APPIUM_URL));

            assertTrue(description.contains("Env        : dev"));
            assertTrue(description.contains("Config     : " + CONFIG_PATH));
        }

        @Test
        void showsConnectionWithUrl() {
            String description = describe(new RuntimeException("boom"), mobileContext(APPIUM_URL));

            assertTrue(description.contains("Connection : Appium Server (" + APPIUM_URL + ")"));
        }

        @Test
        void omitsUrlWhenItCouldNotBeResolved() {
            String description = describe(new RuntimeException("boom"), mobileContext(null));

            assertTrue(description.contains("Connection : Appium Server"));
            assertFalse(description.contains("Appium Server ("));
        }

        @Test
        void masksBrowserStackCredentials() {
            DriverFailureContext context = DriverFailureContext.builder()
                    .kind(UiDriverKind.NATIVE)
                    .alias("bs-iphone")
                    .qualifier("IOS")
                    .env("staging")
                    .configPath("/staging/ui.xml")
                    .connectionName("BrowserStack")
                    .serverUrl("https://bobuser:s3cr3tKey@hub-cloud.browserstack.com/wd/hub")
                    .build();

            String description = describe(new RuntimeException("boom"), context);

            assertFalse(description.contains("s3cr3tKey"));
            assertFalse(description.contains("bobuser"));
            assertTrue(description.contains("https://***@hub-cloud.browserstack.com/wd/hub"));
        }
    }

    @Nested
    class CauseCleanup {
        @Test
        void stripsSeleniumDiagnosticNoise() {
            RuntimeException failure = new RuntimeException("Could not start a new session. "
                                                            + "Original error: udid 'ABC123' is not connected\n"
                                                            + "Host info: host: 'mac.local'\n"
                                                            + "Build info: version: '4.21.0'\n"
                                                            + "Capabilities {appium:automationName: uiautomator2}");

            String description = describe(failure, mobileContext(APPIUM_URL));

            assertTrue(description.contains("udid 'ABC123' is not connected"));
            assertFalse(description.contains("Host info:"));
            assertFalse(description.contains("Build info:"));
            assertFalse(description.contains("Capabilities {"));
        }

        @Test
        void keepsAResponseCodeThatIsPartOfTheMessage() {
            String description = describe(
                    new RuntimeException("WWW-Authenticate header missing for response code 401"),
                    mobileContext(APPIUM_URL));

            assertTrue(description.contains("WWW-Authenticate header missing for response code 401"));
        }

        @Test
        void stillStripsAResponseCodeThatStandsAsItsOwnSentence() {
            String description = describe(
                    new RuntimeException("Could not start a new session. Response code 401. "
                                         + "Message: Authorization required"),
                    mobileContext(APPIUM_URL));

            assertTrue(description.contains("Cause      : Message: Authorization required"));
            assertFalse(description.contains("Response code 401"));
        }

        @Test
        void unwrapsToTheRootCause() {
            RuntimeException failure = new RuntimeException("wrapper", new ConnectException("Connection refused"));

            String description = describe(failure, mobileContext(APPIUM_URL));

            assertTrue(description.contains("Cause      : Connection refused"));
        }

        @Test
        void takesTheDeepestMessageAvailableWhenTheRootCauseHasNone() {
            RuntimeException failure = new RuntimeException(
                    "Could not start a new session", new ClosedChannelException());

            String description = describe(failure, mobileContext(APPIUM_URL));

            assertTrue(description.contains("Cause      : ClosedChannelException: Could not start a new session"));
        }

        @Test
        void ignoresAWrapperMessageThatOnlyEchoesItsCause() {
            ClosedChannelException cause = new ClosedChannelException();
            RuntimeException failure = new RuntimeException(cause.toString(), cause);

            String description = describe(failure, mobileContext(APPIUM_URL));

            assertTrue(description.contains("Cause      : ClosedChannelException"));
            assertFalse(description.contains("java.nio.channels"));
        }

        @Test
        void fallsBackToTheExceptionTypeWhenThereIsNoMessage() {
            String description = describe(new IllegalStateException(), mobileContext(APPIUM_URL));

            assertTrue(description.contains("Cause      : IllegalStateException"));
        }

        @Test
        void wrapsLongLinesToStayReadable() {
            String description = describe(new RuntimeException("boom"), mobileContext(APPIUM_URL));

            assertTrue(description.lines().allMatch(line -> line.length() <= 120));
        }
    }

    @Nested
    class Hints {
        @Test
        void explainsUnreachableServerAndAllowsRetry() {
            DriverCreationException exception = diagnostic.describeForRetry(
                    new ConnectException("Connection refused"), mobileContext(APPIUM_URL));

            assertTrue(exception.describe().contains("Server at " + APPIUM_URL + " did not respond"));
            assertTrue(exception.isRetryable());
        }

        @Test
        void treatsAClosedChannelAsAnUnreachableServer() {
            DriverCreationException exception = diagnostic.describeForRetry(
                    new RuntimeException("Could not start a new session", new ClosedChannelException()),
                    mobileContext(APPIUM_URL));

            assertTrue(exception.describe().contains("Server at " + APPIUM_URL + " did not respond"));
            assertTrue(exception.isRetryable());
        }

        @Test
        void matchesASignalBuriedDeepInTheCauseChain() {
            RuntimeException failure = new RuntimeException("Could not start a new session",
                    new IllegalStateException("wrapper", new ConnectException("Connection refused")));

            String description = describe(failure, mobileContext(APPIUM_URL));

            assertTrue(description.contains("Server at " + APPIUM_URL + " did not respond"));
        }

        @Test
        void doesNotMistakeCapabilityNamesForFailurePhrases() {
            RuntimeException failure = new RuntimeException(
                    "Connection error (POST http://127.0.0.1:4723/session)\n"
                    + "Capabilities {appium:appActivity: .MainActivity, appium:appPackage: com.example.app}",
                    new ClosedChannelException());

            String description = describe(failure, mobileContext(APPIUM_URL));

            assertFalse(description.contains("<appActivity>"));
            assertTrue(description.contains("Server at " + APPIUM_URL + " did not respond"));
        }

        @Test
        void stillExplainsARealActivityFailure() {
            RuntimeException failure = new RuntimeException(
                    "Activity name '.Main' used to start the app doesn't exist or cannot be launched");

            String description = describe(failure, mobileContext(APPIUM_URL));

            assertTrue(description.contains("<appActivity>"));
        }

        @Test
        void explainsRejectedBrowserStackCredentialsWithoutRetrying() {
            DriverCreationException exception = diagnostic.describeForRetry(
                    new RuntimeException("Response code 401. Message: Authorization required"),
                    mobileContext(APPIUM_URL));

            assertTrue(exception.describe().contains("<browserStackLogin>"));
            assertFalse(exception.isRetryable());
        }

        @Test
        void explainsDisconnectedDevice() {
            String description = describe(
                    new RuntimeException("udid 'ABC123' is not connected"), mobileContext(APPIUM_URL));

            assertTrue(description.contains("adb devices"));
            assertTrue(description.contains("<udid> in " + CONFIG_PATH));
        }

        @Test
        void explainsDriverVersionMismatch() {
            String description = describe(
                    new RuntimeException("session not created: This version of ChromeDriver "
                                         + "only supports Chrome version 126"), webContext());

            assertTrue(description.contains("<driverVersion>"));
        }

        @Test
        void explainsInvalidUrlEvenWhenItWasNeverResolved() {
            String description = describe(new MalformedURLException("no protocol: 127.0.0.1:4723"),
                    mobileContext(null));

            assertTrue(description.contains("is not a valid URL"));
        }

        @Test
        void doesNotClaimAnUnreachableServerWhenNoUrlIsKnown() {
            String description = describe(new ConnectException("Connection refused"), webContext());

            assertFalse(description.contains("Server at null"));
            assertTrue(description.contains("Check the <web> browser configuration"));
        }

        @Test
        void fallsBackPerDriverKindAndKeepsRetryingUnknownFailures() {
            DriverCreationException exception = diagnostic.describeForRetry(
                    new RuntimeException("something odd"), mobileContext(APPIUM_URL));

            assertTrue(exception.describe().contains("Check the <mobilebrowser> device configuration"));
            assertTrue(exception.isRetryable());
        }
    }

    @Nested
    class AppiumCauses {
        @Test
        void reducesAnAdbTimeoutToASingleReadableLine() {
            String description = describe(new RuntimeException(ADB_TIMEOUT_FAILURE), mobileContext(APPIUM_URL));

            assertTrue(description.contains("Cause      : Could not start 'com.instagram.android': "
                                            + "adb command timed out after 20000ms"));
            assertTrue(description.contains("appium:adbExecTimeout"));
            assertFalse(description.contains("platform-tools/adb"));
        }

        @Test
        void reducesAMissingActivityToTheLineThatExplainsIt() {
            String description = describe(new RuntimeException(MISSING_ACTIVITY_FAILURE), mobileContext(APPIUM_URL));

            assertTrue(description.contains("Cause      : Activity class {com.instagram.android/"
                                            + "com.instagram.android.NonExistingActivity} does not exist"));
            assertTrue(description.contains("dumpsys package <appPackage>"));
            assertFalse(description.contains("Error type 3"));
        }

        @Test
        void doesNotMistakeAMissingActivityForAnAdbTimeout() {
            String description = describe(new RuntimeException(MISSING_ACTIVITY_FAILURE), mobileContext(APPIUM_URL));

            assertFalse(description.contains("adbExecTimeout"));
            assertFalse(description.contains("timed out"));
        }

        @Test
        void doesNotMistakeAnAdbTimeoutForAMissingActivity() {
            String description = describe(new RuntimeException(ADB_TIMEOUT_FAILURE), mobileContext(APPIUM_URL));

            assertFalse(description.contains("dumpsys package"));
            assertFalse(description.contains("does not exist"));
        }

        @Test
        void neitherFailureIsRetried() {
            assertFalse(diagnostic.describeForRetry(
                    new RuntimeException(ADB_TIMEOUT_FAILURE), mobileContext(APPIUM_URL)).isRetryable());
            assertFalse(diagnostic.describeForRetry(
                    new RuntimeException(MISSING_ACTIVITY_FAILURE), mobileContext(APPIUM_URL)).isRetryable());
        }

        @Test
        void namesTheExceptionTypeWhenNothingCouldExplainTheFailure() {
            String description = describe(new FileNotFoundException("No such file or directory"), webContext());

            assertTrue(description.contains("Cause      : FileNotFoundException: No such file or directory"));
        }

        @Test
        void keepsAnExplainedCauseFreeOfTheExceptionType() {
            String description = describe(new ConnectException("Connection refused"), mobileContext(APPIUM_URL));

            assertTrue(description.contains("Cause      : Connection refused"));
            assertFalse(description.contains("ConnectException: Connection refused"));
        }

        @Test
        void dropsAppiumBoilerplateFromAnUnexplainedFailure() {
            String description = describe(new RuntimeException(
                            "Could not start a new session. Response code 500. Message: An unknown server-side "
                            + "error occurred while processing the command. Original error: something odd"),
                    mobileContext(APPIUM_URL));

            assertTrue(description.contains("Cause      : RuntimeException: something odd"));
        }
    }

    @Nested
    class DockerCauses {

        private RuntimeException daemonDown() {
            SocketException cause = new SocketException("No such file or directory");
            return new RuntimeException(cause.toString(), cause);
        }

        @Test
        void explainsAnUnreachableDockerDaemonWithoutRetrying() {
            DriverCreationException exception = diagnostic.describeForRetry(daemonDown(), dockerContext());

            assertTrue(exception.describe().contains(
                    "Cause      : Docker daemon is not reachable: No such file or directory"));
            assertTrue(exception.describe().contains("Start Docker"));
            assertFalse(exception.isRetryable());
        }

        @Test
        void doesNotBlameDockerWhenTheBrowserDoesNotRunInDocker() {
            String description = describe(daemonDown(), webContext());

            assertFalse(description.contains("Start Docker"));
            assertTrue(description.contains("Cause      : SocketException: No such file or directory"));
        }
    }

    @Nested
    class Reuse {
        @Test
        void doesNotDescribeAnAlreadyDescribedFailureTwice() {
            DriverCreationException original =
                    new DriverCreationException("already formatted", false, null);

            DriverCreationException described =
                    diagnostic.describeForRetry(new RuntimeException("wrapper", original), mobileContext(APPIUM_URL));

            assertSame(original, described);
            assertEquals("already formatted", described.describe());
        }
    }
}
