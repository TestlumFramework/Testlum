package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.DriverFailureMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.*;
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
            false, DriverFailureScope.ANY, CauseRule.none(),
            context -> String.format(DriverFailureMessage.INVALID_SERVER_URL, context.getConfigPath())),

    BROWSER_STACK_AUTH_FAILED(
            (failure, message) -> message.contains("authorization required")
                                  || message.contains("invalid username or password")
                                  || message.contains("response code 401")
                                  || message.contains("response code 403"),
            false, DriverFailureScope.ANY, CauseRule.none(),
            context -> String.format(DriverFailureMessage.BROWSER_STACK_AUTH_FAILED, context.getConfigPath())),

    SAFARI_REMOTE_AUTOMATION_DISABLED(
            (failure, message) -> message.contains("allow remote automation")
                                  || message.contains("safaridriver --enable"),
            false, DriverFailureScope.ANY, CauseRule.none(),
            context -> DriverFailureMessage.SAFARI_REMOTE_AUTOMATION_DISABLED),

    DOCKER_NOT_AVAILABLE(
            (failure, message) -> failure.getClass().getSimpleName().contains("Docker")
                                  || isDockerFailure(message)
                                  || isDaemonUnreachable(failure, message),
            false, DriverFailureScope.IN_DOCKER,
            CauseRule.of("No such file or directory|Connection refused"
                         + "|Cannot connect to the Docker daemon[^\\n]*",
                    "Docker daemon is not reachable: %s"),
            context -> String.format(DriverFailureMessage.DOCKER_NOT_AVAILABLE, context.getConfigPath())),

    DEVICE_NOT_CONNECTED(
            (failure, message) -> message.contains("is not connected")
                                  || message.contains("device not found")
                                  || message.contains("could not find a connected")
                                  || message.contains("no devices found")
                                  || message.contains("unable to find an active device"),
            false, DriverFailureScope.ANY, CauseRule.none(),
            context -> String.format(DriverFailureMessage.DEVICE_NOT_CONNECTED, context.getConfigPath())),

    APP_NOT_FOUND(
            (failure, message) -> message.contains("does not exist or is not accessible")
                                  || message.contains("could not find app")
                                  || message.contains("the application at"),
            false, DriverFailureScope.ANY, CauseRule.none(),
            context -> String.format(DriverFailureMessage.APP_NOT_FOUND, context.getConfigPath())),

    APP_ACTIVITY_NOT_FOUND(
            (failure, message) -> message.contains("activity class")
                                  && message.contains("does not exist"),
            false, DriverFailureScope.ANY, CauseRule.of("Activity class \\{[^}]+} does not exist"),
            context -> String.format(DriverFailureMessage.APP_ACTIVITY_NOT_FOUND, context.getConfigPath())),

    APP_ACTIVITY_NOT_STARTED(
            (failure, message) -> message.contains("used to start the app doesn't exist")
                                  || message.contains("consider passing appwaitactivity")
                                  || message.contains("never started"),
            false, DriverFailureScope.ANY, CauseRule.none(),
            context -> String.format(DriverFailureMessage.APP_ACTIVITY_NOT_STARTED, context.getConfigPath())),

    ADB_EXEC_TIMEOUT(
            (failure, message) -> message.contains("adbexectimeout")
                                  || message.contains("adb execution timeout"),
            false, DriverFailureScope.ANY,
            CauseRule.of("Cannot start the '([^']+)' application.*?timed out after (\\d+)ms",
                    "Could not start '%s': adb command timed out after %sms"),
            context -> String.format(DriverFailureMessage.ADB_EXEC_TIMEOUT, context.getConfigPath())),

    BROWSER_VERSION_MISMATCH(
            (failure, message) -> message.contains("only supports chrome version")
                                  || message.contains("only supports edge version")
                                  || message.contains("cannot find matching capabilities")
                                  || message.contains("unable to find a matching set of capabilities"),
            false, DriverFailureScope.ANY, CauseRule.none(),
            context -> String.format(DriverFailureMessage.BROWSER_VERSION_MISMATCH, context.getConfigPath())),

    SERVER_ENDPOINT_NOT_FOUND(
            (failure, message) -> message.contains("response code 404")
                                  || message.contains("unable to find handler"),
            false, DriverFailureScope.SERVER_URL_KNOWN, CauseRule.none(),
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
            true, DriverFailureScope.SERVER_URL_KNOWN, CauseRule.none(),
            context -> String.format(DriverFailureMessage.SERVER_NOT_REACHABLE,
                    context.getServerUrl(), context.getConfigPath()));

    private final BiPredicate<Throwable, String> matcher;
    private final boolean retryable;
    private final DriverFailureScope scope;
    private final CauseRule causeRule;
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

    private static boolean isDaemonUnreachable(final Throwable failure, final String message) {
        return failure instanceof SocketException
               && (message.contains("no such file or directory")
                   || message.contains("connection refused")
                   || message.contains("permission denied"));
    }

    private boolean isApplicable(final DriverFailureContext context) {
        return scope.covers(context);
    }

    public String describe(final DriverFailureContext context) {
        return hint.apply(context);
    }
}
