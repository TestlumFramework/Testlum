package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.VerifyEmailAddressRequest;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.EMPTY;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@InterpreterForClass(Ses.class)
public class SESInterpreter extends AbstractInterpreter<Ses> {

    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String DESTINATION_LOG = format(TABLE_FORMAT, "Destination", "{}");
    private static final String SOURCE_LOG = format(TABLE_FORMAT, "Source", "{}");
    private static final String BODY_LOG = format(TABLE_FORMAT, "Body", "{}");
    private static final String ALIAS = "Alias";
    private static final String DESTINATION = "Destination";
    private static final String SUBJECT = "Subject";
    private static final String HTML = "HTML";
    private static final String TEXT = "Text";
    private static final String SOURCE = "Source";
    private static final String SES_BODY_CONTENT_AND_TITLE_TEMPLATE = "%n%46s:%n%47s%-100s";
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    private Map<AliasEnv, SesClient> sesClient;

    public SESInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Ses o, final CommandResult result) {
        Ses ses = injectCommand(o);
        checkAlias(ses);
        logSesInfo(ses);
        addSesMetaData(ses, result);
        AliasEnv aliasEnv = new AliasEnv(ses.getAlias(), dependencies.getEnvironment());
        verify(ses, aliasEnv);
        sendEmail(ses, aliasEnv);
    }

    private void checkAlias(final Ses ses) {
        if (ses.getAlias() == null) {
            ses.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private void verify(final Ses ses, final AliasEnv aliasEnv) {
        VerifyEmailAddressRequest verifyEmailAddressRequest = createVerifyEmailAddress(ses);
        sesClient.get(aliasEnv).verifyEmailAddress(verifyEmailAddressRequest);
    }

    private void sendEmail(final Ses ses, final AliasEnv aliasEnv) {
        SendEmailRequest sendEmailRequest = createSendEmailRequest(ses);
        logSESMessage(sendEmailRequest.message());
        sesClient.get(aliasEnv).sendEmail(sendEmailRequest);
    }

    private VerifyEmailAddressRequest createVerifyEmailAddress(final Ses ses) {
        return VerifyEmailAddressRequest.builder()
                .emailAddress(ses.getSource())
                .build();
    }

    private SendEmailRequest createSendEmailRequest(final Ses ses) {
        return SendEmailRequest.builder()
                .destination(createDestination(ses))
                .message(createMessage(ses))
                .source(ses.getSource())
                .build();
    }

    private Destination createDestination(final Ses ses) {
        return Destination.builder()
                .toAddresses(ses.getDestination())
                .build();
    }

    private Message createMessage(final Ses ses) {
        SesMessage message = ses.getMessage();
        return Message.builder()
                .body(createBody(message.getBody()))
                .subject(createContent(message.getSubject()))
                .build();
    }

    private Body createBody(final SesBody body) {
        return Body.builder()
                .html(createContent(body.getHtml()))
                .text(createContent(body.getText()))
                .build();
    }

    private Content createContent(final SesTextContent textContent) {
        return Content.builder()
                .charset(textContent.getCharset())
                .data(textContent.getValue())
                .build();
    }

    private void logSesInfo(final Ses ses) {
        log.info(ALIAS_LOG, ses.getAlias());
        log.info(SOURCE_LOG, ses.getSource());
        log.info(DESTINATION_LOG, ses.getDestination());
    }

    private void addSesMetaData(final Ses ses, final CommandResult result) {
        SesMessage message = ses.getMessage();
        SesBody body = message.getBody();
        result.put(ALIAS, ses.getAlias());
        result.put(DESTINATION, ses.getDestination());
        result.put(SOURCE, ses.getSource());
        result.put(SUBJECT, message.getSubject().getValue());
        result.put(TEXT, body.getText().getValue());
        result.put(HTML, body.getHtml().getValue());
    }

    private void logSESMessage(final Message sesMessage) {
        StringBuilder message = new StringBuilder();
        if (nonNull(sesMessage.body())) {
            appendBodyContentIfNotBlank(sesMessage.body().html().data(), "HTML", message);
            appendBodyContentIfNotBlank(sesMessage.body().text().data(), "Text", message);
        } else {
            message.append("Message body is empty");
        }
        log.info(BODY_LOG, message);
    }
    private void appendBodyContentIfNotBlank(final String data, final String title, final StringBuilder sb) {
        if (isNotBlank(data)) {
            sb.append(format(SES_BODY_CONTENT_AND_TITLE_TEMPLATE,
                    title,
                    EMPTY,
                    data.replaceAll(REGEX_NEW_LINE, format("%n%15s", EMPTY))));
        }
    }
}