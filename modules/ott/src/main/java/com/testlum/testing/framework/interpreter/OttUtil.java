package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.util.IntegrationsUtil;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Ott;
import com.testlum.testing.model.global_config.OttIntegration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
@Service
public class OttUtil {

    private static final long TIME_STEP_SECONDS = 30L;
    private static final int CODE_DIGITS = 6;
    private static final int CODE_DIGITS_LIMIT = (int) Math.pow(10, CODE_DIGITS);
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final Duration MIN_REMAINING_VALIDITY = Duration.ofSeconds(5L);
    private static final String BLANK_SECRET_KEY = "OTT secret key must not be blank";

    private static final int LAST_NIBBLE_MASK = 0xF;
    private static final int SIGN_BIT_MASK = 0x7F;
    private static final int BYTE_MASK = 0xFF;
    private static final int BYTE_3_SHIFT = 24;
    private static final int BYTE_2_SHIFT = 16;
    private static final int BYTE_1_SHIFT = 8;
    private static final int OFFSET_BYTE_1 = 1;
    private static final int OFFSET_BYTE_2 = 2;
    private static final int OFFSET_BYTE_3 = 3;

    private final Integrations integrations;
    private final IntegrationsUtil integrationsUtil;

    public String generateCode(final String alias) {
        String secretKey = resolveSecretKey(alias);
        return generateFreshCode(secretKey);
    }

    private String resolveSecretKey(final String alias) {
        OttIntegration ottIntegration = integrations.getOttIntegration();
        Ott ott = integrationsUtil.findForAlias(ottIntegration.getOtt(), alias);
        return ott.getSecretKey();
    }

    private String generateFreshCode(final String secretKey) {
        Instant now = Instant.now();
        if (validityLeft(now).compareTo(MIN_REMAINING_VALIDITY) < 0) {
            sleepUntilNextWindow(validityLeft(now));
            now = Instant.now();
        }
        return generate(secretKey, now);
    }

    private Duration validityLeft(final Instant time) {
        long secondsIntoStep = time.getEpochSecond() % TIME_STEP_SECONDS;
        return Duration.ofSeconds(TIME_STEP_SECONDS - secondsIntoStep);
    }

    private void sleepUntilNextWindow(final Duration remaining) {
        try {
            Thread.sleep(remaining.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DefaultFrameworkException(e);
        }
    }

    private String generate(final String secretKey, final Instant time) {
        long counter = time.getEpochSecond() / TIME_STEP_SECONDS;
        byte[] key = decodeSecret(secretKey);
        byte[] hash = hmacSha1(key, counter);
        int code = truncate(hash) % CODE_DIGITS_LIMIT;
        return String.format("%0" + CODE_DIGITS + "d", code);
    }

    private byte[] decodeSecret(final String secretKey) {
        if (StringUtils.isBlank(secretKey)) {
            throw new DefaultFrameworkException(BLANK_SECRET_KEY);
        }
        return new Base32().decode(secretKey.trim().toUpperCase());
    }

    private byte[] hmacSha1(final byte[] key, final long counter) {
        byte[] data = ByteBuffer.allocate(Long.BYTES).putLong(counter).array();
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            return mac.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private int truncate(final byte[] hash) {
        int offset = hash[hash.length - 1] & LAST_NIBBLE_MASK;
        return ((hash[offset] & SIGN_BIT_MASK) << BYTE_3_SHIFT)
                | ((hash[offset + OFFSET_BYTE_1] & BYTE_MASK) << BYTE_2_SHIFT)
                | ((hash[offset + OFFSET_BYTE_2] & BYTE_MASK) << BYTE_1_SHIFT)
                | (hash[offset + OFFSET_BYTE_3] & BYTE_MASK);
    }
}
