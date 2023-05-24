package com.knubisoft.testlum.testing.framework.interpreter;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.ReceiveSqsMessage;
import com.knubisoft.testlum.testing.model.scenario.SendSqsMessage;
import com.knubisoft.testlum.testing.model.scenario.Sqs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INCORRECT_SQS_PROCESSING;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ALIAS_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SEND_ACTION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.MESSAGE_TO_SEND;

@Slf4j
@InterpreterForClass(Sqs.class)
public class SQSInterpreter extends AbstractInterpreter<Sqs> {

    @Autowired(required = false)
    private Map<AliasEnv, AmazonSQS> amazonSQS;

    public SQSInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Sqs sqs, final CommandResult commandResult) {
        List<CommandResult> subCommandsResultList = new LinkedList<>();
        int actionNumber = 1;
        for (Object action : sqs.getSendOrReceive()) {
            log.info(COMMAND_LOG, dependencies.getPosition().incrementAndGet(), action.getClass().getSimpleName());
            CommandResult result = ResultUtil.createNewCommandResultInstance(actionNumber);
            processEachAction(action, sqs.getAlias(), result);
            subCommandsResultList.add(result);
            actionNumber++;
        }
        commandResult.setSubCommandsResult(subCommandsResultList);
        ResultUtil.setExecutionResultIfSubCommandsFailed(commandResult);
    }

    private void processEachAction(final Object action,
                                   final String alias,
                                   final CommandResult subCommandResult) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            processSqsAction(subCommandResult, action, alias);
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(subCommandResult, e);
        } finally {
            subCommandResult.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void processSqsAction(final CommandResult subCommandResult,
                                    final Object action,
                                    final String alias) {
        log.info(ALIAS_LOG, alias);
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (action instanceof SendSqsMessage) {
            SendSqsMessage send = (SendSqsMessage) action;
            ResultUtil.addSqsInfoForSendAction(send, alias, subCommandResult);
            sendMessage(createSendRequest(send, aliasEnv), aliasEnv, subCommandResult);
        } else if (action instanceof ReceiveSqsMessage) {
            ReceiveSqsMessage receive = (ReceiveSqsMessage) action;
            ResultUtil.addSqsInfoForReceiveAction(receive, alias, subCommandResult);
            receiveAndCompareMessage(createReceiveRequest(receive, aliasEnv), receive, aliasEnv, subCommandResult);
        } else {
            throw new DefaultFrameworkException(INCORRECT_SQS_PROCESSING);
        }
    }

    private void sendMessage(final SendMessageRequest sendRequest,
                             final AliasEnv aliasEnv,
                             final CommandResult result) {
        String queue = sendRequest.getQueueUrl();
        String message = sendRequest.getMessageBody();
        LogUtil.logBrokerActionInfo(SEND_ACTION, queue, message);
        result.put(MESSAGE_TO_SEND, StringPrettifier.asJsonResult(message));
        this.amazonSQS.get(aliasEnv).sendMessage(sendRequest);
    }

    private String receiveAndCompareMessage(final ReceiveMessageRequest receiveRequest,
                                            final ReceiveSqsMessage receiveAction,
                                            final AliasEnv aliasEnv,
                                            final CommandResult result) {
        final String message = receiveMessage(receiveRequest.getQueueUrl(), aliasEnv);
        LogUtil.logBrokerActionInfo(RECEIVE_ACTION, receiveAction.getQueue(), message);
        compareMessage(getMessageToReceive(receiveAction), message, result);
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
                .withExpected(inject(getContentIfFile(fileOrContent)))
                .withActual(message);
        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));
        result.setActual(StringPrettifier.asJsonResult(message));
        comparator.exec();
    }

    private String createQueueIfNotExists(final String queue, final AliasEnv aliasEnv) {
        String queueName = checkIfQueueExists(queue, aliasEnv);
        if (StringUtils.isBlank(queueName)) {
            return this.amazonSQS.get(aliasEnv).createQueue(queue).getQueueUrl();
        }
        return amazonSQS.get(aliasEnv).getQueueUrl(queueName).getQueueUrl();
    }

    private String checkIfQueueExists(final String queue, final AliasEnv aliasEnv) {
        List<String> queueNames = getQueueNameFromQueueUrl(aliasEnv);
        return queueNames.stream()
                .filter(queueName -> queueName.equals(queue))
                .findFirst()
                .orElse(null);
    }

    private List<String> getQueueNameFromQueueUrl(final AliasEnv aliasEnv) {
        List<String> queueNames = new ArrayList<>();
        List<String> queueUrls = amazonSQS.get(aliasEnv).listQueues().getQueueUrls();
        for (String queueUrl : queueUrls) {
            queueNames.add(queueUrl.substring(queueUrl.lastIndexOf("/") + 1));
        }
        return queueNames;
    }

    private SendMessageRequest createSendRequest(final SendSqsMessage sendAction, final AliasEnv aliasEnv) {
        SendMessageRequest sendRequest = new SendMessageRequest();
        String message = getMessageToSend(sendAction);
        sendRequest.setQueueUrl(createQueueIfNotExists(sendAction.getQueue(), aliasEnv));
        sendRequest.setMessageBody(message);
        sendRequest.setDelaySeconds(sendAction.getDelaySeconds());
        sendRequest.setMessageDeduplicationId(sendAction.getMessageDeduplicationId());
        sendRequest.setMessageGroupId(sendAction.getMessageGroupId());
        return sendRequest;
    }

    private ReceiveMessageRequest createReceiveRequest(final ReceiveSqsMessage receiveAction, final AliasEnv aliasEnv) {
        ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest();
        receiveRequest.setQueueUrl(createQueueIfNotExists(receiveAction.getQueue(), aliasEnv));
        receiveRequest.setMaxNumberOfMessages(receiveAction.getMaxNumberOfMessages());
        receiveRequest.setVisibilityTimeout(receiveAction.getVisibilityTimeout());
        receiveRequest.setWaitTimeSeconds(receiveAction.getWaitTimeSeconds());
        receiveRequest.setReceiveRequestAttemptId(receiveAction.getReceiveRequestAttemptId());
        return receiveRequest;
    }

    private String getMessageToSend(final SendSqsMessage send) {
        return getValue(send.getValue(), send.getFile());
    }

    private String getMessageToReceive(final ReceiveSqsMessage receive) {
        return getValue(receive.getValue(), receive.getFile());
    }

    private String getValue(final String message, final String file) {
        return StringUtils.isNotBlank(message)
                ? message
                : FileSearcher.searchFileToString(file, dependencies.getFile());
    }
}
