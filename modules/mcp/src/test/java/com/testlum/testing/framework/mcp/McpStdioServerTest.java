package com.testlum.testing.framework.mcp;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpStdioServerTest {

    private static final int QUIET_MILLIS = 1500;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void answersInitializeListAndCallThenEndsOnEof() throws Exception {
        ToolRegistry tools = TestSupport.registry("");
        String in = """
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26"}}
                {"jsonrpc":"2.0","method":"notifications/initialized"}
                {"jsonrpc":"2.0","id":2,"method":"tools/list"}
                {"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"testlum_lookup",\
                "arguments":{"path":"http/get"}}}
                {"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"nope","arguments":{}}}
                {"jsonrpc":"2.0","id":5,"method":"bogus"}
                """;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        new McpStdioServer(tools.all(), new ByteArrayInputStream(in.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buf, true, StandardCharsets.UTF_8)).serve();
        List<String> lines = buf.toString(StandardCharsets.UTF_8).lines().toList();
        assertEquals(5, lines.size(), "the notification gets no reply");
        assertEquals("testlum-mcp", JSON.readTree(lines.get(0)).at("/result/serverInfo/name").asText());
        assertEquals(7, JSON.readTree(lines.get(1)).at("/result/tools").size());
        JsonNode call = JSON.readTree(lines.get(2)).path("result");
        assertTrue(call.at("/content/0/text").asText().contains("endpoint"));
        assertTrue(JSON.readTree(lines.get(3)).at("/result/isError").asBoolean());
        assertEquals(-32601, JSON.readTree(lines.get(4)).at("/error/code").asInt());
    }

    @Test
    void answersWithASupportedProtocolVersion() throws Exception {
        String in = """
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05"}}
                {"jsonrpc":"2.0","id":2,"method":"initialize","params":{"protocolVersion":"1999-01-01"}}
                {"jsonrpc":"2.0","id":3,"method":"initialize","params":{}}
                """;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        new McpStdioServer(List.of(), new ByteArrayInputStream(in.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buf, true, StandardCharsets.UTF_8)).serve();
        List<String> lines = buf.toString(StandardCharsets.UTF_8).lines().toList();
        assertEquals("2024-11-05", JSON.readTree(lines.get(0)).at("/result/protocolVersion").asText());
        assertEquals("2025-06-18", JSON.readTree(lines.get(1)).at("/result/protocolVersion").asText());
        assertEquals("2025-06-18", JSON.readTree(lines.get(2)).at("/result/protocolVersion").asText());
    }

    @Test
    void staysUpWithoutTrafficAndEndsOnEof() throws Exception {
        PipedOutputStream feed = new PipedOutputStream();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        McpStdioServer server = new McpStdioServer(List.of(), new PipedInputStream(feed),
                new PrintStream(buf, true, StandardCharsets.UTF_8));
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread serving = new Thread(() -> {
            try {
                server.serve();
            } catch (IOException e) {
                failure.set(e);
            }
        });
        serving.start();
        serving.join(QUIET_MILLIS);
        assertTrue(serving.isAlive(), "no traffic must not end the session");
        feed.write("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}\n".getBytes(StandardCharsets.UTF_8));
        feed.flush();
        feed.close();
        serving.join(QUIET_MILLIS * 4);
        assertFalse(serving.isAlive(), "closing stdin ends the session");
        assertNull(failure.get(), "the session ends cleanly");
        assertEquals(1, buf.toString(StandardCharsets.UTF_8).lines().count(), "the late ping was answered");
    }
}
