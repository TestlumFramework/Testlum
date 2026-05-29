package com.knubisoft.testlum.testing.framework.session;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.AbstractInboundExchange;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeStatus;

import java.util.List;
import java.util.Optional;

public class WebhookExchange extends AbstractInboundExchange {

    private final InboundRequest unexpectedExtraRetry; // Only for UNEXPECTED_EXTRA_RETRY status
    private final List<InboundRequest> receivedRequest;
    private final List<InboundResponse> sendToSenderResponse;
    private final List<InboundResponse> responseFromDestination;

    public WebhookExchange(InboundExchangeStatus status,
                           List<InboundRequest> receivedRequest,
                           List<InboundResponse> sendToSenderResponse,
                           List<InboundResponse> responseFromDestination,
                           String errorMessage,
                           InboundRequest unexpectedExtraRetry) {
        super(status, errorMessage);
        this.receivedRequest = receivedRequest;
        this.sendToSenderResponse = sendToSenderResponse;
        this.responseFromDestination = responseFromDestination;
        this.unexpectedExtraRetry = unexpectedExtraRetry;
    }

    public InboundRequest getUnexpectedExtraRetry() {
        return unexpectedExtraRetry;
    }

    public List<InboundRequest> getReceivedRequest() {
        return receivedRequest;
    }

    public List<InboundResponse> getSendToSenderResponse() {
        return sendToSenderResponse;
    }

    public List<InboundResponse> getResponseFromDestination() {
        return responseFromDestination;
    }

    public Optional<InboundRequest> getFirstReceivedRequest() {
        return getReceivedRequest() != null ? Optional.of(getReceivedRequest().get(0)) : Optional.empty();
    }

    public Optional<InboundResponse> getFirstResponseFromDestination() {
        return getResponseFromDestination() != null ? Optional.of(getResponseFromDestination().get(0)) : Optional.empty();
    }

    public static WebhookExchange success(List<InboundRequest> receivedRequests,
                                          List<InboundResponse> responsesSendToSender,
                                          List<InboundResponse> responsesFromDestination) {
        return new WebhookExchange(InboundExchangeStatus.RECEIVED, receivedRequests,
                responsesSendToSender, responsesFromDestination, null, null);
    }

    public static WebhookExchange timeout() {
        return new WebhookExchange(InboundExchangeStatus.TIMEOUT, List.of(),
                List.of(), List.of(), null, null);
    }

    public static WebhookExchange belowMin(List<InboundRequest> receivedRequests,
                                           List<InboundResponse> responsesSendToSender,
                                           List<InboundResponse> responsesFromDestination,
                                           int minRequired, int actual) {
        String errorMessage = "Expected at least " + minRequired + " calls, but " + actual + " received";
        return new WebhookExchange(InboundExchangeStatus.BELOW_MIN_RETRIES, receivedRequests,
                responsesSendToSender, responsesFromDestination, errorMessage, null);
    }

    public static WebhookExchange unexpectedExtraRequest(List<InboundRequest> receivedRequests,
                                                  List<InboundResponse> responsesSendToSender,
                                                  List<InboundResponse> responsesFromDestination,
                                                  InboundRequest unexpectedExtraRetry) {
        return new WebhookExchange(InboundExchangeStatus.UNEXPECTED_EXTRA_RETRY, receivedRequests,
                responsesSendToSender, responsesFromDestination,
                "Unexpected extra retry arrived in no-retry windows", unexpectedExtraRetry);
    }

    public static WebhookExchange error(InboundRequest request, String errorMessage) {
        return new WebhookExchange(InboundExchangeStatus.ERROR,
                request == null ? List.of() : List.of(request),
                List.of(), List.of(), errorMessage, null);
    }


}
