package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil.BrowserType;
import com.knubisoft.testlum.testing.model.global_config.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrowserUtilTest {

    @Mock
    private EnvironmentLoader environmentLoader;

    @Mock
    private UiConfig uiConfig;

    @InjectMocks
    private BrowserUtil browserUtil;

    @Nested
    class FilterDefaultEnabledBrowsers {

        @Test
        void returnsEmptyListWhenWebIsNull() {
            when(uiConfig.getWeb()).thenReturn(null);

            List<AbstractBrowser> result = browserUtil.filterDefaultEnabledBrowsers();

            assertTrue(result.isEmpty());
        }

        @Test
        void filtersOnlyEnabledBrowsers() {
            Web web = mock(Web.class);
            BrowserSettings browserSettings = mock(BrowserSettings.class);
            Browsers browsers = mock(Browsers.class);

            AbstractBrowser enabledBrowser = mock(AbstractBrowser.class);
            AbstractBrowser disabledBrowser = mock(AbstractBrowser.class);
            when(enabledBrowser.isEnabled()).thenReturn(true);
            when(disabledBrowser.isEnabled()).thenReturn(false);

            when(uiConfig.getWeb()).thenReturn(web);
            when(web.getBrowserSettings()).thenReturn(browserSettings);
            when(browserSettings.getBrowsers()).thenReturn(browsers);
            when(browsers.getChromeOrFirefoxOrSafari()).thenReturn(Arrays.asList(enabledBrowser, disabledBrowser));

            List<AbstractBrowser> result = browserUtil.filterDefaultEnabledBrowsers();

            assertEquals(1, result.size());
            assertEquals(enabledBrowser, result.get(0));
        }

        @Test
        void returnsAllWhenAllEnabled() {
            Web web = mock(Web.class);
            BrowserSettings browserSettings = mock(BrowserSettings.class);
            Browsers browsers = mock(Browsers.class);

            AbstractBrowser b1 = mock(AbstractBrowser.class);
            AbstractBrowser b2 = mock(AbstractBrowser.class);
            when(b1.isEnabled()).thenReturn(true);
            when(b2.isEnabled()).thenReturn(true);

            when(uiConfig.getWeb()).thenReturn(web);
            when(web.getBrowserSettings()).thenReturn(browserSettings);
            when(browserSettings.getBrowsers()).thenReturn(browsers);
            when(browsers.getChromeOrFirefoxOrSafari()).thenReturn(Arrays.asList(b1, b2));

            List<AbstractBrowser> result = browserUtil.filterDefaultEnabledBrowsers();

            assertEquals(2, result.size());
        }

        @Test
        void returnsEmptyWhenAllDisabled() {
            Web web = mock(Web.class);
            BrowserSettings browserSettings = mock(BrowserSettings.class);
            Browsers browsers = mock(Browsers.class);

            AbstractBrowser disabled = mock(AbstractBrowser.class);
            when(disabled.isEnabled()).thenReturn(false);

            when(uiConfig.getWeb()).thenReturn(web);
            when(web.getBrowserSettings()).thenReturn(browserSettings);
            when(browserSettings.getBrowsers()).thenReturn(browsers);
            when(browsers.getChromeOrFirefoxOrSafari()).thenReturn(List.of(disabled));

            List<AbstractBrowser> result = browserUtil.filterDefaultEnabledBrowsers();

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyWhenNoBrowsersConfigured() {
            Web web = mock(Web.class);
            BrowserSettings browserSettings = mock(BrowserSettings.class);
            Browsers browsers = mock(Browsers.class);

            when(uiConfig.getWeb()).thenReturn(web);
            when(web.getBrowserSettings()).thenReturn(browserSettings);
            when(browserSettings.getBrowsers()).thenReturn(browsers);
            when(browsers.getChromeOrFirefoxOrSafari()).thenReturn(Collections.emptyList());

            List<AbstractBrowser> result = browserUtil.filterDefaultEnabledBrowsers();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetBrowserBy {

        @Test
        void returnsEmptyWhenAliasIsBlank() {
            Optional<AbstractBrowser> result = browserUtil.getBrowserBy("env", "");
            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyWhenAliasIsNull() {
            Optional<AbstractBrowser> result = browserUtil.getBrowserBy("env", null);
            assertTrue(result.isEmpty());
        }

        @Test
        void returnsBrowserWhenFound() {
            Web web = mock(Web.class);
            BrowserSettings browserSettings = mock(BrowserSettings.class);
            Browsers browsers = mock(Browsers.class);
            AbstractBrowser browser = mock(AbstractBrowser.class);
            when(browser.isEnabled()).thenReturn(true);
            when(browser.getAlias()).thenReturn("chrome");
            when(browsers.getChromeOrFirefoxOrSafari()).thenReturn(List.of(browser));
            when(browserSettings.getBrowsers()).thenReturn(browsers);
            when(web.getBrowserSettings()).thenReturn(browserSettings);
            when(environmentLoader.getWebSettings("dev")).thenReturn(Optional.of(web));

            Optional<AbstractBrowser> result = browserUtil.getBrowserBy("dev", "chrome");

            assertTrue(result.isPresent());
            assertSame(browser, result.get());
        }

        @Test
        void returnsEmptyWhenBrowserDisabled() {
            Web web = mock(Web.class);
            BrowserSettings browserSettings = mock(BrowserSettings.class);
            Browsers browsers = mock(Browsers.class);
            AbstractBrowser browser = mock(AbstractBrowser.class);
            when(browser.isEnabled()).thenReturn(false);
            when(browsers.getChromeOrFirefoxOrSafari()).thenReturn(List.of(browser));
            when(browserSettings.getBrowsers()).thenReturn(browsers);
            when(web.getBrowserSettings()).thenReturn(browserSettings);
            when(environmentLoader.getWebSettings("dev")).thenReturn(Optional.of(web));

            Optional<AbstractBrowser> result = browserUtil.getBrowserBy("dev", "chrome");

            assertTrue(result.isEmpty());
        }

        @Test
        void caseInsensitiveAliasMatch() {
            Web web = mock(Web.class);
            BrowserSettings browserSettings = mock(BrowserSettings.class);
            Browsers browsers = mock(Browsers.class);
            AbstractBrowser browser = mock(AbstractBrowser.class);
            when(browser.isEnabled()).thenReturn(true);
            when(browser.getAlias()).thenReturn("Chrome");
            when(browsers.getChromeOrFirefoxOrSafari()).thenReturn(List.of(browser));
            when(browserSettings.getBrowsers()).thenReturn(browsers);
            when(web.getBrowserSettings()).thenReturn(browserSettings);
            when(environmentLoader.getWebSettings("dev")).thenReturn(Optional.of(web));

            Optional<AbstractBrowser> result = browserUtil.getBrowserBy("dev", "chrome");

            assertTrue(result.isPresent());
        }

        @Test
        void returnsEmptyWhenWebSettingsNotFound() {
            when(environmentLoader.getWebSettings("dev")).thenReturn(Optional.empty());

            Optional<AbstractBrowser> result = browserUtil.getBrowserBy("dev", "chrome");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class ManageWindowSize {

        @Test
        void setsWindowSizeWhenSpecified() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            when(browser.getBrowserWindowSize()).thenReturn("1920x1080");
            when(browser.isMaximizedBrowserWindow()).thenReturn(false);

            WebDriver webDriver = mock(WebDriver.class);
            WebDriver.Options options = mock(WebDriver.Options.class);
            WebDriver.Window window = mock(WebDriver.Window.class);
            when(webDriver.manage()).thenReturn(options);
            when(options.window()).thenReturn(window);

            browserUtil.manageWindowSize(browser, webDriver);

            verify(window).setSize(new Dimension(1920, 1080));
            verify(window, never()).maximize();
        }

        @Test
        void maximizesWindowWhenEnabled() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            when(browser.getBrowserWindowSize()).thenReturn(null);
            when(browser.isMaximizedBrowserWindow()).thenReturn(true);

            WebDriver webDriver = mock(WebDriver.class);
            WebDriver.Options options = mock(WebDriver.Options.class);
            WebDriver.Window window = mock(WebDriver.Window.class);
            when(webDriver.manage()).thenReturn(options);
            when(options.window()).thenReturn(window);

            browserUtil.manageWindowSize(browser, webDriver);

            verify(window).maximize();
        }

        @Test
        void doesNothingWhenNoSizeAndNoMaximize() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            when(browser.getBrowserWindowSize()).thenReturn(null);
            when(browser.isMaximizedBrowserWindow()).thenReturn(false);

            WebDriver webDriver = mock(WebDriver.class);

            browserUtil.manageWindowSize(browser, webDriver);

            verify(webDriver, never()).manage();
        }

        @Test
        void setsWindowSizeAndMaximizesBoth() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            when(browser.getBrowserWindowSize()).thenReturn("800x600");
            when(browser.isMaximizedBrowserWindow()).thenReturn(true);

            WebDriver webDriver = mock(WebDriver.class);
            WebDriver.Options options = mock(WebDriver.Options.class);
            WebDriver.Window window = mock(WebDriver.Window.class);
            when(webDriver.manage()).thenReturn(options);
            when(options.window()).thenReturn(window);

            browserUtil.manageWindowSize(browser, webDriver);

            verify(window).setSize(new Dimension(800, 600));
            verify(window).maximize();
        }
    }

    @Nested
    class GetBrowserType {

        @Test
        void returnsRemoteWhenRemoteBrowserIsNotNull() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getRemoteBrowser()).thenReturn(mock(RemoteBrowser.class));

            assertEquals(BrowserType.REMOTE, browserUtil.getBrowserType(browser));
        }

        @Test
        void returnsInDockerWhenBrowserInDockerIsNotNull() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getRemoteBrowser()).thenReturn(null);
            when(browserTypeObj.getBrowserInDocker()).thenReturn(mock(BrowserInDocker.class));

            assertEquals(BrowserType.IN_DOCKER, browserUtil.getBrowserType(browser));
        }

        @Test
        void returnsBrowserStackWhenBrowserStackIsNotNull() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getRemoteBrowser()).thenReturn(null);
            when(browserTypeObj.getBrowserInDocker()).thenReturn(null);
            when(browserTypeObj.getBrowserStack()).thenReturn(mock(BrowserStackWeb.class));

            assertEquals(BrowserType.BROWSER_STACK, browserUtil.getBrowserType(browser));
        }

        @Test
        void returnsLocalWhenAllAreNull() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getRemoteBrowser()).thenReturn(null);
            when(browserTypeObj.getBrowserInDocker()).thenReturn(null);
            when(browserTypeObj.getBrowserStack()).thenReturn(null);

            assertEquals(BrowserType.LOCAL, browserUtil.getBrowserType(browser));
        }
    }

    @Nested
    class GetBrowserVersion {

        @Test
        void returnsRemoteVersion() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            RemoteBrowser remoteBrowser = mock(RemoteBrowser.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getRemoteBrowser()).thenReturn(remoteBrowser);
            when(remoteBrowser.getBrowserVersion()).thenReturn("100.0");

            assertEquals("100.0", browserUtil.getBrowserVersion(browser, BrowserType.REMOTE));
        }

        @Test
        void returnsInDockerVersion() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            BrowserInDocker dockerBrowser = mock(BrowserInDocker.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getBrowserInDocker()).thenReturn(dockerBrowser);
            when(dockerBrowser.getBrowserVersion()).thenReturn("99.0");

            assertEquals("99.0", browserUtil.getBrowserVersion(browser, BrowserType.IN_DOCKER));
        }

        @Test
        void returnsBrowserStackVersion() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            BrowserStackWeb bsBrowser = mock(BrowserStackWeb.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getBrowserStack()).thenReturn(bsBrowser);
            when(bsBrowser.getBrowserVersion()).thenReturn("98.0");

            assertEquals("98.0", browserUtil.getBrowserVersion(browser, BrowserType.BROWSER_STACK));
        }

        @Test
        void returnsLocalDriverVersion() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            LocalBrowser localBrowser = mock(LocalBrowser.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getLocalBrowser()).thenReturn(localBrowser);
            when(localBrowser.getDriverVersion()).thenReturn("97.0");

            assertEquals("97.0", browserUtil.getBrowserVersion(browser, BrowserType.LOCAL));
        }

        @Test
        void returnsDefaultMessageWhenLocalVersionIsBlank() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            LocalBrowser localBrowser = mock(LocalBrowser.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getLocalBrowser()).thenReturn(localBrowser);
            when(localBrowser.getDriverVersion()).thenReturn("");

            assertEquals("No browser version specified (the latest version is used)",
                    browserUtil.getBrowserVersion(browser, BrowserType.LOCAL));
        }

        @Test
        void returnsDefaultMessageWhenLocalVersionIsNull() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            LocalBrowser localBrowser = mock(LocalBrowser.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getLocalBrowser()).thenReturn(localBrowser);
            when(localBrowser.getDriverVersion()).thenReturn(null);

            assertEquals("No browser version specified (the latest version is used)",
                    browserUtil.getBrowserVersion(browser, BrowserType.LOCAL));
        }
    }

    @Nested
    class GetBrowserInfo {

        @Test
        void formatsBrowserInfoString() {
            AbstractBrowser browser = mock(AbstractBrowser.class);
            com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                    mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
            when(browser.getBrowserType()).thenReturn(browserTypeObj);
            when(browserTypeObj.getRemoteBrowser()).thenReturn(null);
            when(browserTypeObj.getBrowserInDocker()).thenReturn(null);
            when(browserTypeObj.getBrowserStack()).thenReturn(null);

            LocalBrowser localBrowser = mock(LocalBrowser.class);
            when(browserTypeObj.getLocalBrowser()).thenReturn(localBrowser);
            when(localBrowser.getDriverVersion()).thenReturn("120.0");

            String info = browserUtil.getBrowserInfo(browser);

            assertNotNull(info);
            assertTrue(info.contains("local browser"));
            assertTrue(info.contains("120.0"));
        }
    }

    @Nested
    class BrowserTypeEnum {

        @Test
        void valuesHaveCorrectTypeNames() {
            assertEquals("local browser", BrowserType.LOCAL.getTypeName());
            assertEquals("remote browser", BrowserType.REMOTE.getTypeName());
            assertEquals("browser in docker", BrowserType.IN_DOCKER.getTypeName());
            assertEquals("browserStack", BrowserType.BROWSER_STACK.getTypeName());
        }

        @Test
        void hasFourValues() {
            assertEquals(4, BrowserType.values().length);
        }

        @Test
        void valueOfWorksForAllTypes() {
            assertEquals(BrowserType.LOCAL, BrowserType.valueOf("LOCAL"));
            assertEquals(BrowserType.REMOTE, BrowserType.valueOf("REMOTE"));
            assertEquals(BrowserType.IN_DOCKER, BrowserType.valueOf("IN_DOCKER"));
            assertEquals(BrowserType.BROWSER_STACK, BrowserType.valueOf("BROWSER_STACK"));
        }
    }
}
