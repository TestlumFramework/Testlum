package com.testlum.testing.framework.service;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.Email;
import com.testlum.testing.model.global_config.EmailProperties;
import com.testlum.testing.model.global_config.EmailProperty;
import com.testlum.testing.model.global_config.EmailProtocol;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailInboxServiceTest {

    private Email emailSettings;
    private EmailInboxService service;

    @BeforeEach
    void setUp() {
        this.emailSettings = new Email();
        this.emailSettings.setAlias("testAlias");
        this.emailSettings.setHost("imap.test.com");
        this.emailSettings.setPort(BigInteger.valueOf(993));
        this.emailSettings.setUsername("user@test.com");
        this.emailSettings.setPassword("secret");
        this.emailSettings.setProtocol(EmailProtocol.IMAPS);
        this.emailSettings.setFolder("INBOX");
        this.emailSettings.setSsl(true);
        this.emailSettings.setTimeout(BigInteger.valueOf(5000));
        this.service = new EmailInboxService(this.emailSettings);
    }

    @Nested
    class MatchPattern {

        @Test
        void matchesWithCaptureGroup() {
            final Pattern pattern = Pattern.compile("code: (\\d+)");
            final String result = service.matchPattern("Your activation code: 849201 for account", pattern);
            assertEquals("849201", result);
        }

        @Test
        void matchesWithoutCaptureGroup() {
            final Pattern pattern = Pattern.compile("\\d{6}");
            final String result = service.matchPattern("Your code is 123456 thank you", pattern);
            assertEquals("123456", result);
        }

        @Test
        void returnsNullWhenNoMatch() {
            final Pattern pattern = Pattern.compile("token=([a-z]+)");
            final String result = service.matchPattern("No token here", pattern);
            assertNull(result);
        }

        @Test
        void returnsNullOnNullOrEmptyContent() {
            final Pattern pattern = Pattern.compile("\\d+");
            assertNull(service.matchPattern(null, pattern));
            assertNull(service.matchPattern("", pattern));
        }
    }

    @Nested
    class ExtractContent {

        @Test
        void extractsTextPlain() throws Exception {
            final Part part = mock(Part.class);
            when(part.isMimeType("text/plain")).thenReturn(true);
            when(part.getContent()).thenReturn("Hello plain text");

            final String content = service.extractContent(part);
            assertEquals("Hello plain text", content);
        }

        @Test
        void extractsTextHtml() throws Exception {
            final Part part = mock(Part.class);
            when(part.isMimeType("text/plain")).thenReturn(false);
            when(part.isMimeType("text/html")).thenReturn(true);
            when(part.getContent()).thenReturn("<p>Code: 999</p>");

            final String content = service.extractContent(part);
            assertEquals("<p>Code: 999</p>", content);
        }

        @Test
        void extractsSubjectAndBodyWhenMessage() throws Exception {
            final Message msg = mock(Message.class);
            when(msg.getSubject()).thenReturn("OTP Confirmation");
            when(msg.isMimeType("text/plain")).thenReturn(true);
            when(msg.getContent()).thenReturn("Your OTP is 554433");

            final String content = service.extractContent(msg);
            assertEquals("Subject: OTP Confirmation\nYour OTP is 554433", content);
        }

        @Test
        void extractsFromMultipart() throws Exception {
            final Part part = mock(Part.class);
            when(part.isMimeType("text/plain")).thenReturn(false);
            when(part.isMimeType("text/html")).thenReturn(false);
            when(part.isMimeType("multipart/*")).thenReturn(true);

            final Multipart multipart = mock(Multipart.class);
            when(multipart.getCount()).thenReturn(2);

            final jakarta.mail.BodyPart subPart1 = mock(jakarta.mail.BodyPart.class);
            when(subPart1.isMimeType("text/plain")).thenReturn(true);
            when(subPart1.getContent()).thenReturn("Part 1 text");

            final jakarta.mail.BodyPart subPart2 = mock(jakarta.mail.BodyPart.class);
            when(subPart2.isMimeType("text/plain")).thenReturn(false);
            when(subPart2.isMimeType("text/html")).thenReturn(true);
            when(subPart2.getContent()).thenReturn("<b>Part 2 html</b>");

            when(multipart.getBodyPart(0)).thenReturn(subPart1);
            when(multipart.getBodyPart(1)).thenReturn(subPart2);
            when(part.getContent()).thenReturn(multipart);

            final String content = service.extractContent(part);
            assertEquals("Part 1 text\n<b>Part 2 html</b>\n", content);
        }

        @Test
        void returnsEmptyForUnsupportedMimeType() throws Exception {
            final Part part = mock(Part.class);
            when(part.isMimeType("text/plain")).thenReturn(false);
            when(part.isMimeType("text/html")).thenReturn(false);
            when(part.isMimeType("multipart/*")).thenReturn(false);

            final String content = service.extractContent(part);
            assertEquals("", content);
        }
    }

    @Nested
    class BuildProperties {

        @Test
        void buildsDefaultPropertiesForImaps() {
            final Properties props = service.buildProperties("imaps");
            assertEquals("imap.test.com", props.getProperty("mail.imaps.host"));
            assertEquals("993", props.getProperty("mail.imaps.port"));
            assertEquals("5000", props.getProperty("mail.imaps.connectiontimeout"));
            assertEquals("5000", props.getProperty("mail.imaps.timeout"));
            assertEquals("true", props.getProperty("mail.imaps.ssl.enable"));
        }

        @Test
        void appliesCustomProperties() {
            final EmailProperties customProps = new EmailProperties();
            final EmailProperty prop = new EmailProperty();
            prop.setName("mail.imaps.ssl.trust");
            prop.setValue("*");
            customProps.getProperty().add(prop);
            emailSettings.setProperties(customProps);

            final Properties props = service.buildProperties("imaps");
            assertEquals("*", props.getProperty("mail.imaps.ssl.trust"));
        }
    }

    @Nested
    class ConnectionAndSearch {

        @Test
        void testConnectionSucceedsWhenStoreAndFolderOpen() throws Exception {
            final Store mockStore = mock(Store.class);
            final Folder mockFolder = mock(Folder.class);
            when(mockStore.isConnected()).thenReturn(true);
            when(mockFolder.isOpen()).thenReturn(true);
            when(mockStore.getFolder("INBOX")).thenReturn(mockFolder);

            final EmailInboxService spyService = new EmailInboxService(emailSettings) {
                @Override
                protected Store connectStore() {
                    return mockStore;
                }
            };

            spyService.testConnection();
            verify(mockFolder).open(Folder.READ_ONLY);
            verify(mockFolder).close(false);
            verify(mockStore).close();
        }

        @Test
        void testConnectionThrowsWhenConnectFails() {
            final EmailInboxService failingService = new EmailInboxService(emailSettings) {
                @Override
                protected Store connectStore() throws MessagingException {
                    throw new MessagingException("Auth failed");
                }
            };

            assertThrows(DefaultFrameworkException.class, failingService::testConnection);
        }

        @Test
        void fetchValueByPatternFindsUnreadMessageAndMarksAsRead() throws Exception {
            final Store mockStore = mock(Store.class);
            final Folder mockFolder = mock(Folder.class);
            final Message mockMessage = mock(Message.class);

            when(mockStore.isConnected()).thenReturn(true);
            when(mockFolder.isOpen()).thenReturn(true);
            when(mockStore.getFolder("INBOX")).thenReturn(mockFolder);
            when(mockFolder.getMessageCount()).thenReturn(1);
            when(mockFolder.getMessage(1)).thenReturn(mockMessage);
            when(mockMessage.isSet(Flags.Flag.SEEN)).thenReturn(false);
            when(mockMessage.isMimeType("text/plain")).thenReturn(true);
            when(mockMessage.getContent()).thenReturn("Verification code: 778899");

            final EmailInboxService spyService = new EmailInboxService(emailSettings) {
                @Override
                protected Store connectStore() {
                    return mockStore;
                }
            };

            final String value = spyService.fetchValueByPattern("code: (\\d+)", 2000L);
            assertEquals("778899", value);
            verify(mockMessage).setFlag(Flags.Flag.SEEN, true);
            verify(mockFolder).open(Folder.READ_WRITE);
            verify(mockStore, times(1)).close();
        }

        @Test
        void fetchValueByPatternIgnoresAlreadyReadMessages() throws Exception {
            final Store mockStore = mock(Store.class);
            final Folder mockFolder = mock(Folder.class);
            final Message readMessage = mock(Message.class);

            when(mockStore.isConnected()).thenReturn(true);
            when(mockFolder.isOpen()).thenReturn(true);
            when(mockStore.getFolder("INBOX")).thenReturn(mockFolder);
            when(mockFolder.getMessageCount()).thenReturn(1);
            when(mockFolder.getMessage(1)).thenReturn(readMessage);
            when(readMessage.isSet(Flags.Flag.SEEN)).thenReturn(true);

            final EmailInboxService spyService = new EmailInboxService(emailSettings) {
                @Override
                protected Store connectStore() {
                    return mockStore;
                }
            };

            assertThrows(DefaultFrameworkException.class,
                    () -> spyService.fetchValueByPattern("code: (\\d+)", 100L));
            verify(readMessage, times(0)).setFlag(Flags.Flag.SEEN, true);
        }

        @Test
        void fetchValueByPatternThrowsOnTimeout() throws Exception {
            final Store mockStore = mock(Store.class);
            final Folder mockFolder = mock(Folder.class);

            when(mockStore.isConnected()).thenReturn(true);
            when(mockFolder.isOpen()).thenReturn(true);
            when(mockStore.getFolder("INBOX")).thenReturn(mockFolder);
            when(mockFolder.getMessageCount()).thenReturn(0);

            final EmailInboxService spyService = new EmailInboxService(emailSettings) {
                @Override
                protected Store connectStore() {
                    return mockStore;
                }
            };

            assertThrows(DefaultFrameworkException.class,
                    () -> spyService.fetchValueByPattern("code: (\\d+)", 100L));
            verify(mockStore, times(1)).close();
        }
    }
}
