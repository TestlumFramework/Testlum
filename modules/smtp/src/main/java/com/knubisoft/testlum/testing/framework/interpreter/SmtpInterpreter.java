package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Smtp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;

import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Smtp.class)
public class SmtpInterpreter extends AbstractInterpreter<Smtp> {

    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String SMTP_HOST_LOG = format(TABLE_FORMAT, "SMTP Host", "{}");
    private static final String SMTP_PORT_LOG = format(TABLE_FORMAT, "SMTP Port", "{}");
    private static final String SUBJECT_LOG = format(TABLE_FORMAT, "Subject", "{}");
    private static final String DESTINATION_LOG = format(TABLE_FORMAT, "Destination", "{}");
    private static final String SOURCE_LOG = format(TABLE_FORMAT, "Source", "{}");
    private static final String CONTENT_LOG = format(TABLE_FORMAT, "Content", "{}");

    private static final String ALIAS = "Alias";
    private static final String SMTP_HOST = "SMTP Host";
    private static final String SMTP_PORT = "SMTP Port";
    private static final String DESTINATION = "Destination";
    private static final String SUBJECT = "Subject";
    private static final String TEXT = "Text";
    private static final String SOURCE = "Source";
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    private Map<AliasEnv, JavaMailSenderImpl> javaMailSenderMap;

    public SmtpInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Smtp o, final CommandResult result) {
        Smtp smtp = injectCommand(o);
        checkAlias(smtp);
        AliasEnv aliasEnv = new AliasEnv(smtp.getAlias(), dependencies.getEnvironment());
        JavaMailSenderImpl javaMailSender = javaMailSenderMap.get(aliasEnv);
        logSmtpInfo(smtp, javaMailSender);
        addSmtpMetaData(smtp, javaMailSender, result);
        sendEmail(smtp, javaMailSender);
    }

    private void checkAlias(final Smtp smtp) {
        if (smtp.getAlias() == null) {
            smtp.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private void sendEmail(final Smtp smtp, final JavaMailSenderImpl javaMailSender) {
        try {
            String username = javaMailSender.getUsername();
            SimpleMailMessage simpleMailMessage = getSimpleMailMessage(smtp, username);
            javaMailSender.send(simpleMailMessage);
        } catch (Exception exception) {
            throw new DefaultFrameworkException(exception.getMessage());
        }
    }

    private SimpleMailMessage getSimpleMailMessage(final Smtp smtp, final String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(smtp.getRecipientEmail());
        message.setSubject(smtp.getSubject());
        message.setText(smtp.getText());
        return message;
    }

    private void logSmtpInfo(final Smtp smtp, final JavaMailSenderImpl javaMailSender) {
        log.info(ALIAS_LOG, smtp.getAlias());
        log.info(SMTP_HOST_LOG, javaMailSender.getHost());
        log.info(SMTP_PORT_LOG, javaMailSender.getPort());
        log.info(SOURCE_LOG, javaMailSender.getUsername());
        log.info(DESTINATION_LOG, smtp.getRecipientEmail());
        log.info(SUBJECT_LOG, smtp.getSubject());
        log.info(CONTENT_LOG, smtp.getText());
    }

    private static void addSmtpMetaData(final Smtp smtp,
                                       final JavaMailSenderImpl javaMailSender,
                                       final CommandResult result) {
        result.put(ALIAS, smtp.getAlias());
        result.put(SMTP_HOST, javaMailSender.getHost());
        result.put(SMTP_PORT, javaMailSender.getPort());
        result.put(SOURCE, javaMailSender.getUsername());
        result.put(DESTINATION, smtp.getRecipientEmail());
        result.put(SUBJECT, smtp.getSubject());
        result.put(TEXT, smtp.getText());
    }
}
