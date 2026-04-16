package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigUtilTest {

    @Mock
    private GlobalTestConfiguration globalTestConfiguration;

    @InjectMocks
    private ConfigUtil configUtil;

    @Test
    void shouldThrowDefaultFrameworkExceptionWhenStopOnFailureIsTrue() {
        when(globalTestConfiguration.isStopScenarioOnFailure()).thenReturn(true);
        RuntimeException originalException = new RuntimeException("original error");

        DefaultFrameworkException thrown = assertThrows(DefaultFrameworkException.class,
                () -> configUtil.checkIfStopScenarioOnFailure(originalException));
        assertEquals(originalException, thrown.getCause());
    }

    @Test
    void shouldNotThrowWhenStopOnFailureIsFalse() {
        when(globalTestConfiguration.isStopScenarioOnFailure()).thenReturn(false);
        RuntimeException originalException = new RuntimeException("original error");

        assertDoesNotThrow(() -> configUtil.checkIfStopScenarioOnFailure(originalException));
    }

    @Test
    void shouldVerifyConfigurationIsChecked() {
        when(globalTestConfiguration.isStopScenarioOnFailure()).thenReturn(false);

        configUtil.checkIfStopScenarioOnFailure(new RuntimeException());

        verify(globalTestConfiguration).isStopScenarioOnFailure();
    }
}
