package com.testlum.testing.framework.service;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.Email;
import com.testlum.testing.model.global_config.EmailProperties;
import com.testlum.testing.model.global_config.EmailProtocol;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for interacting with email inboxes (IMAP, POP3).
 * Supports connection pooling, message polling, subject matching, and regex token extraction.
 */
@Slf4j
@RequiredArgsConstructor
public class EmailInboxService {

    private static final long DEFAULT_POLL_INTERVAL_MS = 1000L;
    private static final int MAX_SEARCH_MESSAGES = 50;
    private static final String PROTOCOL_PROPERTY_FORMAT = "mail.%s.%s";
    private static final String EMAIL_NOT_FOUND_MSG =
            "Timeout of %d ms reached. No email matching pattern [%s] found in folder [%s]";

    @Getter
    private final Email emailSettings;

    /**
     * Verifies the connection to the configured email store and folder.
     */
    public void testConnection() {
        final String folderName = this.resolveFolder();
        Store store = null;
        Folder folder = null;
        try {
            store = this.connectStore();
            folder = store.getFolder(folderName);
            folder.open(Folder.READ_ONLY);
        } catch (final Exception e) {
            this.handleConnectionException(e);
        } finally {
            this.closeQuietly(folder);
            this.closeQuietly(store);
        }
    }

    private void handleConnectionException(final Exception e) {
        if (e instanceof DefaultFrameworkException dfe) {
            throw dfe;
        }
        throw new DefaultFrameworkException("Failed to connect to email store: " + e.getMessage(), e);
    }

    /**
     * Polls the inbox for messages matching the given pattern until timeout.
     *
     * @param patternStr regex pattern to extract value from message content
     * @param timeoutMs maximum time to wait in milliseconds
     * @return extracted value matching the first capturing group
     */
    public String fetchValueByPattern(final String patternStr, final long timeoutMs) {
        this.validateFetchParams(patternStr, timeoutMs);
        final Pattern pattern = Pattern.compile(patternStr);
        final long deadline = System.currentTimeMillis() + timeoutMs;
        Store store = null;
        try {
            store = this.connectStore();
            return this.pollForMatchingEmail(store, pattern, deadline, timeoutMs, patternStr);
        } catch (final MessagingException e) {
            throw new DefaultFrameworkException("Failed to connect to email store: " + e.getMessage(), e);
        } finally {
            this.closeQuietly(store);
        }
    }

    private void validateFetchParams(final String patternStr, final long timeoutMs) {
        if (StringUtils.isBlank(patternStr)) {
            throw new DefaultFrameworkException("Pattern must not be null or blank");
        }
        if (timeoutMs <= 0) {
            throw new DefaultFrameworkException("Timeout must be greater than 0");
        }
    }

    private String pollForMatchingEmail(final Store store, final Pattern pattern,
                                        final long deadline, final long timeoutMs,
                                        final String patternStr) {
        while (System.currentTimeMillis() < deadline) {
            final Optional<String> match = this.searchInStoreFolder(store, pattern);
            if (match.isPresent()) {
                return match.get();
            }
            this.sleep(DEFAULT_POLL_INTERVAL_MS);
        }
        final String folder = this.resolveFolder();
        throw new DefaultFrameworkException(String.format(EMAIL_NOT_FOUND_MSG, timeoutMs, patternStr, folder));
    }

    private Optional<String> searchInStoreFolder(final Store store, final Pattern pattern) {
        Folder folder = null;
        try {
            folder = store.getFolder(this.resolveFolder());
            folder.open(Folder.READ_WRITE);
            return this.findMatchInFolder(folder, pattern);
        } catch (final Exception e) {
            log.debug("Error checking folder: {}", e.getMessage());
            return Optional.empty();
        } finally {
            this.closeQuietly(folder);
        }
    }

    private Optional<String> findMatchInFolder(final Folder folder, final Pattern pattern) throws Exception {
        final int count = folder.getMessageCount();
        final int minIndex = Math.max(1, count - MAX_SEARCH_MESSAGES + 1);
        for (int i = count; i >= minIndex; i--) {
            final Optional<String> match = this.checkMessage(folder.getMessage(i), pattern);
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }

    private Optional<String> checkMessage(final Message msg, final Pattern pattern) throws Exception {
        if (this.isMessageUnread(msg)) {
            final String content = this.extractContent(msg);
            final Optional<String> match = this.matchPattern(content, pattern);
            match.ifPresent(m -> this.markAsRead(msg));
            return match;
        }
        return Optional.empty();
    }

    private boolean isMessageUnread(final Message msg) {
        try {
            return !msg.isSet(Flags.Flag.SEEN);
        } catch (final Exception e) {
            return true;
        }
    }

    private void markAsRead(final Message msg) {
        try {
            msg.setFlag(Flags.Flag.SEEN, true);
        } catch (final Exception e) {
            log.debug("Could not mark message as read: {}", e.getMessage());
        }
    }

    /**
     * Extracts text content from a message or MIME body part.
     *
     * @param part email message part
     * @return extracted text content
     * @throws MessagingException if a message error occurs
     * @throws IOException if an I/O error occurs
     */
    public String extractContent(final Part part) throws MessagingException, IOException {
        final StringBuilder builder = new StringBuilder();
        if (part instanceof Message msg) {
            Optional.ofNullable(msg.getSubject())
                    .ifPresent(subject -> builder.append("Subject: ").append(subject).append("\n"));
        }
        builder.append(this.extractBody(part));
        return builder.toString();
    }

    private String extractBody(final Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
            return String.valueOf(part.getContent());
        }
        if (part.isMimeType("multipart/*")) {
            return this.extractFromMultipart((Multipart) part.getContent());
        }
        return "";
    }

