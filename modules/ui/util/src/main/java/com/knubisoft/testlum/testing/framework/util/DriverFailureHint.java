package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.DriverFailureMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum DriverFailureHint {

    INVALID_SERVER_URL(
            (failure, message) -> failure instanceof MalformedURLException
                                  || message.contains("no protocol")
                                  || message.contains("unknown protocol"),
            false, false,
            context -> String.format(DriverFailureMessage.INVALID_SERVER_URL, context.getConfigPath())),

    BROWSER_STACK_AUTH_FAILED(
            (failure, message) -> message.contains("authorization required")
                                  || message.contains("invalid username or password")
                                  || message.contains("response code 401")
                                  || message.contains("response code 403"),
            false, false,
            context -> String.format(DriverFailureMessage.BROWSER_STACK_AUTH_FAILED, context.getConfigPath())),

    SAFARI_REMOTE_AUTOMATION_DISABLED(
            (failure, message) -> message.contains("allow remote automation")
                                  || message.contains("safaridriver --enable"),
            false, false,
            context -> DriverFailureMessage.SAFARI_REMOTE_AUTOMATION_DISABLED),

    DOCKER_NOT_AVAILABLE(
            (failure, message) -> failure.getClass().getSimpleName().contains("Docker")
                                  || isDockerFailure(message),
            false, false,
            context -> String.format(DriverFailureMessage.DOCKER_NOT_AVAILABLE, context.getConfigPath())),

    DEVICE_NOT_CONNECTED(
            (failure, message) -> message.contains("is not connected")
                                  || message.contains("device not found")
                                  || message.contains("could not find a connected")
                                  || message.contains("no devices found")
                                  || message.contains("unable to find an active device"),
            false, false,
            context -> String.format(DriverFailureMessage.DEVICE_NOT_CONNECTED, context.getConfigPath())),

    APP_NOT_FOUND(
            (failure, message) -> message.contains("does not exist or is not accessible")
                                  || message.contains("could not find app")
                                  || message.contains("the application at")
                                  || message.contains("app file"),
            false, false,
            context -> String.format(DriverFailureMessage.APP_NOT_FOUND, context.getConfigPath())),

    APP_ACTIVITY_NOT_STARTED(
            (failure, message) -> message.contains("appactivity")
                                  || message.contains("activity used to start the app doesn't exist")
                                  || message.contains("never started"),
            false, false,
            context -> String.format(DriverFailureMessage.APP_ACTIVITY_NOT_STARTED, context.getConfigPath())),

    BROWSER_VERSION_MISMATCH(
            (failure, message) -> message.contains("only supports chrome version")
                                  || message.contains("only supports edge version")
                                  || message.contains("cannot find matching capabilities")
                                  || message.contains("unable to find a matching set of capabilities"),
            false, false,
            context -> String.format(DriverFailureMessage.BROWSER_VERSION_MISMATCH, context.getConfigPath())),

    SERVER_ENDPOINT_NOT_FOUND(
            (failure, message) -> message.contains("response code 404")
                                  || message.contains("unable to find handler"),
            false, true,
            context -> String.format(DriverFailureMessage.SERVER_ENDPOINT_NOT_FOUND,
                    context.getServerUrl(), context.getConfigPath())),

    SERVER_NOT_REACHABLE(
            (failure, message) -> failure instanceof ConnectException
                                  || failure instanceof UnknownHostException
                                  || failure instanceof SocketTimeoutException
                                  || failure instanceof ClosedChannelException
                                  || message.contains("connection refused")
                                  || message.contains("closed channel")
                                  || message.contains("connect timed out")
                                  || message.contains("failed to connect")
                                  || message.contains("connection reset")
                                  || message.contains("unreachable"),
            true, true,
            context -> String.format(DriverFailureMessage.SERVER_NOT_REACHABLE,
                    context.getServerUrl(), context.getConfigPath()));

    private final BiPredicate<Throwable, String> matcher;
    private final boolean retryable;
    private final boolean requiresServerUrl;
    private final Function<DriverFailureContext, String> hint;

    public static Optional<DriverFailureHint> resolve(final List<Throwable> chain,
                                                      final String message,
                                                      final DriverFailureContext context) {
        String lowerCase = message.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(candidate -> candidate.isApplicable(context))
                .filter(candidate -> chain.stream().anyMatch(failure -> candidate.matcher.test(failure, lowerCase)))
                .findFirst();
    }

    private static boolean isDockerFailure(final String message) {
        return message.contains("docker")
               && (message.contains("cannot connect")
                   || message.contains("permission denied")
                   || message.contains("is not running")
                   || message.contains("no such file or directory"));
    }

    private boolean isApplicable(final DriverFailureContext context) {
        return !requiresServerUrl || StringUtils.isNotBlank(context.getServerUrl());
    }

    public String describe(final DriverFailureContext context) {
        return hint.apply(context);
    }
}
