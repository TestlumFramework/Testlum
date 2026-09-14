package com.testlum.testing.framework.interpreter.lib.cryptography;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptographyServiceTest {

    private static final String ALIAS = "TEST_ALIAS";
    private static final String AES_SECRET = "12345678901234567890123456789012";
    private static final String CHACHA_SECRET = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";
    private static final String RAW_TEXT = "Testlum_Ultimate_Crypto_Payload_2026_$%&!";

    private CryptographyService service;

    @BeforeEach
    void setUp() {
        service = new CryptographyService();
    }

    @ParameterizedTest
    @DisplayName("Should successfully encrypt and decrypt data (Roundtrip test)")
    @ValueSource(strings = {"AES", "CHACHA20"})
    void shouldEncryptAndDecryptSymmetricAlgorithms(final String method) {
        final String secret = "AES".equals(method) ? AES_SECRET : CHACHA_SECRET;

        final String encrypted = service.processCommand(RAW_TEXT, "ENCRYPT", method, secret, ALIAS);
        assertNotNull(encrypted);
        assertNotEquals(RAW_TEXT, encrypted);

        final String decrypted = service.processCommand(encrypted, "DECRYPT", method, secret, ALIAS);
        assertEquals(RAW_TEXT, decrypted);
    }

    @ParameterizedTest
    @DisplayName("Should handle case-insensitivity and extra whitespaces in action and method")
    @CsvSource({
            " encrypt ,  aes ",
            " DeCrYpT ,  ChaCha20 "
    })
    void shouldHandleCaseAndWhitespaceInActionAndMethod(final String action, final String method) {
        final String secret = method.toUpperCase().contains("CHACHA20") ? CHACHA_SECRET : AES_SECRET;

        final String encrypted = service.processCommand(RAW_TEXT, "encrypt", method, secret, ALIAS);
        final String decrypted = service.processCommand(encrypted, "DECRYPT", method, secret, ALIAS);

        assertEquals(RAW_TEXT, decrypted);
    }

    @ParameterizedTest
    @DisplayName("Should correctly handle complex Unicode payloads, emojis and multiline texts")
    @ValueSource(strings = {
            "Привет мир! 🚀🔥 KEY_2026",
            "Line1\nLine2\r\nLine3\tTabbed",
            "中国語テスト - Japanese: テスト - Arabic: اختبار"
    })
    void shouldEncryptAndDecryptComplexUnicodePayloads(final String payload) {
        final String encrypted = service.processCommand(payload, "ENCRYPT", "AES", AES_SECRET, ALIAS);
        final String decrypted = service.processCommand(encrypted, "DECRYPT", "AES", AES_SECRET, ALIAS);

        assertEquals(payload, decrypted);
    }

    @Test
    @DisplayName("Should fail when ciphertext payload is corrupted or tampered (GCM/Poly1305 Tag Failure)")
    void shouldFailWhenCiphertextIsTampered() {
        final String encrypted = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", AES_SECRET, ALIAS);
        final String corrupted = encrypted.substring(0, encrypted.length() - 2) + "AA";

        final DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand(corrupted, "DECRYPT", "AES", AES_SECRET, ALIAS));
        assertTrue(ex.getMessage().contains("Try changing the ALIAS or check the secret key"));
    }

    @ParameterizedTest
    @DisplayName("Should throw DefaultFrameworkException when any mandatory input parameter is blank")
    @CsvSource({
            "'', ENCRYPT, AES, secret",
            "val, '', AES, secret",
            "val, ENCRYPT, '', secret",
            "val, ENCRYPT, AES, ''"
    })
    void shouldThrowExceptionWhenParametersAreBlank(final String val, final String act,
                                                    final String mtd, final String sec) {
        final DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand(val, act, mtd, sec, ALIAS));
        assertTrue(ex.getMessage().contains("must not be empty"));
    }

    @Test
    @DisplayName("Should throw exception for unsupported action")
    void shouldThrowExceptionForUnsupportedAction() {
        final DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand(RAW_TEXT, "SIGN", "AES", AES_SECRET, ALIAS));
        assertTrue(ex.getMessage().contains("Unsupported cryptography action 'SIGN'"));
    }

    @Test
    @DisplayName("Should throw exception for unsupported method")
    void shouldThrowExceptionForUnsupportedMethod() {
        final DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand(RAW_TEXT, "ENCRYPT", "RSA", AES_SECRET, ALIAS));
        assertTrue(ex.getMessage().contains("Unsupported algorithm method 'RSA'"));
    }

    @Test
    @DisplayName("Should throw error with Base64 hint when trying to decrypt invalid Base64 string")
    void shouldThrowExceptionWhenDecryptingNonBase64() {
        final DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand("NOT_BASE_64!", "DECRYPT", "AES", AES_SECRET, ALIAS));
        assertTrue(ex.getMessage().contains("Input text is not a valid Base64 string"));
    }

    @Test
    @DisplayName("Should throw error with ALIAS suggestion when decrypting with wrong key")
    void shouldThrowExceptionWhenDecryptingWithWrongKey() {
        final String encrypted = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", AES_SECRET, ALIAS);
        final String wrongSecret = "99999999901234567890123456789012";

        final DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand(encrypted, "DECRYPT", "AES", wrongSecret, ALIAS));
        assertTrue(ex.getMessage().contains("Try changing the ALIAS or check the secret key"));
    }

    @Test
    @DisplayName("Should throw error with Key Specification hint when key format is invalid")
    void shouldThrowExceptionWhenKeyFormatIsInvalid() {
        final String invalidKey = "ShortKey";

        final DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand(RAW_TEXT, "ENCRYPT", "AES", invalidKey, ALIAS));
        assertTrue(ex.getMessage().contains("Invalid key specification for alias")
                || ex.getMessage().contains("Try changing the ALIAS"));
    }

    @Test
    @DisplayName("Should decrypt data using ALIAS_2 that was encrypted by ALIAS_1 with same secret")
    void shouldDecryptDataFromDifferentAliasWithSameSecret() {
        final String alias1 = "PRODUCER_ALIAS";
        final String alias2 = "CONSUMER_ALIAS";

        final String encrypted = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", AES_SECRET, alias1);
        final String decrypted = service.processCommand(encrypted, "DECRYPT", "AES", AES_SECRET, alias2);

        assertEquals(RAW_TEXT, decrypted);
    }

    @Test
    @DisplayName("Should fail with user-friendly error when ALIAS_2 has different secret key than ALIAS_1")
    void shouldFailWhenDifferentAliasHasDifferentSecretKey() {
        final String senderAlias = "SENDER_SERVICE";
        final String receiverAlias = "RECEIVER_SERVICE";
        final String wrongSecret = "99999999901234567890123456789012";

        final String encrypted = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", AES_SECRET, senderAlias);

        final DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand(encrypted, "DECRYPT", "AES", wrongSecret, receiverAlias));

        assertTrue(ex.getMessage().contains("RECEIVER_SERVICE"));
        assertTrue(ex.getMessage().contains("Try changing the ALIAS or check the secret key"));
    }
}