package com.testlum.testing.framework.interpreter.lib.cryptography;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class CryptographyService {

    private static final String ACTION_ENCRYPT = "ENCRYPT";
    private static final String ACTION_DECRYPT = "DECRYPT";
    private static final int IV_NONCE_SIZE = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_SIZE_128_BITS = 16;
    private static final int KEY_SIZE_192_BITS = 24;
    private static final int KEY_SIZE_256_BITS = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String processCommand(final String rawValue, final String action,
                                 final String method, final String secret, final String alias) {
        validateInputs(rawValue, action, method, secret, alias);
        final String normAction = action.toUpperCase().trim();
        final String normMethod = method.toUpperCase().trim();

        try {
            return dispatchMethod(rawValue, normAction, normMethod, secret);
        } catch (final Exception e) {
            log.error("Crypto operation [{}] failed for alias [{}] with method [{}]", action, alias, method, e);
            throw new DefaultFrameworkException(handleCryptoError(e, action, alias, method));
        }
    }

    private String dispatchMethod(final String val, final String act, final String mtd, final String sec)
            throws Exception {
        if (mtd.contains("AES")) {
            return processCipher(val, act, sec, "AES", "AES/GCM/NoPadding", true);
        }
        if (mtd.contains("CHACHA20")) {
            return processCipher(val, act, sec, "ChaCha20", "ChaCha20-Poly1305", false);
        }
        throw new DefaultFrameworkException(
                String.format("Unsupported algorithm method '%s'. Supported: AES, CHACHA20.", mtd));
    }

    private String processCipher(final String rawValue, final String action, final String secret,
                                 final String algoName, final String transformation, final boolean isGcm)
            throws Exception {
        final byte[] keyBytes = getSecretBytes(secret);
        final SecretKey keySpec = new SecretKeySpec(keyBytes, algoName);
        final Cipher cipher = Cipher.getInstance(transformation);

        if (ACTION_ENCRYPT.equals(action)) {
            return encrypt(cipher, keySpec, rawValue, isGcm);
        }
        return decrypt(cipher, keySpec, rawValue, isGcm);
    }

    private String encrypt(final Cipher cipher, final SecretKey key, final String rawValue, final boolean isGcm)
            throws Exception {
        final byte[] iv = new byte[IV_NONCE_SIZE];
        secureRandom.nextBytes(iv);
        initCipher(cipher, Cipher.ENCRYPT_MODE, key, iv, isGcm);
        final byte[] encrypted = cipher.doFinal(rawValue.getBytes(StandardCharsets.UTF_8));

        final byte[] encryptedWithIv = ByteBuffer.allocate(iv.length + encrypted.length)
                .put(iv)
                .put(encrypted)
                .array();
        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    private String decrypt(final Cipher cipher, final SecretKey key, final String rawValue, final boolean isGcm)
            throws Exception {
        final byte[] decodedInput = decodeBase64Input(rawValue);
        if (decodedInput.length < IV_NONCE_SIZE) {
            throw new DefaultFrameworkException("DECRYPT operation failed: Ciphertext is too short.");
        }
        final ByteBuffer buffer = ByteBuffer.wrap(decodedInput);
        final byte[] iv = new byte[IV_NONCE_SIZE];
        buffer.get(iv);

        final byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);
        initCipher(cipher, Cipher.DECRYPT_MODE, key, iv, isGcm);
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    private void initCipher(final Cipher cipher, final int mode, final SecretKey key,
                            final byte[] iv, final boolean isGcm) throws Exception {
        if (isGcm) {
            cipher.init(mode, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        } else {
            cipher.init(mode, key, new IvParameterSpec(iv));
        }
    }

    private byte[] decodeBase64Input(final String rawValue) {
        try {
            return Base64.getDecoder().decode(rawValue);
        } catch (final IllegalArgumentException e) {
            throw new DefaultFrameworkException("DECRYPT operation failed: Input text is not a valid Base64 "
                    + "string. Ensure you are passing encrypted Base64 content.");
        }
    }

    private void validateInputs(final String rawValue, final String action,
                                final String method, final String secret, final String alias) {
        if (StringUtils.isAnyBlank(rawValue, action, method, secret, alias)) {
            throw new DefaultFrameworkException("Parameters 'rawValue', 'action', 'method', 'secret' and 'alias' "
                    + "must not be empty.");
        }
        final String normAction = action.trim();
        if (!ACTION_ENCRYPT.equalsIgnoreCase(normAction) && !ACTION_DECRYPT.equalsIgnoreCase(normAction)) {
            throw new DefaultFrameworkException(String.format("Unsupported cryptography action '%s'."
                    + " Use ENCRYPT or DECRYPT.", action));
        }
    }

    private byte[] getSecretBytes(final String secret) {
        try {
            final byte[] decoded = Base64.getDecoder().decode(secret);
            if (isValidKeyLength(decoded.length)) {
                return decoded;
            }
        } catch (final IllegalArgumentException ignored) {

        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    private boolean isValidKeyLength(final int length) {
        return length == KEY_SIZE_128_BITS || length == KEY_SIZE_192_BITS || length == KEY_SIZE_256_BITS;
    }

    private String handleCryptoError(final Exception e, final String action, final String alias, final String method) {
        if (e instanceof AEADBadTagException || e instanceof BadPaddingException) {
            return String.format("Failed to %s input text using alias '%s' (%s). Decryption failed: key or input "
                            + "data is invalid. Try changing the ALIAS or check the secret key.",
                    action.toLowerCase(), alias, method);
        }
        if (e instanceof InvalidKeyException) {
            return String.format("Invalid key specification for alias '%s' (%s). "
                    + "Verify that 'secret' in config matches expected format.", alias, method);
        }
        return (e instanceof DefaultFrameworkException) ? e.getMessage()
                : String.format("Failed to %s value using alias '%s' (%s): %s",
                action.toLowerCase(), alias, method, e.getMessage());
    }
}