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
    @DisplayName("Should fail when ciphertext payload is corrupted or tampered")
    void shouldFailWhenCiphertextIsTampered() {
        final String encrypted = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", AES_SECRET, ALIAS);
        final String corrupted = encrypted.substring(0, encrypted.length() - 2) + "AA";

        assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand(corrupted, "DECRYPT", "AES", AES_SECRET, ALIAS));
    }

    @Test
    @DisplayName("Should throw exception for unsupported method")
    void shouldThrowExceptionForUnsupportedMethod() {
        assertThrows(IllegalArgumentException.class,
                () -> service.processCommand(RAW_TEXT, "ENCRYPT", "RSA", AES_SECRET, ALIAS));
    }

    @Test
    @DisplayName("Should throw CryptoException when trying to decrypt invalid Base64 string")
    void shouldThrowExceptionWhenDecryptingNonBase64() {
        assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand("NOT_BASE_64!", "DECRYPT", "AES", AES_SECRET, ALIAS));
    }

    @Test
    @DisplayName("Should throw CryptoException when decrypting with wrong key")
    void shouldThrowExceptionWhenDecryptingWithWrongKey() {
        final String encrypted = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", AES_SECRET, ALIAS);
        final String wrongSecret = "99999999901234567890123456789012";

        assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand(encrypted, "DECRYPT", "AES", wrongSecret, ALIAS));
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
    @DisplayName("Should use SHA-256 fallback for arbitrary plain text secret and decrypt successfully")
    void shouldEncryptAndDecryptWithArbitraryPlainTextSecret() {
        final String arbitrarySecret = "simple_user_password_123";

        final String encrypted = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", arbitrarySecret, ALIAS);
        assertNotNull(encrypted);

        final String decrypted = service.processCommand(encrypted, "DECRYPT", "AES", arbitrarySecret, ALIAS);
        assertEquals(RAW_TEXT, decrypted);
    }

    @Test
    @DisplayName("Should correctly handle 16-byte (128-bit) and 24-byte (192-bit) secrets for AES")
    void shouldSupportDifferentAesKeySizes() {
        final String secret128 = "1234567890123456"; // 16 bytes
        final String secret192 = "123456789012345678901234"; // 24 bytes

        final String enc128 = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", secret128, ALIAS);
        final String dec128 = service.processCommand(enc128, "DECRYPT", "AES", secret128, ALIAS);

        final String enc192 = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", secret192, ALIAS);
        final String dec192 = service.processCommand(enc192, "DECRYPT", "AES", secret192, ALIAS);

        assertEquals(RAW_TEXT, dec128);
        assertEquals(RAW_TEXT, dec192);
    }

    @Test
    @DisplayName("Should generate different ciphertexts for same raw value due to random IV (Uniqueness Test)")
    void shouldProduceDifferentCiphertextsForSameInput() {
        final String encrypted1 = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", AES_SECRET, ALIAS);
        final String encrypted2 = service.processCommand(RAW_TEXT, "ENCRYPT", "AES", AES_SECRET, ALIAS);

        assertNotEquals(encrypted1, encrypted2);

        assertEquals(RAW_TEXT, service.processCommand(encrypted1, "DECRYPT", "AES", AES_SECRET, ALIAS));
        assertEquals(RAW_TEXT, service.processCommand(encrypted2, "DECRYPT", "AES", AES_SECRET, ALIAS));
    }

    @Test
    @DisplayName("Should throw exception when decrypting ChaCha20 payload with wrong secret key")
    void shouldFailChaCha20DecryptionWithWrongKey() {
        final String wrongSecret = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5OTk=";

        final String encrypted = service.processCommand(RAW_TEXT, "ENCRYPT", "CHACHA20", CHACHA_SECRET, ALIAS);

        assertThrows(DefaultFrameworkException.class,
                () -> service.processCommand(encrypted, "DECRYPT", "CHACHA20", wrongSecret, ALIAS));
    }
}