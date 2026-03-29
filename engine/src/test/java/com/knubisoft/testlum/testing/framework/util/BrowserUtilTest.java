package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil.BrowserType;
import com.knubisoft.testlum.testing.model.global_config.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrowserUtilTest {

    @Mock
    private EnvironmentLoader environmentLoader;

    @Mock
    private UiConfig uiConfig;

    @InjectMocks
    private BrowserUtil browserUtil;

    @Test
    void filterDefaultEnabledBrowsersReturnsEmptyListWhenWebIsNull() {
        when(uiConfig.getWeb()).thenReturn(null);

        List<AbstractBrowser> result = browserUtil.filterDefaultEnabledBrowsers();

        assertTrue(result.isEmpty());
    }

    @Test
    void filterDefaultEnabledBrowsersFiltersOnlyEnabledBrowsers() {
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
    void getBrowserByReturnsEmptyWhenAliasIsBlank() {
        Optional<AbstractBrowser> result = browserUtil.getBrowserBy("env", "");

        assertTrue(result.isEmpty());
    }

    @Test
    void getBrowserByReturnsEmptyWhenAliasIsNull() {
        Optional<AbstractBrowser> result = browserUtil.getBrowserBy("env", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBrowserTypeReturnsRemoteWhenRemoteBrowserIsNotNull() {
        AbstractBrowser browser = mock(AbstractBrowser.class);
        com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
        when(browser.getBrowserType()).thenReturn(browserTypeObj);
        when(browserTypeObj.getRemoteBrowser()).thenReturn(mock(RemoteBrowser.class));

        BrowserType result = browserUtil.getBrowserType(browser);

        assertEquals(BrowserType.REMOTE, result);
    }

    @Test
    void getBrowserTypeReturnsInDockerWhenBrowserInDockerIsNotNull() {
        AbstractBrowser browser = mock(AbstractBrowser.class);
        com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
        when(browser.getBrowserType()).thenReturn(browserTypeObj);
        when(browserTypeObj.getRemoteBrowser()).thenReturn(null);
        when(browserTypeObj.getBrowserInDocker()).thenReturn(mock(BrowserInDocker.class));

        BrowserType result = browserUtil.getBrowserType(browser);

        assertEquals(BrowserType.IN_DOCKER, result);
    }

    @Test
    void getBrowserTypeReturnsBrowserStackWhenBrowserStackIsNotNull() {
        AbstractBrowser browser = mock(AbstractBrowser.class);
        com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
        when(browser.getBrowserType()).thenReturn(browserTypeObj);
        when(browserTypeObj.getRemoteBrowser()).thenReturn(null);
        when(browserTypeObj.getBrowserInDocker()).thenReturn(null);
        when(browserTypeObj.getBrowserStack()).thenReturn(mock(BrowserStackWeb.class));

        BrowserType result = browserUtil.getBrowserType(browser);

        assertEquals(BrowserType.BROWSER_STACK, result);
    }

    @Test
    void getBrowserTypeReturnsLocalWhenAllAreNull() {
        AbstractBrowser browser = mock(AbstractBrowser.class);
        com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
        when(browser.getBrowserType()).thenReturn(browserTypeObj);
        when(browserTypeObj.getRemoteBrowser()).thenReturn(null);
        when(browserTypeObj.getBrowserInDocker()).thenReturn(null);
        when(browserTypeObj.getBrowserStack()).thenReturn(null);

        BrowserType result = browserUtil.getBrowserType(browser);

        assertEquals(BrowserType.LOCAL, result);
    }

    @Test
    void getBrowserVersionReturnsRemoteVersion() {
        AbstractBrowser browser = mock(AbstractBrowser.class);
        com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
        RemoteBrowser remoteBrowser = mock(RemoteBrowser.class);
        when(browser.getBrowserType()).thenReturn(browserTypeObj);
        when(browserTypeObj.getRemoteBrowser()).thenReturn(remoteBrowser);
        when(remoteBrowser.getBrowserVersion()).thenReturn("100.0");

        String result = browserUtil.getBrowserVersion(browser, BrowserType.REMOTE);

        assertEquals("100.0", result);
    }

    @Test
    void getBrowserVersionReturnsInDockerVersion() {
        AbstractBrowser browser = mock(AbstractBrowser.class);
        com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
        BrowserInDocker dockerBrowser = mock(BrowserInDocker.class);
        when(browser.getBrowserType()).thenReturn(browserTypeObj);
        when(browserTypeObj.getBrowserInDocker()).thenReturn(dockerBrowser);
        when(dockerBrowser.getBrowserVersion()).thenReturn("99.0");

        String result = browserUtil.getBrowserVersion(browser, BrowserType.IN_DOCKER);

        assertEquals("99.0", result);
    }

    @Test
    void getBrowserVersionReturnsBrowserStackVersion() {
        AbstractBrowser browser = mock(AbstractBrowser.class);
        com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
        BrowserStackWeb bsBrowser = mock(BrowserStackWeb.class);
        when(browser.getBrowserType()).thenReturn(browserTypeObj);
        when(browserTypeObj.getBrowserStack()).thenReturn(bsBrowser);
        when(bsBrowser.getBrowserVersion()).thenReturn("98.0");

        String result = browserUtil.getBrowserVersion(browser, BrowserType.BROWSER_STACK);

        assertEquals("98.0", result);
    }

    @Test
    void getBrowserVersionReturnsLocalDriverVersion() {
        AbstractBrowser browser = mock(AbstractBrowser.class);
        com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
        LocalBrowser localBrowser = mock(LocalBrowser.class);
        when(browser.getBrowserType()).thenReturn(browserTypeObj);
        when(browserTypeObj.getLocalBrowser()).thenReturn(localBrowser);
        when(localBrowser.getDriverVersion()).thenReturn("97.0");

        String result = browserUtil.getBrowserVersion(browser, BrowserType.LOCAL);

        assertEquals("97.0", result);
    }

    @Test
    void getBrowserVersionReturnsDefaultMessageWhenLocalVersionIsBlank() {
        AbstractBrowser browser = mock(AbstractBrowser.class);
        com.knubisoft.testlum.testing.model.global_config.BrowserType browserTypeObj =
                mock(com.knubisoft.testlum.testing.model.global_config.BrowserType.class);
        LocalBrowser localBrowser = mock(LocalBrowser.class);
        when(browser.getBrowserType()).thenReturn(browserTypeObj);
        when(browserTypeObj.getLocalBrowser()).thenReturn(localBrowser);
        when(localBrowser.getDriverVersion()).thenReturn("");

        String result = browserUtil.getBrowserVersion(browser, BrowserType.LOCAL);

        assertEquals("No browser version specified (the latest version is used)", result);
    }

    @Test
    void browserTypeEnumValuesHaveCorrectTypeNames() {
        assertEquals("local browser", BrowserType.LOCAL.getTypeName());
        assertEquals("remote browser", BrowserType.REMOTE.getTypeName());
        assertEquals("browser in docker", BrowserType.IN_DOCKER.getTypeName());
        assertEquals("browserStack", BrowserType.BROWSER_STACK.getTypeName());
    }

    @Test
    void browserTypeEnumHasFourValues() {
        assertEquals(4, BrowserType.values().length);
    }
}
