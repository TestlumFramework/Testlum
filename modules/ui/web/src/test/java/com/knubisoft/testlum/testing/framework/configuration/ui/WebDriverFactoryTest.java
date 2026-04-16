package com.knubisoft.testlum.testing.framework.configuration.ui;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.UIConfiguration;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.SeleniumDriverUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class WebDriverFactoryTest {

    @Mock
    private SeleniumDriverUtil seleniumDriverUtil;

    @Mock
    private EnvironmentLoader environmentLoader;

    @Mock
    private BrowserUtil browserUtil;

    @Mock
    private UIConfiguration uiConfigs;

    @InjectMocks
    private WebDriverFactory webDriverFactory;

    @Nested
    class Initialization {
        @Test
        void factoryIsCreated() {
            assertNotNull(webDriverFactory);
        }
    }
}
