package com.testlum.testing.framework.util;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

enum DriverFailureScope {

    ANY(context -> true),
    SERVER_URL_KNOWN(context -> StringUtils.isNotBlank(context.getServerUrl())),
    IN_DOCKER(DriverFailureContext::isInDocker);

    private final Predicate<DriverFailureContext> applicability;

    DriverFailureScope(final Predicate<DriverFailureContext> applicability) {
        this.applicability = applicability;
    }

    boolean covers(final DriverFailureContext context) {
        return applicability.test(context);
    }
}
