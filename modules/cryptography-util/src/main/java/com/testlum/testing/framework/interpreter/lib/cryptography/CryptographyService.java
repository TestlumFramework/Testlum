package com.testlum.testing.framework.interpreter.lib.cryptography;

import com.testlum.testing.framework.exception.CryptoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class CryptographyService {

    private static final int IV_SIZE_BYTES = 12;
    private static final List<Integer> VALID_KEY_SIZES = List.of(16, 24, 32);
    private final SecureRandom secureRandom = new SecureRandom();

    public String processCommand(final String rawValue, final String action,
                                 final String method, final String secret, final String alias) {
        final CryptoAlgorithm algo = CryptoAlgorithm.parse(method);
        final SecretKey key = deriveSecretKey(secret, algo.getKeyAlgorithm());
        try {
            return "ENCRYPT".equalsIgnoreCase(action)
                    ? encrypt(rawValue, key, algo)
                    : decrypt(rawValue, key, algo);
        } catch (final Exception e) {
            log.error("Crypto operation [{}] failed for alias [{}] with method [{}]", action, alias, method, e);
            throw new CryptoException(String.format("Crypto operation [%s] failed for alias [%s]", action, alias), e);
        }
    }

    private String encrypt(final String rawValue, final SecretKey key, final CryptoAlgorithm algo)
            throws GeneralSecurityException {
        final byte[] iv = new byte[IV_SIZE_BYTES];
        secureRandom.nextBytes(iv);

        final Cipher cipher = Cipher.getInstance(algo.getTransformation());
        cipher.init(Cipher.ENCRYPT_MODE, key, algo.createParameterSpec(iv));

        final byte[] encrypted = cipher.doFinal(rawValue.getBytes(StandardCharsets.UTF_8));
        final byte[] result = ByteBuffer.allocate(iv.length + encrypted.length)
                .put(iv)
                .put(encrypted)
                .array();

        return Base64.getEncoder().encodeToString(result);
    }

    private String decrypt(final String rawValue, final SecretKey key, final CryptoAlgorithm algo)
            throws GeneralSecurityException {
        final byte[] decodedInput = Base64.getDecoder().decode(rawValue);
        final ByteBuffer buffer = ByteBuffer.wrap(decodedInput);

        final byte[] iv = new byte[IV_SIZE_BYTES];
        buffer.get(iv);

        final byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        final Cipher cipher = Cipher.getInstance(algo.getTransformation());
        cipher.init(Cipher.DECRYPT_MODE, key, algo.createParameterSpec(iv));

        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    private SecretKey deriveSecretKey(final String secret, final String algoName) {
        final byte[] keyBytes = tryDecodeBase64(secret)
                .filter(bytes -> VALID_KEY_SIZES.contains(bytes.length))
                .orElseGet(() -> digestSha256(secret));

        return new SecretKeySpec(keyBytes, algoName);
    }

    private Optional<byte[]> tryDecodeBase64(final String text) {
        try {
            return Optional.of(Base64.getDecoder().decode(text));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private byte[] digestSha256(final String text) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}