package com.knubisoft.testlum.testing.framework.interpreter.lib.http.util;

import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;

import java.nio.charset.StandardCharsets;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@UtilityClass
public class LogUtil {

    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String HTTP_METHOD_LOG = format(TABLE_FORMAT, "HTTP method", "{}");
    private static final String ENDPOINT_LOG = format(TABLE_FORMAT, "Endpoint", "{}");
    private static final int MAX_CONTENT_LENGTH = 25 * 1024;
    private static final String BODY_LOG = format(TABLE_FORMAT, "Body", "{}");
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String CONTENT_FORMAT = format("%n%19s| %-23s|", EMPTY, EMPTY);
    private static final String ERROR_LOG = "Error ->";
    private static final String LAMBDA_FUNCTION_LOG = format(TABLE_FORMAT, "Function name", "{}");
    private static final String LAMBDA_PAYLOAD_LOG = format(TABLE_FORMAT, "Payload", "{}");
    private static final String SKIPPED_BODY_VALIDATION = "Validation of the response body was skipped "
            + "because of no expected file";


    public void logHttpInfo(final String alias, final String method, final String endpoint) {
        log.info(ALIAS_LOG, alias);
        log.info(HTTP_METHOD_LOG, method);
        log.info(ENDPOINT_LOG, endpoint);
    }

    @SneakyThrows
    public void logBodyContent(final HttpEntity body) {
        if (nonNull(body) && body.getContentLength() < MAX_CONTENT_LENGTH) {
            logBody(IOUtils.toString(body.getContent(), StandardCharsets.UTF_8.name()));
        }
    }

    public void logBody(final String body) {
        if (isNotBlank(body)) {
            log.info(BODY_LOG,
                    StringPrettifier.asJsonResult(StringPrettifier.cut(body))
                            .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
        }
    }

    public void logError(final Exception ex) {
        log.error(ERROR_LOG, ex);
    }

    public void logLambdaInfo(final String alias, final String functionName, final String payload) {
        log.info(ALIAS_LOG, alias);
        log.info(LAMBDA_FUNCTION_LOG, functionName);
        if (isNotBlank(payload)) {
            log.info(LAMBDA_PAYLOAD_LOG,
                    StringPrettifier.asJsonResult(payload).replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
        }
    }

    public void logBodyValidationSkipped() {
        log.info(SKIPPED_BODY_VALIDATION);
    }
}
