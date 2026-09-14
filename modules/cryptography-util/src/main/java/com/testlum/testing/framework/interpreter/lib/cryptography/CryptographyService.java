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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class CryptographyService {

    private static final String ACTION_ENCRYPT = "ENCRYPT";
    private static final String ACTION_DECRYPT = "DECRYPT";

    private static final String ALGO_AES = "AES";
    private static final String ALGO_CHACHA20 = "CHACHA20";

    private static final String TRANSFORMATION_AES = "AES/GCM/NoPadding";
    private static final String TRANSFORMATION_CHACHA = "ChaCha20-Poly1305";

    private static final int IV_SIZE_12_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private static final int KEY_16_BYTES = 16;
    private static final int KEY_24_BYTES = 24;
    private static final int KEY_32_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String processCommand(final String rawValue, final String action,
                                 final String method, final String secret, final String alias) {
        validateInputs(rawValue, action, method, secret, alias);
        final String normAction = action.trim().toUpperCase();
        final String normMethod = method.trim().toUpperCase();

        try {
            return dispatchMethod(rawValue, normAction, normMethod, secret);
        } catch (final Exception e) {
            log.error("Crypto operation [{}] failed for alias [{}] with method [{}]", action, alias, method, e);
            throw new DefaultFrameworkException(handleCryptoError(e, action, alias, method));
        }
    }

    private String dispatchMethod(final String val, final String act, final String mtd, final String sec)
            throws Exception {
        if (mtd.contains(ALGO_AES)) {
            return processCipher(val, act, sec, "AES", TRANSFORMATION_AES);
        }
        if (mtd.contains(ALGO_CHACHA20)) {
            return processCipher(val, act, sec, "ChaCha20", TRANSFORMATION_CHACHA);
        }
        throw new DefaultFrameworkException(
                String.format("Unsupported algorithm method '%s'. Supported methods: AES, CHACHA20.", mtd));
    }

    private String processCipher(final String rawValue, final String action, final String secret,
                                 final String algoName, final String transformation) throws Exception {
        final byte[] keyBytes = deriveSecretKeyBytes(secret, algoName);
        final SecretKey keySpec = new SecretKeySpec(keyBytes, algoName);
        final Cipher cipher = Cipher.getInstance(transformation);

        return ACTION_ENCRYPT.equals(action)
                ? encrypt(cipher, keySpec, rawValue, algoName)
                : decrypt(cipher, keySpec, rawValue, algoName);
    }

    private String encrypt(final Cipher cipher, final SecretKey key, final String rawValue, final String algoName)
            throws Exception {
        final byte[] iv = new byte[IV_SIZE_12_BYTES];
        secureRandom.nextBytes(iv);
        initCipher(cipher, Cipher.ENCRYPT_MODE, key, iv, algoName);

        final byte[] encrypted = cipher.doFinal(rawValue.getBytes(StandardCharsets.UTF_8));
        final byte[] encryptedWithIv = ByteBuffer.allocate(iv.length + encrypted.length)
                .put(iv)
                .put(encrypted)
                .array();

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    private String decrypt(final Cipher cipher, final SecretKey key, final String rawValue, final String algoName)
            throws Exception {
        final byte[] decodedInput = decodeBase64Input(rawValue);
        if (decodedInput.length < IV_SIZE_12_BYTES) {
            throw new DefaultFrameworkException("DECRYPT operation failed: Ciphertext is too short to extract IV.");
        }

        final ByteBuffer buffer = ByteBuffer.wrap(decodedInput);
        final byte[] iv = new byte[IV_SIZE_12_BYTES];
        buffer.get(iv);

        final byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        initCipher(cipher, Cipher.DECRYPT_MODE, key, iv, algoName);
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    private void initCipher(final Cipher cipher, final int mode, final SecretKey key,
                            final byte[] iv, final String algoName) throws Exception {
        if ("AES".equalsIgnoreCase(algoName)) {
            cipher.init(mode, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        } else {
            cipher.init(mode, key, new IvParameterSpec(iv));
        }
    }

    private byte[] deriveSecretKeyBytes(final String secret, final String algoName) {
        try {
            final byte[] decoded = Base64.getDecoder().decode(secret);
            if (isValidKeyLength(decoded.length, algoName)) {
                return decoded;
            }
        } catch (final IllegalArgumentException ignored) {
            log.trace("Secret is not Base64 encoded, falling back to SHA-256 derivation");
        }

        final byte[] rawBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (isValidKeyLength(rawBytes.length, algoName)) {
            return rawBytes;
        }
        return digestSha256(secret);
    }

    private boolean isValidKeyLength(final int length, final String algoName) {
        if ("ChaCha20".equalsIgnoreCase(algoName)) {
            return length == KEY_32_BYTES;
        }
        return length == KEY_16_BYTES || length == KEY_24_BYTES || length == KEY_32_BYTES; // AES 128, 192, 256
    }

    private byte[] digestSha256(final String text) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (final NoSuchAlgorithmException e) {
            throw new DefaultFrameworkException("SHA-256 algorithm unavailable in environment", e);
        }
    }

    private byte[] decodeBase64Input(final String rawValue) {
        try {
            return Base64.getDecoder().decode(rawValue);
        } catch (final IllegalArgumentException e) {
            throw new DefaultFrameworkException("DECRYPT operation failed: Input text is not a valid Base64 string.");
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

    private String handleCryptoError(final Exception e, final String action, final String alias, final String method) {
        if (e instanceof AEADBadTagException || e instanceof BadPaddingException) {
            return String.format("Failed to %s value for alias '%s' (%s). Decryption failed: "
                            + "invalid secret key or corrupted ciphertext.",
                    action.toLowerCase(), alias, method);
        }
        if (e instanceof InvalidKeyException) {
            return String.format("Invalid secret key specification for alias '%s' (%s). "
                    + "Ensure key size is valid (AES requires 128/192/256 bits,"
                    + " ChaCha20 requires 256 bits).", alias, method);
        }
        return (e instanceof DefaultFrameworkException) ? e.getMessage()
                : String.format("Failed to %s value using alias '%s' (%s): %s",
                action.toLowerCase(), alias, method, e.getMessage());
    }
}