For inbound testing, we have such layers:

1. HTTP connection layer. It's represented by InboundServer.

This server does not know anything about sessions or registry. Its main responsibility is 
to send an HTTP request to InboundRequestHandler and return a response from it. It maps
the HTTP request to the InboundRequest and writes InboundResponse into the output stream.

2. Session registry layer. 

SubscriptionRegistry represents mapping {endpoint -> list of subscribers}. Subscribers 
are test-threads that are waiting to be notified when the request is received.

RegistryRequestHandler serves as an interface between the core functionality and the server.