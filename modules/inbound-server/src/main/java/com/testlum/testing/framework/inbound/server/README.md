For inbound testing, we have such layers:

1. HTTP connection layer. It's represented by InboundServer.

This server does not know anything about sessions or registry. Its main responsibility is 
to send an HTTP request to InboundRequestHandler and return a response from it. It maps
the HTTP request to the InboundRequest and writes InboundResponse into the output stream.

2. Request handling layer.

InboundRequestHandler is the only thing the server talks to. Its implementation lives in the
module of the command that receives the requests, e.g. WebhookRequestHandler in the webhook
module: it buffers each request per endpoint, and a <webhook> step claims it from the buffer
with a WebhookMatchStrategy (matching by header, query param, body key or path variable).

3. Public access. NgrokTunnel exposes the local server, InboundServerLifecycle starts and stops both.