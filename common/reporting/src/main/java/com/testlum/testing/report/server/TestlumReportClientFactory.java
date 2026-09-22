package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.client.TestlumReportClient;
import com.testlum.reporting.sdk.exception.BrokerConnectionException;
import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.RabbitConfig;
import com.testlum.testing.model.global_config.Report;
import com.testlum.testing.model.global_config.TestlumReportServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class TestlumReportClientFactory {

    public TestlumReportClient create(final Report report) {
        try {
            return builderFor(report).build();
        } catch (BrokerConnectionException e) {
            throw new DefaultFrameworkException(
                    String.format(ExceptionMessage.REPORT_SERVER_UNREACHABLE, e.getMessage()), e);
        }
    }

    private TestlumReportClient.Builder builderFor(final Report report) {
        TestlumReportServer server = report.getTestlumReportServer();
        TestlumReportClient.Builder builder = TestlumReportClient.builder()
                .projectName(report.getProjectName())
                .apiKey(server.getApiKey())
                .reportingUrl(server.getServerUrl());
        return withBroker(builder, server.getRabbitConfig());
    }

    private TestlumReportClient.Builder withBroker(final TestlumReportClient.Builder builder,
                                                   final RabbitConfig rabbit) {
        builder.rabbitHost(rabbit.getHost())
                .rabbitUsername(rabbit.getUsername())
                .rabbitPassword(rabbit.getPassword());
        if (StringUtils.isNotBlank(rabbit.getPort())) {
            builder.rabbitPort(Integer.parseInt(rabbit.getPort().trim()));
        }
        if (StringUtils.isNotBlank(rabbit.getVirtualHost())) {
            builder.rabbitVirtualHost(rabbit.getVirtualHost());
        }
        return builder;
    }
}
