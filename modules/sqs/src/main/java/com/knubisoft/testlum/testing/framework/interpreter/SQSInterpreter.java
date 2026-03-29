package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractMessageBrokerInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.ReceiveSqsMessage;
import com.knubisoft.testlum.testing.model.scenario.SendSqsMessage;
import com.knubisoft.testlum.testing.model.scenario.Sqs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;
import java.util.Map;


@Slf4j
@InterpreterForClass(Sqs.class)
public class SQSInterpreter extends AbstractMessageBrokerInterpreter<Sqs> {

    private static final String QUEUE_LOG = LogFormat.table("Queue");
    private static final String MESSAGE_DEDUPLICATION_ID_LOG = LogFormat.table("Deduplication Id");
    private static final String MESSAGE_GROUP_ID_LOG = LogFormat.table("Message Group Id");
    private static final String DELAY_SECONDS_LOG = LogFormat.table("Delay Seconds");
    private static final String MAX_NUMBER_OF_MESSAGES_LOG = LogFormat.table("Max Number of Messages");
    private static final String WAIT_TIME_SECONDS_LOG = LogFormat.table("Wait Time Seconds");
    private static final String RECEIVE_REQUEST_ATTEMPT_ID_LOG = LogFormat.table("Attempt Id");
    private static final String VISIBILITY_TIMEOUT_LOG = LogFormat.table("Visibility Timeout");

    private static final String QUEUE = "Queue";
    private static final String COMMENT_FOR_SQS_SEND_ACTION = "Send message to SQS";
    private static final String COMMENT_FOR_SQS_RECEIVE_ACTION = "Receive message from SQS";
    private static final String SQS_DELAY_SECONDS = "Delay";
    private static final String SQS_MESSAGE_DUPLICATION_ID = "Message duplication id";
    private static final String SQS_MESSAGE_GROUP_ID = "Message group id";
    private static final String SQS_MAX_NUMBER_OF_MESSAGES = "Max number of messages";
    private static final String SQS_VISIBILITY_TIMEOUT = "Visibility timeout";
    private static final String SQS_WAIT_TIME_SECONDS = "Wait time";
    private static final String SQS_RECEIVE_REQUEST_ATTEMPT_ID = "Receive request attempt id";

    private static final String SQS_NOT_CONFIGURED = "SQS integration is not configured for this environment";
    private static final String INCORRECT_SQS_PROCESSING = "Incorrect SQS processing";

    @Autowired(required = false)
    private Map<AliasEnv, SqsClient> sqsClient;

    public SQSInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void validate() {
        if (sqsClient == null) {
            throw new DefaultFrameworkException(SQS_NOT_CONFIGURED);
        }
    }

