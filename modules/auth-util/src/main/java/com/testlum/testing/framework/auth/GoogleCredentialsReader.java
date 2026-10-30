package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.SystemVariableService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleCredentialsReader {

    private final FileSearcher fileSearcher;
    private final SystemVariableService systemVariableService;
    private final JacksonService jacksonService;

    public String readRefreshToken(final String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new DefaultFrameworkException("Google credentials file name must not be empty");
        }
        String content = readCredentialsFile(fileName);
        String injected = systemVariableService.inject(content);
        Object credentials = parseCredentials(injected, fileName);
        return extractRefreshToken(credentials, fileName);
    }

    private String extractRefreshToken(final Object credentials, final String fileName) {
        if (!(credentials instanceof Map<?, ?> values)) {
            throw new DefaultFrameworkException("Google credentials file <%s> must contain a JSON object", fileName);
        }
        Object value = values.get("refreshToken");
        if (!(value instanceof String refreshToken) || StringUtils.isBlank(refreshToken)) {
            throw new DefaultFrameworkException(
                    "Google credentials file <%s> must contain a non-empty string refreshToken", fileName);
        }
        return refreshToken;
    }

    private String readCredentialsFile(final String fileName) {
        try {
            return FileUtils.readFileToString(fileSearcher.searchFileFromDataFolder(fileName), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DefaultFrameworkException("Cannot read Google credentials file <%s>", fileName);
        }
    }

    private Object parseCredentials(final String content, final String fileName) {
        try {
            return jacksonService.readValue(content, Object.class);
        } catch (DefaultFrameworkException exception) {
            throw new DefaultFrameworkException("Google credentials file <%s> must contain valid JSON", fileName);
        }
    }
}
