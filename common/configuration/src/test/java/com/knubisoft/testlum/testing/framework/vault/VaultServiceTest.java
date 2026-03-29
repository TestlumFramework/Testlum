package com.knubisoft.testlum.testing.framework.vault;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VaultServiceTest {

    private VaultTemplate vaultTemplate;
    private VaultService vaultService;

    @BeforeEach
    void setUp() {
        vaultTemplate = mock(VaultTemplate.class);
        vaultService = new VaultService(vaultTemplate);
    }

    @Nested
    class Inject {
        @Test
        void blankInputReturnsAsIs() {
            assertNull(vaultService.inject(null));
            assertEquals("", vaultService.inject(""));
            assertEquals("   ", vaultService.inject("   "));
        }

        @Test
        void noPlaceholdersReturnsUnchanged() {
            assertEquals("plain text", vaultService.inject("plain text"));
        }

        @Test
        void replacesVaultPlaceholder() {
            final VaultResponse response = new VaultResponse();
            response.setData(Map.of("data", Map.of("password", "s3cret")));
            when(vaultTemplate.read("secret/myapp")).thenReturn(response);

            final String result = vaultService.inject("pwd={{secret/myapp.password}}");
            assertEquals("pwd=s3cret", result);
        }

        @Test
        void replacesMultiplePlaceholders() {
            final VaultResponse resp1 = new VaultResponse();
            resp1.setData(Map.of("data", Map.of("user", "admin")));
            when(vaultTemplate.read("secret/db")).thenReturn(resp1);

            final VaultResponse resp2 = new VaultResponse();
            resp2.setData(Map.of("data", Map.of("pass", "pw123")));
            when(vaultTemplate.read("secret/db2")).thenReturn(resp2);

            final String result = vaultService.inject(
                    "u={{secret/db.user}}&p={{secret/db2.pass}}");
            assertEquals("u=admin&p=pw123", result);
        }

        @Test
        void throwsWhenPathNotFound() {
            when(vaultTemplate.read("secret/missing")).thenReturn(null);
            assertThrows(DefaultFrameworkException.class,
                    () -> vaultService.inject("{{secret/missing.key}}"));
        }

        @Test
        void throwsWhenKeyNotFound() {
            final VaultResponse response = new VaultResponse();
            response.setData(Map.of("data", Map.of("other", "value")));
            when(vaultTemplate.read("secret/app")).thenReturn(response);
            assertThrows(DefaultFrameworkException.class,
                    () -> vaultService.inject("{{secret/app.missing}}"));
        }
    }

    @Nested
    class VaultDtoTest {
        @Test
        void storesKeyAndValue() {
            final VaultService.VaultDto dto = new VaultService.VaultDto("k", "v");
            assertEquals("k", dto.getKey());
            assertEquals("v", dto.getValue());
        }
    }
}