    @Override
    protected String getAlias(final Sqs command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Sqs command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<Object> getActions(final Sqs command) {
        return command.getSendOrReceive();
    }

    @Override
    protected void processAction(final Object action, final String alias, final CommandResult result) {
        processSqsAction(action, alias, result);
    }

    private void processSqsAction(final Object action, final String alias, final CommandResult result) {
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (action instanceof SendSqsMessage send) {
            sendMessage(send, aliasEnv, result);
        } else if (action instanceof ReceiveSqsMessage receive) {
            receiveMessages(receive, aliasEnv, result);
        } else {
            throw new DefaultFrameworkException(INCORRECT_SQS_PROCESSING);
        }
    }

    private void sendMessage(final SendSqsMessage send, final AliasEnv aliasEnv, final CommandResult result) {
        SendMessageRequest sendRequest = createSendRequest(send, aliasEnv);
        String message = sendRequest.messageBody();
        logSQSSendInfo(send, message);
        addSqsSendInfo(send, aliasEnv.getAlias(), result);
        result.put(MESSAGE_TO_SEND, stringPrettifier.asJsonResult(message));
        sqsClient.get(aliasEnv).sendMessage(sendRequest);
    }

    private SendMessageRequest createSendRequest(final SendSqsMessage send, final AliasEnv aliasEnv) {
        String queue = createQueueIfNotExists(send.getQueue(), aliasEnv);
        return SendMessageRequest.builder()
                .queueUrl(queue)
                .messageBody(getValue(send.getValue(), send.getFile()))
                .delaySeconds(send.getDelaySeconds())
                .messageDeduplicationId(send.getMessageDeduplicationId())
                .messageGroupId(send.getMessageGroupId())
                .build();
    }

    private void receiveMessages(final ReceiveSqsMessage receive,
                                 final AliasEnv aliasEnv,
                                 final CommandResult result) {
        final String expectedMessages = getValue(receive.getValue(), receive.getFile());
        logSQSReceiveInfo(receive, expectedMessages);
        addSqsReceiveInfo(receive, aliasEnv.getAlias(), result);
        final List<Object> actualMessages = receiveMessages(receive, aliasEnv);
        compareMessages(actualMessages, expectedMessages, result);
    }

    private List<Object> receiveMessages(final ReceiveSqsMessage receive, final AliasEnv aliasEnv) {
        ReceiveMessageRequest receiveMessageRequest = createReceiveRequest(receive, aliasEnv);
        ReceiveMessageResponse receiveMessageResult = sqsClient.get(aliasEnv).receiveMessage(receiveMessageRequest);
        return receiveMessageResult.messages()
                .stream()
                .map(message -> message.body().replaceAll(DelimiterConstant.REGEX_MANY_SPACES, StringUtils.EMPTY))
                .map(jacksonService::toJsonObject)
                .toList();
    }

    private ReceiveMessageRequest createReceiveRequest(final ReceiveSqsMessage receive, final AliasEnv aliasEnv) {
        String queue = createQueueIfNotExists(receive.getQueue(), aliasEnv);
        return ReceiveMessageRequest.builder()
                .queueUrl(queue)
                .maxNumberOfMessages(receive.getMaxNumberOfMessages())
                .visibilityTimeout(receive.getVisibilityTimeout())
                .waitTimeSeconds(receive.getWaitTimeSeconds())
                .receiveRequestAttemptId(receive.getReceiveRequestAttemptId())
                .build();
    }

    private String createQueueIfNotExists(final String queue, final AliasEnv aliasEnv) {
        try {
            return sqsClient.get(aliasEnv)
                    .getQueueUrl(getQueueUrlRequest -> getQueueUrlRequest.queueName(queue))
                    .queueUrl();
        } catch (final QueueDoesNotExistException e) {
            return sqsClient.get(aliasEnv)
                    .createQueue(createQueueRequest -> createQueueRequest.queueName(queue))
                    .queueUrl();
        }
    }

    private void logSQSSendInfo(final SendSqsMessage send, final String content) {
        logMessageBrokerMetaData(SEND_ACTION, QUEUE_LOG, send.getQueue(), content);
        logIfNotNull(DELAY_SECONDS_LOG, send.getDelaySeconds());
        logIfNotNull(MESSAGE_DEDUPLICATION_ID_LOG, send.getMessageDeduplicationId());
        logIfNotNull(MESSAGE_GROUP_ID_LOG, send.getMessageGroupId());
    }

    private void logSQSReceiveInfo(final ReceiveSqsMessage receive, final String content) {
        logMessageBrokerMetaData(RECEIVE_ACTION, QUEUE_LOG, receive.getQueue(), content);
        logIfNotNull(MAX_NUMBER_OF_MESSAGES_LOG, receive.getMaxNumberOfMessages());
        logIfNotNull(WAIT_TIME_SECONDS_LOG, receive.getWaitTimeSeconds());
        logIfNotNull(RECEIVE_REQUEST_ATTEMPT_ID_LOG, receive.getReceiveRequestAttemptId());
        logIfNotNull(VISIBILITY_TIMEOUT_LOG, receive.getVisibilityTimeout());
    }

    private void addSqsSendInfo(final SendSqsMessage sendAction,
                                final String alias,
                                final CommandResult result) {
        result.setCommandKey(SEND);
        result.setComment(COMMENT_FOR_SQS_SEND_ACTION);
        addMessageBrokerGeneralMetaData(alias, SEND, QUEUE, sendAction.getQueue(), result);
        addSqsAdditionalMetaDataForSendAction(sendAction, result);
    }

    private void addSqsReceiveInfo(final ReceiveSqsMessage receiveAction,
                                   final String alias,
                                   final CommandResult result) {
        result.setCommandKey(RECEIVE);
        result.setComment(COMMENT_FOR_SQS_RECEIVE_ACTION);
        addMessageBrokerGeneralMetaData(alias, RECEIVE, QUEUE, receiveAction.getQueue(), result);
        addSqsAdditionalMetaDataForReceiveAction(receiveAction, result);
    }

    private void addSqsAdditionalMetaDataForSendAction(final SendSqsMessage sendAction, final CommandResult result) {
        putIfNotNull(result, SQS_DELAY_SECONDS, sendAction.getDelaySeconds());
        putIfNotBlank(result, SQS_MESSAGE_DUPLICATION_ID, sendAction.getMessageDeduplicationId());
        putIfNotBlank(result, SQS_MESSAGE_GROUP_ID, sendAction.getMessageGroupId());
    }

    private void addSqsAdditionalMetaDataForReceiveAction(final ReceiveSqsMessage receiveAction,
                                                          final CommandResult result) {
        putIfNotNull(result, SQS_MAX_NUMBER_OF_MESSAGES, receiveAction.getMaxNumberOfMessages());
        putIfNotNull(result, SQS_VISIBILITY_TIMEOUT, receiveAction.getVisibilityTimeout());
        putIfNotNull(result, SQS_WAIT_TIME_SECONDS, receiveAction.getWaitTimeSeconds());
        putIfNotBlank(result, SQS_RECEIVE_REQUEST_ATTEMPT_ID, receiveAction.getReceiveRequestAttemptId());
    }
}
