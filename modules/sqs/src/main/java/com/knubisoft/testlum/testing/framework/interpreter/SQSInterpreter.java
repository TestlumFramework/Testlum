package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@InterpreterForClass(Sqs.class)
public class SQSInterpreter extends AbstractInterpreter<Sqs> {

    //LOGS
    private static final String SEND_ACTION = "send";
    private static final String RECEIVE_ACTION = "receive";
    private static final String CONTENT_FORMAT = String.format("%n%19s| %-23s|", StringUtils.EMPTY, StringUtils.EMPTY);

    private static final String QUEUE_LOG = LogFormat.table("Queue");
    private static final String CONTENT_LOG = LogFormat.table("Content");
    private static final String ACTION_LOG = LogFormat.table("Action");
    private static final String MESSAGE_DEDUPLICATION_ID_LOG = LogFormat.table("Deduplication Id");
    private static final String MESSAGE_GROUP_ID_LOG = LogFormat.table("Message Group Id");
    private static final String DELAY_SECONDS_LOG = LogFormat.table("Delay Seconds");
    private static final String MAX_NUMBER_OF_MESSAGES_LOG = LogFormat.table("Max Number of Messages");
    private static final String WAIT_TIME_SECONDS_LOG = LogFormat.table("Wait Time Seconds");
    private static final String RECEIVE_REQUEST_ATTEMPT_ID_LOG = LogFormat.table("Attempt Id");
    private static final String VISIBILITY_TIMEOUT_LOG = LogFormat.table("Visibility Timeout");

    private static final String NEW_LOG_LINE = String.format("%n%19s| ", StringUtils.EMPTY);

    private static final String ALIAS_LOG = LogFormat.table("Alias");
    private static final String COMMAND_LOG = LogFormat.withCyan("------- Command #{} - {} -------");
    private static final String EXCEPTION_LOG = LogFormat.withRed(
            "----------------    EXCEPTION    -----------------"
                    + NEW_LOG_LINE + "{}" + NEW_LOG_LINE
                    + "--------------------------------------------------");

    //RESULT
    private static final String MESSAGE_TO_SEND = "Message to send";
    private static final String ALIAS = "Alias";
    private static final String QUEUE = "Queue";
    private static final String RECEIVE = "Receive";
    private static final String SEND = "Send";
    private static final String ACTION = "Action";
    private static final String COMMENT_FOR_SQS_SEND_ACTION = "Send message to SQS";
    private static final String COMMENT_FOR_SQS_RECEIVE_ACTION = "Receive message from SQS";
    private static final String SQS_DELAY_SECONDS = "Delay";
    private static final String SQS_MESSAGE_DUPLICATION_ID = "Message duplication id";
    private static final String SQS_MESSAGE_GROUP_ID = "Message group id";
    private static final String SQS_MAX_NUMBER_OF_MESSAGES = "Max number of messages";
    private static final String SQS_VISIBILITY_TIMEOUT = "Visibility timeout";
    private static final String SQS_WAIT_TIME_SECONDS = "Wait time";
    private static final String SQS_RECEIVE_REQUEST_ATTEMPT_ID = "Receive request attempt id";
    private static final String STEP_FAILED = "Step failed";

    private static final String INCORRECT_SQS_PROCESSING = "Incorrect SQS processing";

    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    private Map<AliasEnv, SqsClient> sqsClient;

