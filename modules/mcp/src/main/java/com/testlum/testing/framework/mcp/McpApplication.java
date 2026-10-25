package com.testlum.testing.framework.mcp;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

/** Entry point: serves the Testlum tools over stdio and exits when the client closes stdin. */
public final class McpApplication {

    private McpApplication() {
    }

    public static void main(final String[] args) throws IOException {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(McpApplication.class.getPackageName())) {
            ToolRegistry tools = ctx.getBean(ToolRegistry.class);
            new McpStdioServer(tools.all(), System.in, System.out).serve();
        }
    }
}
