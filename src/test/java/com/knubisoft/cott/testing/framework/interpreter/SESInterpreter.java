package com.knubisoft.cott.testing.framework.interpreter;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.AliasEnv;
import com.knubisoft.cott.testing.model.scenario.Ses;
import com.knubisoft.cott.testing.model.scenario.SesBody;
import com.knubisoft.cott.testing.model.scenario.SesMessage;
import com.knubisoft.cott.testing.model.scenario.SesTextContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
@InterpreterForClass(Ses.class)
public class SESInterpreter extends AbstractInterpreter<Ses> {

    @Autowired(required = false)
    private Map<AliasEnv, AmazonSimpleEmailService> amazonSimpleEmailService;

    public SESInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Ses ses, final CommandResult result) {
        LogUtil.logSesInfo(ses);
        ResultUtil.addSesMetaData(ses, result);
        AliasEnv aliasEnv = new AliasEnv(ses.getAlias(), dependencies.getEnv());
        verify(ses, aliasEnv);
        sendEmail(ses, aliasEnv);
    }

    private void verify(final Ses ses, final AliasEnv aliasEnv) {
        VerifyEmailAddressRequest verifyEmailAddressRequest = createVerifyEmailAddress(ses);
        amazonSimpleEmailService.get(aliasEnv).verifyEmailAddress(verifyEmailAddressRequest);
    }

    private void sendEmail(final Ses ses, final AliasEnv aliasEnv) {
        SendEmailRequest sendEmailRequest = createSendEmailRequest(ses);
        LogUtil.logSESMessage(sendEmailRequest.getMessage());
        amazonSimpleEmailService.get(aliasEnv).sendEmail(sendEmailRequest);
    }

    private VerifyEmailAddressRequest createVerifyEmailAddress(final Ses ses) {
        return new VerifyEmailAddressRequest()
                .withEmailAddress(ses.getSource());
    }

    private SendEmailRequest createSendEmailRequest(final Ses ses) {
        return new SendEmailRequest()
                .withDestination(createDestination(ses))
                .withMessage(createMessage(ses))
                .withSource(ses.getSource());
    }

    private Destination createDestination(final Ses ses) {
        return new Destination()
                .withToAddresses(ses.getDestination());
    }

    private Message createMessage(final Ses ses) {
        SesMessage message = ses.getMessage();
        return new Message()
                .withBody(createBody(message.getBody()))
                .withSubject(createContent(message.getSubject()));
    }

    private Body createBody(final SesBody body) {
        return new Body()
                .withHtml(createContent(body.getHtml()))
                .withText(createContent(body.getText()));
    }

    private Content createContent(final SesTextContent textContent) {
        return new Content()
                .withCharset(textContent.getCharset())
                .withData(textContent.getValue());
    }
}
