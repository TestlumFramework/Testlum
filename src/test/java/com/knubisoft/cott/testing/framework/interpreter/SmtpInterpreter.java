package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.env.AliasEnv;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Smtp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Objects;

@InterpreterForClass(Smtp.class)
public class SmtpInterpreter extends AbstractInterpreter<Smtp> {

    @Autowired(required = false)
    private Map<AliasEnv, JavaMailSenderImpl> javaMailSenderMap;

    public SmtpInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Smtp smtp, final CommandResult result) {
        AliasEnv aliasEnv = new AliasEnv(smtp.getAlias(), dependencies.getEnvironment());
        JavaMailSenderImpl javaMailSender = javaMailSenderMap.get(aliasEnv);
        LogUtil.logSmtpInfo(smtp, javaMailSender);
        ResultUtil.addSmtpMetaData(smtp, javaMailSender, result);
        sendEmail(smtp, javaMailSender);
    }

    private void sendEmail(final Smtp smtp, final JavaMailSenderImpl javaMailSender) {
        String username = Objects.requireNonNull(javaMailSender.getUsername());
        SimpleMailMessage simpleMailMessage = getSimpleMailMessage(smtp, username);
        try {
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
}
