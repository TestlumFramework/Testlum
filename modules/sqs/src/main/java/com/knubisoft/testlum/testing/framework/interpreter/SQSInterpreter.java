package com.knubisoft.testlum.testing.framework.interpreter;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
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
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.ReceiveSqsMessage;
import com.knubisoft.testlum.testing.model.scenario.SendSqsMessage;
import com.knubisoft.testlum.testing.model.scenario.Sqs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.REGEX_MANY_SPACES;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@InterpreterForClass(Sqs.class)
public class SQSInterpreter extends AbstractInterpreter<Sqs> {

    //LOGS
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String SEND_ACTION = "send";
    private static final String RECEIVE_ACTION = "receive";
    private static final String CONTENT_FORMAT = format("%n%19s| %-23s|", EMPTY, EMPTY);
    private static final String QUEUE_LOG = format(TABLE_FORMAT, "Queue", "{}");
    private static final String CONTENT_LOG = format(TABLE_FORMAT, "Content", "{}");
    private static final String ACTION_LOG = format(TABLE_FORMAT, "Action", "{}");
    private static final String MESSAGE_DEDUPLICATION_ID_LOG = format(TABLE_FORMAT, "Deduplication Id", "{}");
    private static final String MESSAGE_GROUP_ID_LOG = format(TABLE_FORMAT, "Message Group Id", "{}");
    private static final String DELAY_SECONDS_LOG = format(TABLE_FORMAT, "Delay Seconds", "{}");
    private static final String MAX_NUMBER_OF_MESSAGES_LOG = format(TABLE_FORMAT, "Max Number of Messages", "{}");
    private static final String WAIT_TIME_SECONDS_LOG = format(TABLE_FORMAT, "Wait Time Seconds", "{}");
    private static final String RECEIVE_REQUEST_ATTEMPT_ID_LOG = format(
            TABLE_FORMAT, "Attempt Id", "{}");
    private static final String VISIBILITY_TIMEOUT_LOG = format(TABLE_FORMAT, "Visibility Timeout", "{}");
    private static final String NEW_LOG_LINE = format("%n%19s| ", EMPTY);
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_CYAN = "\u001b[36m";
    private static final String ANSI_RESET = "\u001b[0m";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String COMMAND_LOG = ANSI_CYAN + "------- Command #{} - {} -------" + ANSI_RESET;
    private static final String EXCEPTION_LOG = ANSI_RED
            + "----------------    EXCEPTION    -----------------"
            + NEW_LOG_LINE + "{}" + NEW_LOG_LINE
            + "--------------------------------------------------" + ANSI_RESET;

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

    @Autowired(required = false)
    private Map<AliasEnv, AmazonSQS> amazonSQS;

    public SQSInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Sqs o, final CommandResult result) {
        Sqs sqs = injectCommand(o);
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
            result.setExecutionTime(stopWatch.getTime());
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
        String message = sendRequest.getMessageBody();
        logSQSSendInfo(send, message);
        addSqsSendInfo(send, aliasEnv.getAlias(), result);
        result.put(MESSAGE_TO_SEND, StringPrettifier.asJsonResult(message));
        this.amazonSQS.get(aliasEnv).sendMessage(sendRequest);
    }

    private SendMessageRequest createSendRequest(final SendSqsMessage send, final AliasEnv aliasEnv) {
        SendMessageRequest sendRequest = new SendMessageRequest();
        String queue = createQueueIfNotExists(send.getQueue(), aliasEnv);
        sendRequest.setQueueUrl(queue);
        sendRequest.setMessageBody(getMessageToSend(send));
        sendRequest.setDelaySeconds(send.getDelaySeconds());
        sendRequest.setMessageDeduplicationId(send.getMessageDeduplicationId());
        sendRequest.setMessageGroupId(send.getMessageGroupId());
        return sendRequest;
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
        ReceiveMessageResult receiveMessageResult = this.amazonSQS.get(aliasEnv).receiveMessage(receiveMessageRequest);
        return receiveMessageResult.getMessages()
                .stream()
                .map(message ->
                        message.getBody().replaceAll(DelimiterConstant.REGEX_MANY_SPACES, DelimiterConstant.EMPTY))
                .map(JacksonMapperUtil::toJsonObject)
                .collect(Collectors.toList());
    }

    private ReceiveMessageRequest createReceiveRequest(final ReceiveSqsMessage receive, final AliasEnv aliasEnv) {
        ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest();
        String queue = createQueueIfNotExists(receive.getQueue(), aliasEnv);
        receiveRequest.setQueueUrl(queue);
        receiveRequest.setMaxNumberOfMessages(receive.getMaxNumberOfMessages());
        receiveRequest.setVisibilityTimeout(receive.getVisibilityTimeout());
        receiveRequest.setWaitTimeSeconds(receive.getWaitTimeSeconds());
        receiveRequest.setReceiveRequestAttemptId(receive.getReceiveRequestAttemptId());
        return receiveRequest;
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
            return amazonSQS.get(aliasEnv).getQueueUrl(queue).getQueueUrl();
        } catch (QueueDoesNotExistException e) {
            return this.amazonSQS.get(aliasEnv).createQueue(queue).getQueueUrl();
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
        log.info(CONTENT_LOG, StringPrettifier.asJsonResult(content.replaceAll(REGEX_MANY_SPACES, SPACE))
                .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
    }

    private void logIfNotNull(final String title, final Object data) {
        if (nonNull(data)) {
            log.info(title, data);
        }
    }

    private void logException(final Exception ex) {
        if (isNotBlank(ex.getMessage())) {
            log.error(EXCEPTION_LOG, ex.getMessage().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        } else {
            log.error(EXCEPTION_LOG, ex.toString());
        }
    }

    //RESULT
    private CommandResult newCommandResultInstance(final int number, final AbstractCommand... command) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        if (nonNull(command) && command.length > 0) {
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
        if (nonNull(sendAction.getDelaySeconds())) {
            result.put(SQS_DELAY_SECONDS, sendAction.getDelaySeconds());
        }
        if (isNotBlank(sendAction.getMessageDeduplicationId())) {
            result.put(SQS_MESSAGE_DUPLICATION_ID, sendAction.getMessageDeduplicationId());
        }
        if (isNotBlank(sendAction.getMessageGroupId())) {
            result.put(SQS_MESSAGE_GROUP_ID, sendAction.getMessageGroupId());
        }
    }

    private void addSqsAdditionalMetaDataForReceiveAction(final ReceiveSqsMessage receiveAction,
                                                          final CommandResult result) {
        if (nonNull(receiveAction.getMaxNumberOfMessages())) {
            result.put(SQS_MAX_NUMBER_OF_MESSAGES, receiveAction.getMaxNumberOfMessages());
        }
        if (nonNull(receiveAction.getVisibilityTimeout())) {
            result.put(SQS_VISIBILITY_TIMEOUT, receiveAction.getVisibilityTimeout());
        }
        if (nonNull(receiveAction.getWaitTimeSeconds())) {
            result.put(SQS_WAIT_TIME_SECONDS, receiveAction.getWaitTimeSeconds());
        }
        if (isNotBlank(receiveAction.getReceiveRequestAttemptId())) {
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