    public SQSInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Sqs o, final CommandResult result) {
        Sqs sqs = injectCommand(o);
        checkAlias(sqs);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (Object action : sqs.getSendOrReceive()) {
            log.info(COMMAND_LOG, dependencies.getPosition().incrementAndGet(), action.getClass().getSimpleName());
            CommandResult commandResult = newCommandResultInstance(dependencies.getPosition().get());
            subCommandsResult.add(commandResult);
            processEachAction(action, sqs.getAlias(), commandResult);
        }
        setExecutionResultIfSubCommandsFailed(result);
    }

    private void checkAlias(final Sqs sqs) {
        if (sqs.getAlias() == null) {
            sqs.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private void processEachAction(final Object action, final String alias, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        log.info(ALIAS_LOG, alias);
        try {
            processSqsAction(action, alias, result);
        } catch (Exception e) {
            logException(e);
            setExceptionResult(result, e);
            checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getDuration().toMillis());
            stopWatch.stop();
        }
    }

    private void processSqsAction(final Object action, final String alias, final CommandResult result) {
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (action instanceof SendSqsMessage) {
            sendMessage((SendSqsMessage) action, aliasEnv, result);
        } else if (action instanceof ReceiveSqsMessage) {
            receiveMessages((ReceiveSqsMessage) action, aliasEnv, result);
        } else {
            throw new DefaultFrameworkException(INCORRECT_SQS_PROCESSING);
        }
    }

    private void sendMessage(final SendSqsMessage send, final AliasEnv aliasEnv, final CommandResult result) {
        SendMessageRequest sendRequest = createSendRequest(send, aliasEnv);
        String message = sendRequest.messageBody();
        logSQSSendInfo(send, message);
        addSqsSendInfo(send, aliasEnv.getAlias(), result);
        result.put(MESSAGE_TO_SEND, StringPrettifier.asJsonResult(message));
        this.sqsClient.get(aliasEnv).sendMessage(sendRequest);
    }

    private SendMessageRequest createSendRequest(final SendSqsMessage send, final AliasEnv aliasEnv) {
        String queue = createQueueIfNotExists(send.getQueue(), aliasEnv);
        return SendMessageRequest.builder()
                .queueUrl(queue)
                .messageBody(getMessageToSend(send))
                .delaySeconds(send.getDelaySeconds())
                .messageDeduplicationId(send.getMessageDeduplicationId())
                .messageGroupId(send.getMessageGroupId())
                .build();
    }

    private void receiveMessages(final ReceiveSqsMessage receive,
                                 final AliasEnv aliasEnv,
                                 final CommandResult result) {
        final String expectedMessages = getMessageToReceive(receive);
        logSQSReceiveInfo(receive, expectedMessages);
        addSqsReceiveInfo(receive, aliasEnv.getAlias(), result);
        final List<Object> actualMessages = receiveMessages(receive, aliasEnv);
        compareMessage(expectedMessages, actualMessages, result);
    }

    private List<Object> receiveMessages(final ReceiveSqsMessage receive, final AliasEnv aliasEnv) {
        ReceiveMessageRequest receiveMessageRequest = createReceiveRequest(receive, aliasEnv);
        ReceiveMessageResponse receiveMessageResult =
                this.sqsClient.get(aliasEnv).receiveMessage(receiveMessageRequest);
        return receiveMessageResult.messages()
                .stream()
                .map(message -> message.body().replaceAll(DelimiterConstant.REGEX_MANY_SPACES, StringUtils.EMPTY))
                .map(JacksonMapperUtil::toJsonObject)
                .collect(Collectors.toList());
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

    private void compareMessage(final String expectedContent, final List<Object> messages, final CommandResult result) {
        final CompareBuilder comparator = newCompare()
                .withExpected(expectedContent)
                .withActual(messages);
        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));
        result.setActual(StringPrettifier.asJsonResult(toString(messages)));
        comparator.exec();
    }

    private String createQueueIfNotExists(final String queue, final AliasEnv aliasEnv) {
        try {
            return sqsClient.get(aliasEnv)
                    .getQueueUrl(getQueueUrlRequest -> getQueueUrlRequest.queueName(queue))
                    .queueUrl();
        } catch (final QueueDoesNotExistException e) {
            return this.sqsClient.get(aliasEnv)
                    .createQueue(createQueueRequest -> createQueueRequest.queueName(queue))
                    .queueUrl();
        }
    }

    private String getMessageToSend(final SendSqsMessage send) {
        return getValue(send.getValue(), send.getFile());
    }

    private String getMessageToReceive(final ReceiveSqsMessage receive) {
        return getValue(receive.getValue(), receive.getFile());
    }

    private String getValue(final String message, final String file) {
        return StringUtils.isNotBlank(message) ? message : getContentIfFile(file);
    }

    //LOGS
    private void logSQSSendInfo(final SendSqsMessage send, final String content) {
        logMessageBrokerGeneralMetaData(SEND_ACTION, send.getQueue(), content);
        logIfNotNull(DELAY_SECONDS_LOG, send.getDelaySeconds());
        logIfNotNull(MESSAGE_DEDUPLICATION_ID_LOG, send.getMessageDeduplicationId());
        logIfNotNull(MESSAGE_GROUP_ID_LOG, send.getMessageGroupId());
    }

    private void logSQSReceiveInfo(final ReceiveSqsMessage receive, final String content) {
        logMessageBrokerGeneralMetaData(RECEIVE_ACTION, receive.getQueue(), content);
        logIfNotNull(MAX_NUMBER_OF_MESSAGES_LOG, receive.getMaxNumberOfMessages());
        logIfNotNull(WAIT_TIME_SECONDS_LOG, receive.getWaitTimeSeconds());
        logIfNotNull(RECEIVE_REQUEST_ATTEMPT_ID_LOG, receive.getReceiveRequestAttemptId());
        logIfNotNull(VISIBILITY_TIMEOUT_LOG, receive.getVisibilityTimeout());
    }

    private void logMessageBrokerGeneralMetaData(final String action,
                                                 final String topicOrRoutingKeyOrQueueValue,
                                                 final String content) {
        log.info(ACTION_LOG, action.toUpperCase(Locale.ROOT));
        log.info(QUEUE_LOG, topicOrRoutingKeyOrQueueValue);
        log.info(CONTENT_LOG, StringPrettifier.asJsonResult(content.replaceAll(
                        DelimiterConstant.REGEX_MANY_SPACES,
                        DelimiterConstant.SPACE))
                .replaceAll(LogFormat.newLine(), CONTENT_FORMAT));
    }

    private void logIfNotNull(final String title, final Object data) {
        if (Objects.nonNull(data)) {
            log.info(title, data);
        }
    }

    private void logException(final Exception ex) {
        if (StringUtils.isNotBlank(ex.getMessage())) {
            log.error(EXCEPTION_LOG, ex.getMessage().replaceAll(LogFormat.newLine(), NEW_LOG_LINE));
        } else {
            log.error(EXCEPTION_LOG, ex.toString());
        }
    }

    //RESULT
    private CommandResult newCommandResultInstance(final int number, final AbstractCommand... command) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        if (Objects.nonNull(command) && command.length > 0) {
            commandResult.setCommandKey(command[0].getClass().getSimpleName());
        }
        return commandResult;
    }

    private void addSqsSendInfo(final SendSqsMessage sendAction,
                                final String alias,
                                final CommandResult result) {
        result.setCommandKey(SEND);
        result.setComment(COMMENT_FOR_SQS_SEND_ACTION);
        addMessageBrokerGeneralMetaData(alias, SEND, sendAction.getQueue(), result);
        addSqsAdditionalMetaDataForSendAction(sendAction, result);
    }

    private void addSqsReceiveInfo(final ReceiveSqsMessage receiveAction,
                                   final String alias,
                                   final CommandResult result) {
        result.setCommandKey(RECEIVE);
        result.setComment(COMMENT_FOR_SQS_RECEIVE_ACTION);
        addMessageBrokerGeneralMetaData(alias, RECEIVE, receiveAction.getQueue(), result);
        addSqsAdditionalMetaDataForReceiveAction(receiveAction, result);
    }

    private void addMessageBrokerGeneralMetaData(final String alias,
                                                 final String action,
                                                 final String destinationValue,
                                                 final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ACTION, action);
        result.put(QUEUE, destinationValue);
    }

    private void addSqsAdditionalMetaDataForSendAction(final SendSqsMessage sendAction, final CommandResult result) {
        if (Objects.nonNull(sendAction.getDelaySeconds())) {
            result.put(SQS_DELAY_SECONDS, sendAction.getDelaySeconds());
        }
        if (StringUtils.isNotBlank(sendAction.getMessageDeduplicationId())) {
            result.put(SQS_MESSAGE_DUPLICATION_ID, sendAction.getMessageDeduplicationId());
        }
        if (StringUtils.isNotBlank(sendAction.getMessageGroupId())) {
            result.put(SQS_MESSAGE_GROUP_ID, sendAction.getMessageGroupId());
        }
    }

    private void addSqsAdditionalMetaDataForReceiveAction(final ReceiveSqsMessage receiveAction,
                                                          final CommandResult result) {
        if (Objects.nonNull(receiveAction.getMaxNumberOfMessages())) {
            result.put(SQS_MAX_NUMBER_OF_MESSAGES, receiveAction.getMaxNumberOfMessages());
        }
        if (Objects.nonNull(receiveAction.getVisibilityTimeout())) {
            result.put(SQS_VISIBILITY_TIMEOUT, receiveAction.getVisibilityTimeout());
        }
        if (Objects.nonNull(receiveAction.getWaitTimeSeconds())) {
            result.put(SQS_WAIT_TIME_SECONDS, receiveAction.getWaitTimeSeconds());
        }
        if (StringUtils.isNotBlank(receiveAction.getReceiveRequestAttemptId())) {
            result.put(SQS_RECEIVE_REQUEST_ATTEMPT_ID, receiveAction.getReceiveRequestAttemptId());
        }
    }

    private void setExecutionResultIfSubCommandsFailed(final CommandResult result) {
        List<CommandResult> subCommandsResult = result.getSubCommandsResult();
        if (subCommandsResult.stream().anyMatch(step -> !step.isSkipped() && !step.isSuccess())) {
            Exception exception = subCommandsResult
                    .stream()
                    .filter(subCommand -> !subCommand.isSuccess())
                    .findFirst()
                    .map(CommandResult::getException)
                    .orElseGet(() -> new DefaultFrameworkException(STEP_FAILED));
            setExceptionResult(result, exception);
        }
    }

    private void setExceptionResult(final CommandResult result, final Exception exception) {
        result.setSuccess(false);
        result.setException(exception);
    }
}
