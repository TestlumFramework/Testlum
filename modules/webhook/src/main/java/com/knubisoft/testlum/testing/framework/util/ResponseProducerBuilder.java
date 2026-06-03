package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testing.framework.producer.ResponseProducer;
import com.knubisoft.testing.framework.producer.ResponseProducerFactory;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import com.knubisoft.testlum.testing.framework.inbound.server.util.InboundResponseFactory;
import com.knubisoft.testlum.testing.model.scenario.WebhookHeader;
import com.knubisoft.testlum.testing.model.scenario.WebhookMockReturn;
import com.knubisoft.testlum.testing.model.scenario.WebhookResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ResponseProducerBuilder {

    private final ContentResolver contentResolver;
    private final ResponseProducerFactory producerFactory;

    public ResponseProducerBuilder(final ContentResolver contentResolver,
                                   final ResponseProducerFactory producerFactory) {
        this.contentResolver = contentResolver;
        this.producerFactory = producerFactory;
    }

    public ResponseProducer buildResponseProducer(final boolean forward,
                                                  final WebhookResponse response,
                                                  final String destinationUrl) {
        if (forward) {
            if (StringUtils.isBlank(destinationUrl)) {
                throw new DefaultFrameworkException("forward=\"true\" requires destinationUrl");
            }
            if (response != null && response.getMockReturn() != null) {
                InboundResponse modifiedResponse = buildMockResponse(response);
                return producerFactory.modifiedProducer(destinationUrl, modifiedResponse);
            }
            return producerFactory.forwardingProducer(destinationUrl);
        }
        InboundResponse mockResponse = buildMockResponse(response);
        return request -> InboundExchangeOutput.mock(mockResponse);
    }

    private InboundResponse buildMockResponse(final WebhookResponse response) {
        WebhookMockReturn mockResult = response != null ? response.getMockReturn() : null;
        if (mockResult == null) {
            log.warn(LogFormat.withYellow("No mock return defined for response. Returning 204 No Content"));
            return InboundResponseFactory.of(HttpStatus.NO_CONTENT.value(), Map.of(), new byte[0]);
        }
        int status = mockStatusIfAny(mockResult);
        byte[] body = mockBodyIfAny(mockResult);
        Map<String, List<String>> headers = mockHeadersIfAny(mockResult);
        return InboundResponseFactory.of(status, headers, body);
    }

    private int mockStatusIfAny(final WebhookMockReturn mockResult) {
        return mockResult.getStatus() == null ? HttpStatus.NO_CONTENT.value() : mockResult.getStatus();
    }

    private byte[] mockBodyIfAny(final WebhookMockReturn mockResult) {
        byte[] body = new byte[0];
        String bodyContent = contentResolver.getContentIfFile(mockResult.getFile());
        if (StringUtils.isNotBlank(bodyContent)) {
            body = bodyContent.getBytes(StandardCharsets.UTF_8);
        }
        return body;
    }

    private Map<String, List<String>> mockHeadersIfAny(final WebhookMockReturn mockResult) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        if (mockResult.getHeaders() == null) {
            return headers;
        }
        for (WebhookHeader header : mockResult.getHeaders().getHeader()) {
            headers.computeIfAbsent(header.getName(), k -> new ArrayList<>()).add(header.getValue());
        }
        return headers;
    }

}
