package com.knubisoft.e2e.testing.framework.interpreter;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import com.knubisoft.e2e.testing.model.scenario.Sqs;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.ALIAS_LOG;

@Slf4j
@InterpreterForClass(Sqs.class)
public class SQSInterpreter extends AbstractInterpreter<Sqs> {

    private static final String SEND_ACTION = "send";
    private static final String RECEIVE_ACTION = "receive";

    @Autowired(required = false)
    private Map<String, AmazonSQS> amazonSQS;

    public SQSInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Sqs sqs, final CommandResult result) {
        String queue = inject(sqs.getQueue());
        result.put("queue", queue);
        runSqsOperation(sqs, queue, result, sqs.getAlias());
    }

    //CHECKSTYLE:OFF
    private void runSqsOperation(final Sqs sqs, final String queue,
                                 final CommandResult result, final String alias) {
        log.info(ALIAS_LOG, alias);
        if (sqs.getSend() != null) {
            result.put("action", SEND_ACTION);
            sendMessage(queue, sqs.getSend(), result, alias);
        } else if (sqs.getReceive() != null) {
            result.put("action", RECEIVE_ACTION);
            setContextBody(receiveAndCompareMessage(queue, sqs.getReceive(), result, alias));
        } else {
            throw new DefaultFrameworkException(LogMessage.INCORRECT_SQS_PROCESSING);
        }
    }
    //CHECKSTYLE:ON

    protected String receiveAndCompareMessage(final String queue, final String fileOrContent,
                                              final CommandResult result, final String alias) {
        final String queueUrl = createQueueIfNotExists(queue, alias);
        final String message = receiveMessage(queueUrl, alias);
        LogUtil.logBrokerActionInfo(RECEIVE_ACTION, queue, message);
        compareMessage(fileOrContent, result, message);
        return message;
    }

    private void compareMessage(final String fileOrContent, final CommandResult result, final String message) {
        final CompareBuilder comparator = newCompare()
                .withExpected(getContentIfFile(fileOrContent))
                .withActual(message);
        result.setExpected(comparator.getExpected());
        result.setActual(message);
        comparator.exec();
    }

    private String receiveMessage(final String queueUrl, final String alias) {
        ReceiveMessageResult receiveMessageResult = this.amazonSQS.get(alias).receiveMessage(queueUrl);
        Iterator<Message> messages = receiveMessageResult.getMessages().iterator();
        return Optional.ofNullable(messages.hasNext() ? messages.next() : null)
                .map(Message::getBody)
                .map(String::new)
                .orElse(null);
    }

    private void sendMessage(final String queue,
                             final String fileOrContent,
                             final CommandResult result,
                             final String alias) {
        String message = inject(getContentIfFile(fileOrContent));
        LogUtil.logBrokerActionInfo(SEND_ACTION, queue, fileOrContent);
        result.setActual(message);
        String queueUrl = createQueueIfNotExists(queue, alias);
        this.amazonSQS.get(alias).sendMessage(queueUrl, message);
    }

    private String createQueueIfNotExists(final String queue, final String alias) {
        return this.amazonSQS.get(alias).createQueue(queue).getQueueUrl();
    }
}
