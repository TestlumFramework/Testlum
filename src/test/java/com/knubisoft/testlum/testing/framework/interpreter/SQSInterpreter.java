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
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INCORRECT_SQS_PROCESSING;
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
    protected void acceptImpl(final Sqs o, final CommandResult result) {
        Sqs sqs = injectCommand(o);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        final AtomicInteger commandId = new AtomicInteger();
        for (Object action : sqs.getSendOrReceive()) {
            LogUtil.logSubCommand(dependencies.getPosition().incrementAndGet(), action);
            CommandResult commandResult = ResultUtil.newCommandResultInstance(dependencies.getPosition().get());
            subCommandsResult.add(commandResult);
            processEachAction(action, sqs.getAlias(), commandResult);
        }
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachAction(final Object action, final String alias, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        LogUtil.logAlias(alias);
        try {
            processSqsAction(action, alias, result);
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(result, e);
            ConfigUtil.checkIfStopScenarioOnFailure(e);
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
        LogUtil.logSQSSendInfo(send, message);
        ResultUtil.addSqsSendInfo(send, aliasEnv.getAlias(), result);
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
        LogUtil.logSQSReceiveInfo(receive, expectedMessages);
        ResultUtil.addSqsReceiveInfo(receive, aliasEnv.getAlias(), result);
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
        return StringUtils.isNotBlank(message)
                ? message
                : getContentIfFile(file);
    }
}
