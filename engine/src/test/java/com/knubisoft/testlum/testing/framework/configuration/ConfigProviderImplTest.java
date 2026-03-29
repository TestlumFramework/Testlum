package com.knubisoft.testlum.testing.framework.configuration;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.Web;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigProviderImplTest {

    @Mock
    private EnvironmentLoader loader;

    @InjectMocks
    private ConfigProviderImpl configProvider;

    @Test
    void getWebSettingsReturnsWebWhenPresent() {
        Web web = mock(Web.class);
        when(loader.getWebSettings("dev")).thenReturn(Optional.of(web));

        Web result = configProvider.getWebSettings("dev");

        assertEquals(web, result);
    }

    @Test
    void getWebSettingsThrowsExceptionWhenEmpty() {
        when(loader.getWebSettings("dev")).thenReturn(Optional.empty());

        assertThrows(DefaultFrameworkException.class, () -> configProvider.getWebSettings("dev"));
    }

    @Test
    void getMobileBrowserSettingsReturnsMobileBrowserWhenPresent() {
        Mobilebrowser mobilebrowser = mock(Mobilebrowser.class);
        when(loader.getMobileBrowserSettings("staging")).thenReturn(Optional.of(mobilebrowser));

        Mobilebrowser result = configProvider.getMobileBrowserSettings("staging");

        assertEquals(mobilebrowser, result);
    }

    @Test
    void getMobileBrowserSettingsThrowsExceptionWhenEmpty() {
        when(loader.getMobileBrowserSettings("staging")).thenReturn(Optional.empty());

        assertThrows(DefaultFrameworkException.class, () -> configProvider.getMobileBrowserSettings("staging"));
    }

    @Test
    void getNativeSettingsReturnsNativeWhenPresent() {
        Native nativeSettings = mock(Native.class);
        when(loader.getNativeSettings("prod")).thenReturn(Optional.of(nativeSettings));

        Native result = configProvider.getNativeSettings("prod");

        assertEquals(nativeSettings, result);
    }

    @Test
    void getNativeSettingsThrowsExceptionWhenEmpty() {
        when(loader.getNativeSettings("prod")).thenReturn(Optional.empty());

        assertThrows(DefaultFrameworkException.class, () -> configProvider.getNativeSettings("prod"));
    }
}
