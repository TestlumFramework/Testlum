package com.testlum.testing.framework.mcp;

import tools.jackson.databind.JsonNode;

import java.util.function.Function;

/** One MCP tool: a name, a description, a JSON schema for its input and a handler that returns text. */
public record McpTool(String name, String description, JsonNode inputSchema, Function<JsonNode, String> handler) {
}
