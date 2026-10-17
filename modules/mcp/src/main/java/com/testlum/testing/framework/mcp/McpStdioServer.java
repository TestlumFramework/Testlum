package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.exception.ToolException;
import com.testlum.testing.framework.mcp.constant.LogMessage;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Minimal MCP server over stdio (newline-delimited JSON-RPC). Handles initialize, ping, tools/list and
 * tools/call and nothing else. Ends when stdin closes or, with no
 * traffic and no call in flight, after the idle limit, so a forgotten session never lingers.
 */
@Slf4j
@RequiredArgsConstructor
public final class McpStdioServer {

    private static final List<String> PROTOCOLS = List.of("2024-11-05", "2025-03-26", "2025-06-18");
    private static final String VERSION = "0.5.0";
    private static final String TYPE = "type";
    private static final String TEXT = "text";
    private static final String CONTENT = "content";
    private static final String PARAMS = "params";
    private static final String JSON_RPC = "2.0";
    private static final int PARSE_ERROR = -32700;
    private static final int METHOD_NOT_FOUND = -32601;
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final long MIN_POLL_MILLIS = 50;
    private static final long MAX_POLL_MILLIS = 1000;
    private static final long POLL_PER_IDLE_SECOND = 250;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final List<McpTool> tools;
    private final InputStream in;
    private final PrintStream out;
    private final int idleSeconds;
    private final AtomicLong lastActivity = new AtomicLong(System.nanoTime());
    private final AtomicBoolean busy = new AtomicBoolean();
    private final AtomicBoolean idleExit = new AtomicBoolean();
    private volatile Thread reader;

    /** True when the last {@link #serve()} ended because of the idle timeout. */
    public boolean endedIdle() {
        return idleExit.get();
    }

    public void serve() throws IOException {
        reader = Thread.currentThread();
        Thread watchdog = startWatchdog();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            readLines(r);
        } catch (IOException e) {
            if (!idleExit.get()) {
                throw e;
            }
        } finally {
            watchdog.interrupt();
        }
    }

    private Thread startWatchdog() {
        Thread watchdog = new Thread(this::watch, "idle-watchdog");
        watchdog.setDaemon(true);
        if (idleSeconds > 0) {
            watchdog.start();
        }
        return watchdog;
    }

    private void readLines(final BufferedReader r) throws IOException {
        String line;
        while (!idleExit.get() && (line = r.readLine()) != null) {
            if (!line.isBlank()) {
                handleBusy(line);
            }
        }
    }

    private void handleBusy(final String line) {
        lastActivity.set(System.nanoTime());
        busy.set(true);
        try {
            handle(line);
        } finally {
            busy.set(false);
            lastActivity.set(System.nanoTime());
        }
    }

    private void watch() {
        long limit = idleSeconds * NANOS_PER_SECOND;
        long poll = Math.max(MIN_POLL_MILLIS, Math.min(MAX_POLL_MILLIS, idleSeconds * POLL_PER_IDLE_SECOND));
        try {
            while (!isIdleFor(limit)) {
                Thread.sleep(poll);
            }
            idleExit.set(true);
            log.info(LogMessage.IDLE_EXIT, idleSeconds);
            closeQuietly();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isIdleFor(final long limit) {
        return !busy.get() && System.nanoTime() - lastActivity.get() > limit;
    }

    private void closeQuietly() {
        try {
            if (in == System.in) {
                System.exit(0); // the reader is blocked on System.in, which cannot be interrupted
            }
            in.close();
        } catch (IOException ignored) {
            // closing only unblocks the reader
        }
        Thread t = reader;
        if (t != null) {
            t.interrupt();
        }
    }

    private void handle(final String line) {
        try {
            dispatch(JSON.readTree(line));
        } catch (JacksonException e) {
            send(error(null, PARSE_ERROR, "Parse error"));
        }
    }

    private void dispatch(final JsonNode msg) {
        JsonNode id = msg.get("id");
        if (id == null || id.isNull()) {
            return; // notifications need no reply
        }
        String method = msg.path("method").asText("");
        JsonNode params = msg.path(PARAMS);
        switch (method) {
            case "initialize" -> send(result(id, initializeResult(params)));
            case "ping" -> send(result(id, JSON.createObjectNode()));
            case "tools/list" -> send(result(id, listResult()));
            case "tools/call" -> send(result(id, call(params)));
            default -> send(error(id, METHOD_NOT_FOUND, "Method not found: " + method));
        }
    }

    private ObjectNode initializeResult(final JsonNode params) {
        ObjectNode r = JSON.createObjectNode();
        String asked = params.path("protocolVersion").asText("");
        r.put("protocolVersion", PROTOCOLS.contains(asked) ? asked : PROTOCOLS.get(PROTOCOLS.size() - 1));
        r.putObject("capabilities").putObject("tools");
        ObjectNode info = r.putObject("serverInfo");
        info.put("name", "testlum-mcp");
        info.put("version", VERSION);
        return r;
    }

    private ObjectNode listResult() {
        ObjectNode r = JSON.createObjectNode();
        ArrayNode arr = r.putArray("tools");
        for (McpTool t : tools) {
            ObjectNode n = arr.addObject();
            n.put("name", t.name());
            n.put("description", t.description());
            n.set("inputSchema", t.inputSchema());
        }
        return r;
    }

    private record Outcome(String text, boolean error) { }

    private ObjectNode call(final JsonNode params) {
        String name = params.path("name").asText();
        JsonNode args = params.has("arguments") ? params.get("arguments") : JSON.createObjectNode();
        Outcome o = tools.stream().filter(t -> t.name().equals(name)).findFirst().map(t -> invoke(t, args))
                .orElse(new Outcome("Unknown tool: " + name, true));
        ObjectNode r = JSON.createObjectNode();
        r.putArray(CONTENT).addObject().put(TYPE, TEXT).put(TEXT, o.text());
        r.put("isError", o.error());
        return r;
    }

    private static Outcome invoke(final McpTool tool, final JsonNode args) {
        try {
            return new Outcome(tool.handler().apply(args), false);
        } catch (ToolException e) {
            return new Outcome(e.getMessage(), true);
        } catch (RuntimeException e) {
            return new Outcome("Internal error: " + e, true);
        }
    }

    private static ObjectNode result(final JsonNode id, final JsonNode result) {
        ObjectNode n = JSON.createObjectNode();
        n.put("jsonrpc", JSON_RPC);
        n.set("id", id);
        n.set("result", result);
        return n;
    }

    private static ObjectNode error(final JsonNode id, final int code, final String message) {
        ObjectNode n = JSON.createObjectNode();
        n.put("jsonrpc", JSON_RPC);
        n.set("id", id == null ? JSON.nullNode() : id);
        ObjectNode e = n.putObject("error");
        e.put("code", code);
        e.put("message", message);
        return n;
    }

    private synchronized void send(final JsonNode node) {
        out.print(node.toString());
        out.print('\n');
        out.flush();
    }
}
