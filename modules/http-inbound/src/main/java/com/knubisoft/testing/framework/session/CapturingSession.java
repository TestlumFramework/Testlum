package com.knubisoft.testing.framework.session;

import com.knubisoft.testing.framework.producer.ResponseProducer;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.InboundSession;
import com.knubisoft.testlum.testing.framework.inbound.server.util.InboundResponseFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents a capturing session for inbound requests.
 */
public class CapturingSession implements InboundSession {

    private final String endpoint;
    private final Duration timeout;
    private final ResponseProducer responseProducer;

    private final CompletableFuture<SingleInboundExchange> exchangeTask = new CompletableFuture<>();
    private final Object lock = new Object();
    private boolean terminated = false;

    public CapturingSession(String endpoint, Duration timeout, ResponseProducer responseProducer) {
        this.endpoint = endpoint;
        this.timeout = timeout;
        this.responseProducer = responseProducer;
    }

    @Override
    public String endpoint() {
        return endpoint;
    }

    @Override
    public InboundResponse onRequest(InboundRequest request) {
        synchronized (lock) {
            if (terminated) {
               // Log that session has been terminated
                return null;
            }

            try {
                InboundExchangeOutput outcome = responseProducer.produce(request);
                terminated = true;
                exchangeTask.complete(SingleInboundExchange.received(request, outcome));
                return outcome.getResponseToSender();
            } catch (Exception e) {
                terminated = true;
                exchangeTask.complete(SingleInboundExchange.error(request, e.getMessage()));
                return InboundResponseFactory.internalError("webhook processing failed: " + e.getMessage());
            }
        }
    }

    @Override
    public SingleInboundExchange awaitExchange() {
        long millis = Math.max(0, timeout.toMillis());
        try {
            return exchangeTask.get(millis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            synchronized (lock) {
                if (!terminated) {
                    terminated = true;
                    exchangeTask.complete(SingleInboundExchange.timeout());
                }
            }
            return exchangeTask.getNow(SingleInboundExchange.timeout());
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return SingleInboundExchange.error(null, "session terminated with exception: " + e.getMessage());
        }
    }

    @Override
    public boolean isCompleted() {
        return exchangeTask.isDone();
    }
}
