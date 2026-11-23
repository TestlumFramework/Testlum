package com.knubisoft.testlum.testing.framework.inbound.server.server.impl;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.handler.InboundRequestHandler;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.util.InboundDtoMapper;
import com.knubisoft.testlum.testing.framework.inbound.server.util.InboundResponseFactory;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of @{link InboundServer} using {@code com.sun.net.httpserver.HttpServer}.
 */
@Slf4j
public class JdkInboundServer implements InboundServer {

    private final boolean enabled;
    private final String host;
    private final int requestedPort;
    private final int maxThreads;
    private final InboundRequestHandler handler;

    private volatile HttpServer server; // server != null ? server is running : server is not running
    private volatile ThreadPoolExecutor executor;
    private volatile int boundPort = -1;

    // Inject it
    private final InboundDtoMapper inboundDtoMapper = new InboundDtoMapper();

    public JdkInboundServer(boolean enabled, String host, int requestedPort, int maxThreads,
                            InboundRequestHandler handler) {
        this.enabled = enabled;
        this.host = host == null ? "0.0.0.0" : host;
        this.requestedPort = requestedPort;
        this.maxThreads = maxThreads <= 0 ? 10 : maxThreads;
        this.handler = handler;
    }

    @Override
    public synchronized void start() {
        if (!enabled) {
            throw new DefaultFrameworkException("Inbound server is disabled. " +
                    "Please, enable it in the global config file.");
        }
        if (server != null) {
            // Log it
            return;
        }
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(host, requestedPort), 0);
            // HTTP server does not make request-routing. It catches any requests.
            httpServer.createContext("/", this::dispatch);
            httpServer.setExecutor(buildExecutor());
            httpServer.start();

            this.server = httpServer;
            this.boundPort = httpServer.getAddress().getPort();
            String displayHost = "0.0.0.0".equals(host) ? "localhost" : host;
            log.info("============================================================");
            log.info(" Testlum inbound HTTP server STARTED");
            log.info("   listening on http://" + displayHost + ":" + boundPort);
            log.info("   (any path; routing by correlation matchers)");
            log.info("============================================================");
        } catch (IOException e) {
            throw new DefaultFrameworkException("Failed to start inbound server on " + host + ":" + requestedPort, e);
        }
    }

    private void dispatch(HttpExchange httpExchange) {
        InboundResponse response;
        try {
            InboundRequest request = inboundDtoMapper.toRequest(httpExchange);
            response = handler.handle(request);
            // To provide a correct response - the handler's deal. But we have to double-check it.
            if (response == null) {
                response = InboundResponseFactory.internalError("handler returned null");
            }
        } catch (Exception e) {
            log.error("Inbound request handler failed for {} {}",
                    httpExchange.getRequestMethod(), httpExchange.getRequestURI(), e);
            response = InboundResponseFactory.internalError("handler throws error: " + e.getMessage());
        }
        try {
            writeResponse(httpExchange, response);
        } catch (IOException e) {
            log.warn("Failed to write inbound response for {} {}: {}",
                    httpExchange.getRequestMethod(), httpExchange.getRequestURI(), e.getMessage());
        } finally {
            httpExchange.close();
        }
    }

    /**
     * Custom cache-style executor. Core = 0. Threads get created on demand
     * and live @{code keepAlive} time. Synchronous queue so that every request
     * searches for a free/new thread.
     */
    private ThreadPoolExecutor buildExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {

            private final AtomicLong threadNumber = new AtomicLong();

            @Override
            public Thread newThread(Runnable r) {
                Thread newThread = new Thread(r, "testlum-inbound-server-thread-" + threadNumber.incrementAndGet());
                newThread.setDaemon(true); // not to keep JVM alive because of this thread
                return newThread;
            }
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                0, maxThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory);
        this.executor = threadPoolExecutor;
        return threadPoolExecutor;
    }

    private void writeResponse(HttpExchange httpExchange, InboundResponse response) throws IOException {
        Headers responseHeaders = httpExchange.getResponseHeaders();
        responseHeaders.putAll(response.getHeaders());

        byte[] body = response.getBody();

        long contentLength = (body.length == 0) ? -1 : body.length;
        httpExchange.sendResponseHeaders(response.getStatus(), contentLength);

        if (body.length > 0) {
            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(body);
            }
        }
    }


    @Override
    public void stop(Duration grace) {
        if (server == null) {
            return;
        }
        int graceSeconds = (int) Math.max(0, grace == null ? 0 : grace.getSeconds());
        try {
            server.stop(graceSeconds);
        } finally {
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(graceSeconds + 1L, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
        server = null;
        executor = null;
        // Log that server has stopped
    }

    @Override
    public int getPort() {
        return boundPort;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public boolean isRunning() {
        return server != null;
    }
}
