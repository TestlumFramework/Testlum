package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.util.IntegrationsUtil;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Ott;
import com.testlum.testing.model.global_config.OttIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OttUtilTest {

    private static final String RFC_SEED_BASE32 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
    private static final String RFC_SEED_ASCII = "12345678901234567890";

    private final Integrations integrations = mock(Integrations.class);
    private final IntegrationsUtil integrationsUtil = mock(IntegrationsUtil.class);
    private final OttUtil ottUtil = new OttUtil(integrations, integrationsUtil);

    @Nested
    class GenerateCode {

        @Test
        void resolvesSecretByAliasAndReturnsSixDigitCode() {
            final Ott ottModel = mock(Ott.class);
            when(ottModel.getSecretKey()).thenReturn("JBSWY3DPEHPK3PXP");
            final List<Ott> ottList = List.of(ottModel);
            final OttIntegration ottIntegration = mock(OttIntegration.class);
            when(ottIntegration.getOtt()).thenReturn(ottList);
            when(integrations.getOttIntegration()).thenReturn(ottIntegration);
            when(integrationsUtil.findForAlias(ottList, "myAlias")).thenReturn(ottModel);

            final String code = ottUtil.generateCode("myAlias");

            assertTrue(code.matches("\\d{6}"), "code should be exactly 6 digits, got: " + code);
            verify(integrationsUtil).findForAlias(ottList, "myAlias");
        }

        @Test
        void throwsWhenSecretKeyIsBlank() {
            final Ott ottModel = mock(Ott.class);
            when(ottModel.getSecretKey()).thenReturn("   ");
            final List<Ott> ottList = List.of(ottModel);
            final OttIntegration ottIntegration = mock(OttIntegration.class);
            when(ottIntegration.getOtt()).thenReturn(ottList);
            when(integrations.getOttIntegration()).thenReturn(ottIntegration);
            when(integrationsUtil.findForAlias(ottList, "blankAlias")).thenReturn(ottModel);

            final DefaultFrameworkException exception =
                    assertThrows(DefaultFrameworkException.class, () -> ottUtil.generateCode("blankAlias"));

            assertEquals("OTT secret key must not be blank", exception.getMessage());
        }
    }

    @Nested
    class Generate {

        @ParameterizedTest(name = "epochSecond={0} -> code={1}")
        @CsvSource({
                "59, 287082",
                "1111111109, 081804",
                "1111111111, 050471",
                "1234567890, 005924",
                "2000000000, 279037",
                "20000000000, 353130"
        })
        void matchesRfc6238TestVectors(final long epochSecond, final String expectedCode) throws Exception {
            final Method method = OttUtil.class.getDeclaredMethod("generate", String.class, Instant.class);
            method.setAccessible(true);

            final String actualCode = (String) method.invoke(
                    ottUtil, RFC_SEED_BASE32, Instant.ofEpochSecond(epochSecond)
            );

            assertEquals(expectedCode, actualCode);
        }
    }

    @Nested
    class DecodeSecret {

        @Test
        void decodesBase32SecretToOriginalBytes() throws Exception {
            final Method method = OttUtil.class.getDeclaredMethod("decodeSecret", String.class);
            method.setAccessible(true);

            final byte[] decoded = (byte[]) method.invoke(ottUtil, RFC_SEED_BASE32);

            assertArrayEquals(RFC_SEED_ASCII.getBytes(StandardCharsets.US_ASCII), decoded);
        }

        @Test
        void isCaseInsensitiveAndTrimsWhitespace() throws Exception {
            final Method method = OttUtil.class.getDeclaredMethod("decodeSecret", String.class);
            method.setAccessible(true);

            final byte[] decoded = (byte[]) method.invoke(ottUtil, "  " + RFC_SEED_BASE32.toLowerCase() + "  ");

            assertArrayEquals(RFC_SEED_ASCII.getBytes(StandardCharsets.US_ASCII), decoded);
        }

        @Test
        void throwsWhenSecretIsBlank() throws Exception {
            final Method method = OttUtil.class.getDeclaredMethod("decodeSecret", String.class);
            method.setAccessible(true);

            final InvocationTargetException wrapper =
                    assertThrows(InvocationTargetException.class, () -> method.invoke(ottUtil, "  "));

            assertInstanceOf(DefaultFrameworkException.class, wrapper.getCause());
            assertEquals("OTT secret key must not be blank", wrapper.getCause().getMessage());
        }
    }
}
