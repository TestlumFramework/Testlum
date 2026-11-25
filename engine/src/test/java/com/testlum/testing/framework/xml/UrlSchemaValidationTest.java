package com.testlum.testing.framework.xml;

import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.UiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlSchemaValidationTest {

    private XMLParser<UiConfig> uiConfigParser;
    private XMLParser<Integrations> integrationsParser;

    @BeforeEach
    void setUp() {
        XMLParsers xmlParsers = new XMLParsers();
        uiConfigParser = xmlParsers.forUiConfig();
        integrationsParser = xmlParsers.forIntegrations();
    }

    private File createNativeUiConfigFile(final Path tempDir, final String serverUrl) throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<uiConfig xmlns=\"http://www.testlum.com/testing/model/global-config\">\n"
                + "    <native enabled=\"true\">\n"
                + "        <takeScreenshots enabled=\"false\"/>\n"
                + "        <elementAutowait seconds=\"5\"/>\n"
                + "        <connection>\n"
                + "            <appiumServer>\n"
                + "                <serverUrl>" + serverUrl + "</serverUrl>\n"
                + "            </appiumServer>\n"
                + "        </connection>\n"
                + "        <devices>\n"
                + "            <device alias=\"android_1\" enabled=\"true\" platformName=\"android\">\n"
                + "                <appiumCapabilities>\n"
                + "                    <deviceName>Pixel</deviceName>\n"
                + "                    <platformVersion>14</platformVersion>\n"
                + "                    <udid>emulator-5554</udid>\n"
                + "                    <appPackage>com.example.app</appPackage>\n"
                + "                    <appActivity>.MainActivity</appActivity>\n"
                + "                </appiumCapabilities>\n"
                + "            </device>\n"
                + "        </devices>\n"
                + "    </native>\n"
                + "</uiConfig>";
        Path file = tempDir.resolve("ui-native.xml");
        Files.writeString(file, xml);
        return file.toFile();
    }

    private File createMobileBrowserUiConfigFile(final Path tempDir,
                                                 final String baseUrl,
                                                 final String serverUrl) throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<uiConfig xmlns=\"http://www.testlum.com/testing/model/global-config\">\n"
                + "    <mobilebrowser enabled=\"true\">\n"
                + "        <takeScreenshots enabled=\"false\"/>\n"
                + "        <elementAutowait seconds=\"5\"/>\n"
                + "        <baseUrl>" + baseUrl + "</baseUrl>\n"
                + "        <connection>\n"
                + "            <appiumServer>\n"
                + "                <serverUrl>" + serverUrl + "</serverUrl>\n"
                + "            </appiumServer>\n"
                + "        </connection>\n"
                + "        <devices>\n"
                + "            <device alias=\"mobile_1\" enabled=\"true\" platformName=\"android\">\n"
                + "                <appiumCapabilities>\n"
                + "                    <deviceName>Pixel</deviceName>\n"
                + "                    <platformVersion>14</platformVersion>\n"
                + "                    <udid>emulator-5554</udid>\n"
                + "                </appiumCapabilities>\n"
                + "            </device>\n"
                + "        </devices>\n"
                + "    </mobilebrowser>\n"
                + "</uiConfig>";
        Path file = tempDir.resolve("ui-mobilebrowser.xml");
        Files.writeString(file, xml);
        return file.toFile();
    }

    private File createApiIntegrationFile(final Path tempDir, final String apiUrl) throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<integrations xmlns=\"http://www.testlum.com/testing/model/global-config\">\n"
                + "    <apis>\n"
                + "        <api alias=\"testApi\" enabled=\"true\" url=\"" + apiUrl + "\"/>\n"
                + "    </apis>\n"
                + "</integrations>";
        Path file = tempDir.resolve("integrations.xml");
        Files.writeString(file, xml);
        return file.toFile();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:4723",
            "http://localhost:4723/wd/hub",
            "https://127.0.0.1:4723/wd/hub",
            "http://appium:4723",
            "http://appium_server:4723/wd_hub",
            "https://hub-cloud.browserstack.com/wd/hub",
            "https://user:s3cr3tKey@hub-cloud.browserstack.com/wd/hub",
            "https://example.com/api/v1?foo=bar_1#section",
            "${APPIUM_SERVER_URL}"
    })
    void acceptsValidNativeAppiumServerUrls(final String serverUrl, @TempDir final Path tempDir) throws IOException {
        File file = createNativeUiConfigFile(tempDir, serverUrl);
        UiConfig config = uiConfigParser.process(file);
        assertNotNull(config);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:abcd",
            "http://localhost:-1",
            "http://:4723",
            "http://localhost:",
            "http://localhost:4723:8080",
            "http://",
            "http://-invalid:4723",
            "http://invalid-:4723",
            "http://user:pass@",
            "ftp://localhost:4723",
            "localhost:4723"
    })
    void rejectsInvalidNativeAppiumServerUrls(final String serverUrl, @TempDir final Path tempDir) throws IOException {
        File file = createNativeUiConfigFile(tempDir, serverUrl);
        XSDException exception = assertThrows(XSDException.class, () -> uiConfigParser.process(file));
        assertUnexpectedFormat(exception, serverUrl);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:abcd",
            "http://localhost:-1",
            "http://:4723"
    })
    void rejectsInvalidMobileBrowserAppiumServerUrls(final String serverUrl,
                                                     @TempDir final Path tempDir) throws IOException {
        File file = createMobileBrowserUiConfigFile(tempDir, "https://example.com", serverUrl);
        XSDException exception = assertThrows(XSDException.class, () -> uiConfigParser.process(file));
        assertUnexpectedFormat(exception, serverUrl);
    }

    @Test
    void acceptsValidMobileBrowserConfig(@TempDir final Path tempDir) throws IOException {
        File file = createMobileBrowserUiConfigFile(tempDir, "https://example.com:8443/app", "http://localhost:4723");
        assertNotNull(uiConfigParser.process(file));
    }

    @Test
    void rejectsInvalidApiIntegrationUrl(@TempDir final Path tempDir) throws IOException {
        String apiUrl = "http://localhost:abcd";
        File file = createApiIntegrationFile(tempDir, apiUrl);
        XSDException exception = assertThrows(XSDException.class, () -> integrationsParser.process(file));
        assertUnexpectedFormat(exception, apiUrl);
    }

    private static void assertUnexpectedFormat(final XSDException exception, final String expectedValue) {
        String message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.contains(expectedValue), () -> "Expected value '" + expectedValue + "' in: " + message);
        assertTrue(message.contains("unexpected format"), () -> "Expected 'unexpected format' in: " + message);
    }

    @Test
    void acceptsValidApiIntegrationUrl(@TempDir final Path tempDir) throws IOException {
        File file = createApiIntegrationFile(tempDir, "https://api.example.com:8080/v1");
        assertNotNull(integrationsParser.process(file));
    }
}
