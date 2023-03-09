package com.knubisoft.cott.testing.framework.interpreter;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.AliasEnv;
import com.knubisoft.cott.testing.model.scenario.Sqs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.INCORRECT_SQS_PROCESSING;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.ALIAS_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SEND_ACTION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.QUEUE;

@Slf4j
@InterpreterForClass(Sqs.class)
public class SQSInterpreter extends AbstractInterpreter<Sqs> {

    @Autowired(required = false)
    private Map<AliasEnv, AmazonSQS> amazonSQS;

    public SQSInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Sqs sqs, final CommandResult result) {
        String queue = inject(sqs.getQueue());
        runSqsOperation(sqs, queue, result, sqs.getAlias());
    }

    private void runSqsOperation(final Sqs sqs,
                                 final String queueName,
                                 final CommandResult result,
                                 final String alias) {
        log.info(ALIAS_LOG, alias);
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (sqs.getSend() != null) {
            ResultUtil.addMessageBrokerGeneralMetaData(alias, SEND_ACTION, QUEUE, queueName, result);
            sendMessage(queueName, sqs.getSend(), aliasEnv, result);
        } else if (sqs.getReceive() != null) {
            ResultUtil.addMessageBrokerGeneralMetaData(alias, RECEIVE_ACTION, QUEUE, queueName, result);
            setContextBody(receiveAndCompareMessage(queueName, sqs.getReceive(), aliasEnv, result));
        } else {
            throw new DefaultFrameworkException(INCORRECT_SQS_PROCESSING);
        }
    }

    private void sendMessage(final String queue,
                             final String fileOrContent,
                             final AliasEnv aliasEnv,
                             final CommandResult result) {
        String message = inject(getContentIfFile(fileOrContent));
        LogUtil.logBrokerActionInfo(SEND_ACTION, queue, message);
        result.put("Message to send", PrettifyStringJson.getJSONResult(message));
        String queueUrl = createQueueIfNotExists(queue, aliasEnv);
        this.amazonSQS.get(aliasEnv).sendMessage(queueUrl, message);
    }

    private String receiveAndCompareMessage(final String queue,
                                            final String fileOrContent,
                                            final AliasEnv aliasEnv,
                                            final CommandResult result) {
        final String queueUrl = createQueueIfNotExists(queue, aliasEnv);
        final String message = receiveMessage(queueUrl, aliasEnv);
        LogUtil.logBrokerActionInfo(RECEIVE_ACTION, queue, message);
        compareMessage(fileOrContent, message, result);
        return message;
    }

    private String receiveMessage(final String queueUrl, final AliasEnv aliasEnv) {
        ReceiveMessageResult receiveMessageResult = this.amazonSQS.get(aliasEnv).receiveMessage(queueUrl);
        Iterator<Message> messages = receiveMessageResult.getMessages().iterator();
        return Optional.ofNullable(messages.hasNext() ? messages.next() : null)
                .map(Message::getBody)
                .map(String::new)
                .orElse(null);
    }

    private void compareMessage(final String fileOrContent, final String message, final CommandResult result) {
        final CompareBuilder comparator = newCompare()
                .withExpected(getContentIfFile(fileOrContent))
                .withActual(message);
        result.setExpected(PrettifyStringJson.getJSONResult(comparator.getExpected()));
        result.setActual(PrettifyStringJson.getJSONResult(message));
        comparator.exec();
    }

    private String createQueueIfNotExists(final String queue, final AliasEnv aliasEnv) {
        return this.amazonSQS.get(aliasEnv).createQueue(queue).getQueueUrl();
    }
}
