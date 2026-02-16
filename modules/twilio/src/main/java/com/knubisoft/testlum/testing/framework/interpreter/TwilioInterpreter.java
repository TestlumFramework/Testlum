package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
@InterpreterForClass(Twilio.class)
public class TwilioInterpreter extends AbstractInterpreter<Twilio> {

    private static final String FROM_PHONE_NUMBER_LOG = LogFormat.table("'From' phone number");
    private static final String TO_PHONE_NUMBER_LOG = LogFormat.table("'To' phone number");
    private static final String MESSAGE_LOG = LogFormat.table("Message");
    private static final String MESSAGE_STATUS = LogFormat.table("Message status");

    private static final String ALIAS = "Alias";
    private static final String FROM = "From";
    private static final String TO = "To";
    private static final String MESSAGE = "Message";

    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    private Map<AliasEnv, com.knubisoft.testlum.testing.model.global_config.Twilio> twilioSettings;

    public TwilioInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Twilio o, final CommandResult result) {
        Twilio twilio = injectCommand(o);
        checkAlias(twilio);
        AliasEnv aliasEnv = new AliasEnv(twilio.getAlias(), dependencies.getEnvironment());
        com.knubisoft.testlum.testing.model.global_config.Twilio twilioSetting = twilioSettings.get(aliasEnv);
        com.twilio.Twilio.init(twilioSetting.getAccountSid(), twilioSetting.getAuthToken());
        String twilioNumber = twilioSetting.getTwilioNumber();
        logTwilioInfo(twilio, twilioNumber);
        addTwilioMetaData(twilio, twilioNumber, result);
        sendSms(twilio, twilioNumber);
    }

    private void checkAlias(final Twilio twilio) {
        if (twilio.getAlias() == null) {
            twilio.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private void sendSms(final Twilio twilio, final String twilioNumber) {
        MessageCreator creator = getMessageCreator(twilio, twilioNumber);
        Message message = creator.create();
        log.info(MESSAGE_STATUS, message.getStatus());
    }

    private MessageCreator getMessageCreator(final Twilio twilio, final String twilioNumber) {
        PhoneNumber from = new PhoneNumber(twilioNumber);
        PhoneNumber to = new PhoneNumber(twilio.getDestinationPhoneNumber());
        String message = twilio.getMessage();
        return Message.creator(to, from, message);
    }

    private void logTwilioInfo(final Twilio twilio, final String twilioPhoneNumber) {
        log.info(FROM_PHONE_NUMBER_LOG, twilioPhoneNumber);
        log.info(TO_PHONE_NUMBER_LOG, twilio.getDestinationPhoneNumber());
        log.info(MESSAGE_LOG, twilio.getMessage());
    }

    private static void addTwilioMetaData(final Twilio twilio, final String twilioNumber, final CommandResult result) {
        result.put(ALIAS, twilio.getAlias());
        result.put(FROM, twilioNumber);
        result.put(TO, twilio.getDestinationPhoneNumber());
        result.put(MESSAGE, twilio.getMessage());
    }
}