    private String extractFromMultipart(final Multipart multipart) throws MessagingException, IOException {
        final StringBuilder builder = new StringBuilder();
        final int partCount = multipart.getCount();
        for (int i = 0; i < partCount; i++) {
            builder.append(this.extractBody(multipart.getBodyPart(i))).append("\n");
        }
        return builder.toString();
    }

    /**
     * Matches the given regex pattern against message content.
     *
     * @param content text content to search within
     * @param pattern compiled regex pattern
     * @return captured value or full match, or null if no match found
     */
    public Optional<String> matchPattern(final String content, final Pattern pattern) {
        return Optional.ofNullable(content)
                .filter(Predicate.not(String::isEmpty))
                .map(pattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.groupCount() >= 1 ? matcher.group(1) : matcher.group(0));
    }

    protected Store connectStore() throws MessagingException {
        this.validateEmailSettings();
        final String protocol = this.resolveProtocol();
        final Properties properties = this.buildProperties(protocol);
        final Session session = Session.getInstance(properties);
        final Store store = session.getStore(protocol);
        store.connect(this.emailSettings.getHost(),
                this.emailSettings.getPort().intValue(),
                this.emailSettings.getEmailAddress(),
                this.emailSettings.getPassword());
        return store;
    }

    private void validateEmailSettings() {
        if (StringUtils.isBlank(this.emailSettings.getHost())) {
            throw new DefaultFrameworkException("Email host must not be null or blank");
        }
        if (this.emailSettings.getPort() == null) {
            throw new DefaultFrameworkException("Email port must not be null");
        }
        if (StringUtils.isBlank(this.emailSettings.getEmailAddress())) {
            throw new DefaultFrameworkException("Email address must not be null or blank");
        }
    }

    /**
     * Builds JavaMail properties for the given protocol from configured email settings.
     *
     * @param protocol email protocol (e.g. imaps, pop3s)
     * @return populated Properties object
     */
    public Properties buildProperties(final String protocol) {
        final Properties props = new Properties();
        props.put(String.format(PROTOCOL_PROPERTY_FORMAT, protocol, "host"), this.emailSettings.getHost());
        props.put(String.format(PROTOCOL_PROPERTY_FORMAT, protocol, "port"),
                String.valueOf(this.emailSettings.getPort()));
        this.configureTimeout(props, protocol);
        this.configureSsl(props, protocol);
        this.configureCustomProperties(props);
        return props;
    }

    private void configureTimeout(final Properties props, final String protocol) {
        final String timeoutVal = Optional.ofNullable(this.emailSettings.getTimeout())
                .map(String::valueOf)
                .orElseThrow(() -> new DefaultFrameworkException("Email timeout must not be null"));
        props.put(String.format(PROTOCOL_PROPERTY_FORMAT, protocol, "connectiontimeout"), timeoutVal);
        props.put(String.format(PROTOCOL_PROPERTY_FORMAT, protocol, "timeout"), timeoutVal);
    }

    private void configureSsl(final Properties props, final String protocol) {
        final boolean isSsl = Optional.ofNullable(this.emailSettings.isSsl())
                .orElseThrow(() -> new DefaultFrameworkException("Email SSL setting must not be null"));
        if (isSsl || protocol.endsWith("s")) {
            props.put(String.format(PROTOCOL_PROPERTY_FORMAT, protocol, "ssl.enable"), "true");
        }
    }

    private void configureCustomProperties(final Properties props) {
        Optional.ofNullable(this.emailSettings.getProperties())
                .map(EmailProperties::getProperty)
                .ifPresent(propsList -> propsList.forEach(prop -> props.put(prop.getName(), prop.getValue())));
    }

    private String resolveProtocol() {
        return Optional.ofNullable(this.emailSettings.getProtocol())
                .map(EmailProtocol::value)
                .orElseThrow(() -> new DefaultFrameworkException("Email protocol must not be null"));
    }

    private String resolveFolder() {
        return Optional.ofNullable(this.emailSettings.getFolder())
                .filter(Predicate.not(String::isBlank))
                .orElseThrow(() -> new DefaultFrameworkException("Email folder must not be null or blank"));
    }

    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DefaultFrameworkException("Email polling was interrupted", e);
        }
    }

    private void closeQuietly(final Folder folder) {
        if (folder != null && folder.isOpen()) {
            try {
                folder.close(false);
            } catch (final Exception e) {
                log.debug("Error closing folder", e);
            }
        }
    }

    private void closeQuietly(final Store store) {
        if (store != null && store.isConnected()) {
            try {
                store.close();
            } catch (final Exception e) {
                log.debug("Error closing store", e);
            }
        }
    }
}
