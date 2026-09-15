package com.testlum.testing.framework.interpreter.lib.cryptography;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.spec.AlgorithmParameterSpec;

@Getter
@RequiredArgsConstructor
public enum CryptoAlgorithm {
    AES("AES", "AES/GCM/NoPadding", 128),
    CHACHA20("ChaCha20", "ChaCha20-Poly1305", 0);

    private final String keyAlgorithm;
    private final String transformation;
    private final int tagLengthBits;

    public static CryptoAlgorithm parse(final String method) {
        if (method.toUpperCase().contains("CHACHA20")) {
            return CHACHA20;
        }
        if (method.toUpperCase().contains("AES")) {
            return AES;
        }
        throw new IllegalArgumentException("Unsupported algorithm method: " + method);
    }

    public AlgorithmParameterSpec createParameterSpec(final byte[] iv) {
        return this == AES ? new GCMParameterSpec(tagLengthBits, iv) : new IvParameterSpec(iv);
    }
}
