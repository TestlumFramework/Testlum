package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.DriverFailureMessage;
import com.knubisoft.testlum.testing.framework.exception.DriverCreationException;
import com.knubisoft.testlum.testing.framework.exception.FormattedFailure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DriverFailureDiagnostic {

    private static final String[] NOISE_MARKERS = {
            "Host info:", "Build info:", "System info:", "Driver info:", "Command:", "Capabilities {"};

    private static final int LABEL_WIDTH = 10;
    private static final int MAX_LINE_WIDTH = 120;
    private static final String INDENT = "  ";
    private static final String SEPARATOR = " : ";
    private static final String LABEL_FORMAT = INDENT + "%-" + LABEL_WIDTH + "s" + SEPARATOR;
    private static final String CREDENTIALS_MASK = "***";
    private static final char AT = '@';
    private static final String SCHEME_SEPARATOR = "://";
    private static final String TYPE_SEPARATOR = ": ";
    private static final String MESSAGE_JOINER = " | ";

    public DriverCreationException describe(final Throwable failure, final DriverFailureContext context) {
        DriverCreationException described = build(failure, context);
        log.error(LogFormat.withRed(described.describe()));
        return described;
    }

    public DriverCreationException describeForRetry(final Throwable failure, final DriverFailureContext context) {
        return build(failure, context);
    }

    private DriverCreationException build(final Throwable failure, final DriverFailureContext context) {
        FormattedFailure alreadyFormatted = FormattedFailure.findIn(failure);
        if (alreadyFormatted instanceof DriverCreationException described) {
            return described;
        }
        List<Throwable> chain = chainOf(failure);
        log.debug(DriverFailureMessage.RAW_FAILURE_DEBUG, context.getAlias(), failure);

        List<String> messages = cleanMessagesOf(chain);
        Optional<DriverFailureHint> hint = DriverFailureHint.resolve(chain, join(messages), context);
        String description = buildDescription(context, causeOf(chain, messages), hintTextOf(hint, context));
        return new DriverCreationException(description, hint.map(DriverFailureHint::isRetryable).orElse(true), failure);
    }

    private String hintTextOf(final Optional<DriverFailureHint> hint, final DriverFailureContext context) {
        return hint.map(matched -> matched.describe(context))
                .orElseGet(() -> String.format(context.getKind().getFallbackHint(), context.getConfigPath()));
    }

    private String buildDescription(final DriverFailureContext context, final String cause, final String hint) {
        List<String> lines = new ArrayList<>();
        lines.add(headlineOf(context));
        appendField(lines, DriverFailureMessage.LABEL_ENV, context.getEnv());
        appendField(lines, DriverFailureMessage.LABEL_CONFIG, context.getConfigPath());
        appendField(lines, DriverFailureMessage.LABEL_CONNECTION, connectionOf(context));
        appendField(lines, DriverFailureMessage.LABEL_CAUSE,
                StringUtils.defaultIfBlank(cause, DriverFailureMessage.UNKNOWN_CAUSE));
        appendField(lines, DriverFailureMessage.LABEL_HOW_TO_FIX, hint);
        return String.join(System.lineSeparator(), lines);
    }

    private String headlineOf(final DriverFailureContext context) {
        String element = context.getKind().getConfigElement();
        if (StringUtils.isBlank(context.getQualifier())) {
            return String.format(DriverFailureMessage.HEADLINE_PLAIN, element, context.getAlias());
        }
        String template = context.getKind().isDevice()
                ? DriverFailureMessage.HEADLINE_DEVICE
                : DriverFailureMessage.HEADLINE_BROWSER;
        return String.format(template, element, context.getAlias(), context.getQualifier());
    }

    private String connectionOf(final DriverFailureContext context) {
        if (StringUtils.isBlank(context.getServerUrl())) {
            return context.getConnectionName();
        }
        String url = maskCredentials(context.getServerUrl());
        if (StringUtils.isBlank(context.getConnectionName())) {
            return url;
        }
        return String.format(DriverFailureMessage.CONNECTION_WITH_URL, context.getConnectionName(), url);
    }

    private void appendField(final List<String> lines, final String label, final String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        String continuation = StringUtils.repeat(' ', String.format(LABEL_FORMAT, label).length());
        boolean first = true;
        for (String wrapped : wrap(value.trim(), MAX_LINE_WIDTH - continuation.length())) {
            lines.add((first ? String.format(LABEL_FORMAT, label) : continuation) + wrapped);
            first = false;
        }
    }

    private List<String> wrap(final String value, final int width) {
        List<String> wrapped = new ArrayList<>();
        for (String paragraph : value.split("\\R")) {
            wrapped.addAll(wrapParagraph(paragraph.trim(), width));
        }
        return wrapped;
    }

    private List<String> wrapParagraph(final String paragraph, final int width) {
        List<String> wrapped = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : paragraph.split(StringUtils.SPACE)) {
            if (!current.isEmpty() && current.length() + 1 + word.length() > width) {
                wrapped.add(current.toString());
                current.setLength(0);
            }
            current.append(!current.isEmpty() ? StringUtils.SPACE : StringUtils.EMPTY).append(word);
        }
        wrapped.add(current.toString());
        return wrapped;
    }

    private String stripNoise(final String message) {
        int cut = message.length();
        for (String marker : NOISE_MARKERS) {
            int index = message.indexOf(marker);
            if (index >= 0 && index < cut) {
                cut = index;
            }
        }
        return message.substring(0, cut).trim();
    }

    private List<Throwable> chainOf(final Throwable failure) {
        List<Throwable> chain = new ArrayList<>();
        Throwable current = failure;
        while (current != null && !containsSame(chain, current)) {
            chain.add(current);
            current = current.getCause();
        }
        return chain;
    }

    private boolean containsSame(final List<Throwable> chain, final Throwable failure) {
        for (Throwable seen : chain) {
            if (seen == failure) {
                return true;
            }
        }
        return false;
    }

    private List<String> cleanMessagesOf(final List<Throwable> chain) {
        List<String> messages = new ArrayList<>();
        for (int index = 0; index < chain.size(); index++) {
            Throwable deeper = index + 1 < chain.size() ? chain.get(index + 1) : null;
            messages.add(messageOf(chain.get(index), deeper));
        }
        return messages;
    }

    private String causeOf(final List<Throwable> chain, final List<String> messages) {
        Throwable rootCause = chain.get(chain.size() - 1);
        String message = deepestMessage(messages);
        if (StringUtils.isBlank(message)) {
            return rootCause.getClass().getSimpleName();
        }
        return StringUtils.isBlank(messages.get(messages.size() - 1))
                ? rootCause.getClass().getSimpleName() + TYPE_SEPARATOR + message
                : message;
    }

    private String deepestMessage(final List<String> messages) {
        for (int index = messages.size() - 1; index >= 0; index--) {
            if (StringUtils.isNotBlank(messages.get(index))) {
                return messages.get(index);
            }
        }
        return StringUtils.EMPTY;
    }

    private String join(final List<String> messages) {
        return messages.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(MESSAGE_JOINER));
    }

    private String messageOf(final Throwable failure, final Throwable deeper) {
        String message = StringUtils.defaultString(failure.getMessage());
        if (deeper != null && message.equals(deeper.toString())) {
            return StringUtils.EMPTY;
        }
        return stripNoise(message);
    }

    private String maskCredentials(final String url) {
        int schemeEnd = url.indexOf(SCHEME_SEPARATOR);
        int credentialsEnd = url.indexOf(AT);
        if (schemeEnd < 0 || credentialsEnd < 0 || credentialsEnd < schemeEnd) {
            return url;
        }
        return url.substring(0, schemeEnd + SCHEME_SEPARATOR.length()) + CREDENTIALS_MASK
               + url.substring(credentialsEnd);
    }

}
