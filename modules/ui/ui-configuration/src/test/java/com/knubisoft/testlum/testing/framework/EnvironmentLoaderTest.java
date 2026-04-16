package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnvironmentLoaderTest {

    private UIConfiguration uiConfigMap;
    private EnvironmentLoader environmentLoader;

    @BeforeEach
    void setUp() {
        uiConfigMap = new UIConfiguration(new HashMap<>());
        environmentLoader = new EnvironmentLoader(uiConfigMap);
    }

    @Test
    void getWebSettingsReturnsWebWhenConfigExists() {
        Web web = mock(Web.class);
        UiConfig uiConfig = mock(UiConfig.class);
        when(uiConfig.getWeb()).thenReturn(web);
        uiConfigMap.put("dev", uiConfig);

        Optional<Web> result = environmentLoader.getWebSettings("dev");

        assertTrue(result.isPresent());
        assertEquals(web, result.get());
    }

    @Test
    void getWebSettingsReturnsEmptyWhenEnvNotInMap() {
        Optional<Web> result = environmentLoader.getWebSettings("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void getWebSettingsReturnsEmptyWhenUiConfigHasNullWeb() {
        UiConfig uiConfig = mock(UiConfig.class);
        when(uiConfig.getWeb()).thenReturn(null);
        uiConfigMap.put("dev", uiConfig);

        Optional<Web> result = environmentLoader.getWebSettings("dev");

        assertTrue(result.isEmpty());
    }

    @Test
    void getMobileBrowserSettingsReturnsMobileBrowserWhenConfigExists() {
        Mobilebrowser mobilebrowser = mock(Mobilebrowser.class);
        UiConfig uiConfig = mock(UiConfig.class);
        when(uiConfig.getMobilebrowser()).thenReturn(mobilebrowser);
        uiConfigMap.put("staging", uiConfig);

        Optional<Mobilebrowser> result = environmentLoader.getMobileBrowserSettings("staging");

        assertTrue(result.isPresent());
        assertEquals(mobilebrowser, result.get());
    }

    @Test
    void getMobileBrowserSettingsReturnsEmptyWhenEnvNotInMap() {
        Optional<Mobilebrowser> result = environmentLoader.getMobileBrowserSettings("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void getMobileBrowserSettingsReturnsEmptyWhenUiConfigHasNullMobileBrowser() {
        UiConfig uiConfig = mock(UiConfig.class);
        when(uiConfig.getMobilebrowser()).thenReturn(null);
        uiConfigMap.put("dev", uiConfig);

        Optional<Mobilebrowser> result = environmentLoader.getMobileBrowserSettings("dev");

        assertTrue(result.isEmpty());
    }

    @Test
    void getNativeSettingsReturnsNativeWhenConfigExists() {
        Native nativeSettings = mock(Native.class);
        UiConfig uiConfig = mock(UiConfig.class);
        when(uiConfig.getNative()).thenReturn(nativeSettings);
        uiConfigMap.put("prod", uiConfig);

        Optional<Native> result = environmentLoader.getNativeSettings("prod");

        assertTrue(result.isPresent());
        assertEquals(nativeSettings, result.get());
    }

    @Test
    void getNativeSettingsReturnsEmptyWhenEnvNotInMap() {
        Optional<Native> result = environmentLoader.getNativeSettings("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void getNativeSettingsReturnsEmptyWhenUiConfigHasNullNative() {
        UiConfig uiConfig = mock(UiConfig.class);
        when(uiConfig.getNative()).thenReturn(null);
        uiConfigMap.put("dev", uiConfig);

        Optional<Native> result = environmentLoader.getNativeSettings("dev");

        assertTrue(result.isEmpty());
    }
}
