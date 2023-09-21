package com.knubisoft.testlum.testing.framework.auth;

import com.knubisoft.testlum.testing.model.scenario.Auth;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;

import static java.lang.String.format;

@Slf4j
@UtilityClass
public class LogUtil {

    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String ENDPOINT_LOG = format(TABLE_FORMAT, "Endpoint", "{}");
    private static final String CREDENTIALS_LOG = format(TABLE_FORMAT, "Credentials", "{}");
    private static final String INVALID_CREDENTIALS_LOG = format(TABLE_FORMAT, "Invalid credentials", "{}");
    private static final String SERVER_BAD_GATEWAY_RESPONSE_LOG = format(TABLE_FORMAT, "Server is shutdown", "{}");
    private static final String SERVER_ERROR_RESPONSE_LOG = format(TABLE_FORMAT, "Request failed", "{}");
    private static final int NOT_FOUND = 404;
    private static final int BAD_GATEWAY = 502;

    public void logAuthInfo(final Auth auth) {
        log.info(ALIAS_LOG, auth.getApiAlias());
        log.info(ENDPOINT_LOG, auth.getLoginEndpoint());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
    }

    public void logResponseStatusError(final HttpClientErrorException exception) {
        if (NOT_FOUND == exception.getRawStatusCode()) {
            log.info(INVALID_CREDENTIALS_LOG, exception.getRawStatusCode());
        } else if (BAD_GATEWAY == exception.getRawStatusCode()) {
            log.info(SERVER_BAD_GATEWAY_RESPONSE_LOG, exception.getRawStatusCode());
        } else {
            log.info(SERVER_ERROR_RESPONSE_LOG, exception.getRawStatusCode());
        }
    }
}
