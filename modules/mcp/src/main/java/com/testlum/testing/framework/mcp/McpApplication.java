package com.testlum.testing.framework.mcp;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

/** Entry point: serves the Testlum tools over stdio and exits when the client goes away or sits idle. */
public final class McpApplication {

    private static final int DEFAULT_IDLE_SECONDS = 900;

    private McpApplication() {
    }

    public static void main(final String[] args) throws IOException {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(McpApplication.class.getPackageName())) {
            ToolRegistry tools = ctx.getBean(ToolRegistry.class);
            new McpStdioServer(tools.all(), System.in, System.out, idleSeconds()).serve();
        }
    }

    private static int idleSeconds() {
        String v = System.getenv("TESTLUM_MCP_IDLE_SECONDS");
        return v == null || v.isBlank() ? DEFAULT_IDLE_SECONDS : Integer.parseInt(v.trim());
    }
}
