package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@InterpreterForClass(Twilio.class)
public class TwilioInterpreter extends AbstractInterpreter<Twilio> {

    @Autowired(required = false)
    private Map<String, com.knubisoft.cott.testing.model.global_config.Twilio> twilioSettings;

    public TwilioInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Twilio twilio, final CommandResult result) {
        com.knubisoft.cott.testing.model.global_config.Twilio twilioSetting = twilioSettings.get(twilio.getAlias());
        com.twilio.Twilio.init(twilioSetting.getAccountSid(), twilioSetting.getAuthToken());
        String twilioNumber = twilioSetting.getTwilioNumber();
        LogUtil.logTwilioInfo(twilio, twilioNumber);
        ResultUtil.addTwilioMetaData(twilio, twilioNumber, result);
        sendSms(twilio, twilioNumber);
    }

    private void sendSms(final Twilio twilio, final String twilioNumber) {
        MessageCreator creator = getMessageCreator(twilio, twilioNumber);
        creator.create();
    }

    private MessageCreator getMessageCreator(final Twilio twilio, final String twilioNumber) {
        PhoneNumber from = new PhoneNumber(twilioNumber);
        PhoneNumber to = new PhoneNumber(twilio.getDestinationPhoneNumber());
        String message = twilio.getMessage();
        return Message.creator(to, from, message);
    }
}
