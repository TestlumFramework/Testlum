package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.MESSAGE_STATUS;

@Slf4j
@InterpreterForClass(Twilio.class)
public class TwilioInterpreter extends AbstractInterpreter<Twilio> {

    @Autowired(required = false)
    private Map<AliasEnv, com.knubisoft.testlum.testing.model.global_config.Twilio> twilioSettings;

    public TwilioInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Twilio o, final CommandResult result) {
        Twilio twilio = injectCommand(o);
        AliasEnv aliasEnv = new AliasEnv(twilio.getAlias(), dependencies.getEnvironment());
        com.knubisoft.testlum.testing.model.global_config.Twilio twilioSetting = twilioSettings.get(aliasEnv);
        com.twilio.Twilio.init(twilioSetting.getAccountSid(), twilioSetting.getAuthToken());
        String twilioNumber = twilioSetting.getTwilioNumber();
        LogUtil.logTwilioInfo(twilio, twilioNumber);
        ResultUtil.addTwilioMetaData(twilio, twilioNumber, result);
        sendSms(twilio, twilioNumber);
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
}
