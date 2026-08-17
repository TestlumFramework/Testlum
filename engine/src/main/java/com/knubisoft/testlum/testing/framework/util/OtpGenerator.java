package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

@Component
public class OtpGenerator {

    private static final String HMAC_ALGO = "HmacSHA1";
    private static final long TIME_STEP_MS = 30000L;
    private static final int MILLIS_IN_SECOND = 1000;
    private static final int WINDOW_SECONDS = 30;
    private static final int DELAY_SECONDS_THRESHOLD = 3;
    private static final int BUFFER_CAPACITY = 8;
    private static final int TRUNCATE_OFFSET_MASK = 0xF;
    private static final int TRUNCATE_LOOP_MAX = 4;
    private static final int BYTE_MASK = 0xFF;
    private static final int INT_MASK = 0x7FFFFFFF;
    private static final int MODULO = 1000000;
    private static final int SHIFT_BITS = 8;

    public String generateOtp(final String secretKey) {
        String sanitizedKey = sanitizeAndValidateBase32(secretKey);
        applyDelay();
        return computeTotp(sanitizedKey);
    }

    private String sanitizeAndValidateBase32(final String key) {
        String sanitized = key.replaceAll("\\s+", "").toUpperCase();
        if (!sanitized.matches("^[A-Z2-7=]+$")) {
            throw new DefaultFrameworkException("Invalid Base32 secretKey for OTP generation.");
        }
        return sanitized;
    }

    private void applyDelay() {
        long currentSeconds = System.currentTimeMillis() / MILLIS_IN_SECOND;
        long secondsRemaining = WINDOW_SECONDS - (currentSeconds % WINDOW_SECONDS);
        if (secondsRemaining <= DELAY_SECONDS_THRESHOLD) {
            try {
                Thread.sleep((secondsRemaining + 1) * MILLIS_IN_SECOND);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DefaultFrameworkException(e);
            }
        }
    }

    private String computeTotp(final String sanitizedKey) {
        try {
            byte[] decodedKey = new Base32().decode(sanitizedKey);
            long timeWindow = System.currentTimeMillis() / TIME_STEP_MS;
            byte[] data = ByteBuffer.allocate(BUFFER_CAPACITY).putLong(timeWindow).array();

            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(decodedKey, HMAC_ALGO));
            byte[] hash = mac.doFinal(data);

            return truncateHash(hash);
        } catch (Exception e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
    }

    private String truncateHash(final byte[] hash) {
        int offset = hash[hash.length - 1] & TRUNCATE_OFFSET_MASK;
        long truncatedHash = 0;
        for (int i = 0; i < TRUNCATE_LOOP_MAX; ++i) {
            truncatedHash <<= SHIFT_BITS;
            truncatedHash |= hash[offset + i] & BYTE_MASK;
        }
        truncatedHash &= INT_MASK;
        truncatedHash %= MODULO;
        return String.format("%06d", truncatedHash);
    }
}
