package com.knubisoft.testlum.testing.framework.configuration.connection.health;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.WebsocketConnectionManager;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.model.global_config.*;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.http.client.Client;
import com.sendgrid.Method;
import com.sendgrid.SendGrid;
import com.twilio.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.conversions.Bson;
import org.elasticsearch.client.*;
import org.openqa.selenium.WebDriver;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HealthCheckFactory {

    private static final int MAX_TIMEOUT_SECONDS = 20;
    private static final int WEB_DRIVER_MAX_TIMEOUT_SECONDS = 60;
    private static final int TIME = 5;

    public static IntegrationHealthCheck<DataSource> forJdbc() {
        return ds -> {
            try (Connection conn = ds.getConnection()) {
                conn.isValid(TIME);
            }
        };
    }

    public static IntegrationHealthCheck<DynamoDbClient> forDynamoDb() {
        return client -> client.listTables(lt -> lt.limit(1));
    }

    public static IntegrationHealthCheck<RestHighLevelClient> forElasticRestHighLevelClient() {
        return restHighLevelClient -> {
            boolean isAlive = restHighLevelClient.ping(RequestOptions.DEFAULT);
            if (!isAlive) {
                throw new DefaultFrameworkException("Connection refused");
            }
        };
    }

    public static IntegrationHealthCheck<RestClient> forElasticRestClient() {
        return restClient -> {
            Response response = restClient.performRequest(new Request("GET", "/"));
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.OK.value()) {
                throw new DefaultFrameworkException("Failed to obtain connection with status code: " + statusCode);
            }
        };
    }

    public static IntegrationHealthCheck<AdminClient> forKafkaAdmin() {
        return client -> client.listTopics().names().get(TIME, TimeUnit.SECONDS);
    }

    public static IntegrationHealthCheck<KafkaConsumer<String, String>> forKafkaConsumer() {
        return kafkaConsumer -> kafkaConsumer.listTopics(Duration.ofSeconds(TIME));
    }

    public static IntegrationHealthCheck<KafkaProducer<String, String>> forKafkaProducer() {
        return kafkaProducer -> kafkaProducer.clientInstanceId(Duration.ofSeconds(TIME));
    }

    public static IntegrationHealthCheck<LambdaClient> forLambda() {
        return client -> client.listFunctions(lf -> lf.maxItems(1));
    }

    public static IntegrationHealthCheck<MongoDatabase> forMongoDb(final Mongo mongo) {
        return mongoDatabase -> {
            Bson ping = new BsonDocument("ping", new BsonInt64(1));
            mongoDatabase.runCommand(ping);
        };
    }

    public static IntegrationHealthCheck<Client> forRabbitMqAdmin() {
        return Client::getVhosts;
    }

    public static IntegrationHealthCheck<CachingConnectionFactory> forRabbitMqAmqp() {
        return connectionFactory -> connectionFactory.createConnection().close();
    }

    public static IntegrationHealthCheck<DefaultStringRedisConnection> forRedis(final JedisConnection jedisConnection) {
        return connection -> {
            String response = jedisConnection.ping();
            if (!"PONG".equalsIgnoreCase(response)) {
                throw new DefaultFrameworkException("Redis did not respond");
            }
        };
    }

    public static IntegrationHealthCheck<SendGrid> forSendGrid() {
        return sdkSendGrid -> {
            com.sendgrid.Request request = new com.sendgrid.Request();
            request.setMethod(Method.GET);
            request.setEndpoint("scopes");

            com.sendgrid.Response response = sdkSendGrid.api(request);
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED.value()
                    || response.getStatusCode() == HttpStatus.FORBIDDEN.value()) {
                throw new DefaultFrameworkException("SendGrid Authentication failed. " + response.getBody());
            } else if (response.getStatusCode() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                throw new DefaultFrameworkException("SendGrid Server Error: " + response.getBody());
            }
        };
    }

    public static IntegrationHealthCheck<SesClient> forSes() {
        return sesClient -> sesClient.listIdentities(li -> li.maxItems(1));
    }

    public static IntegrationHealthCheck<JavaMailSenderImpl> forSmtp() {
        return JavaMailSenderImpl::testConnection;
    }

    public static IntegrationHealthCheck<SqsClient> forSqs() {
        return sqsClient -> sqsClient.listQueues(lq -> lq.maxResults(1));
    }

    public static IntegrationHealthCheck<Twilio> forTwilio() {
        return twilio -> {
            com.twilio.Twilio.init(twilio.getAccountSid(), twilio.getAuthToken());
            try {
                com.twilio.rest.api.v2010.Account.fetcher(twilio.getAccountSid()).fetch();
            } catch (com.twilio.exception.AuthenticationException authEx) {
                throw new DefaultFrameworkException("Twilio Auth Failed - " + authEx.getMessage());
            } catch (ApiException e) {
                throw new DefaultFrameworkException(
                        "Twilio API unreachable - " + e.getMessage() + " " + e.getMoreInfo());
            } catch (Exception e) {
                throw new DefaultFrameworkException("Twilio API unreachable - " + e.getMessage());
            }
        };
    }

    public static IntegrationHealthCheck<WebsocketConnectionManager> forWebSocket(final WebsocketApi websocket) {
        return wsManager -> {
            try {
                wsManager.openConnection();
                if (!wsManager.isConnected()) {
                    throw new DefaultFrameworkException("failed to connect");
                }
            } catch (Exception e) {
                closeConnectionManager(wsManager);
                throw new DefaultFrameworkException(
                        "handshake failed for - " + websocket.getUrl() + " " + e.getMessage());
            }
        };
    }

    private static void closeConnectionManager(final WebsocketConnectionManager wsManager) {
        try {
            wsManager.closeConnection();
        } catch (Exception ex) {
            throw new DefaultFrameworkException(ex.getMessage());
        }
    }

    public static IntegrationHealthCheck<WebDriver> forWebDriver(final AbstractBrowser browser) {
        return webDriver -> {
            try {
                safeInitWebdriverWithTimeouts(browser, webDriver);
            } catch (Exception e) {
                log.error("Failed to initialize driver or reach base URL within {}s", WEB_DRIVER_MAX_TIMEOUT_SECONDS);
                if (webDriver != null) {
                    webDriver.quit();
                }
                throw new DefaultFrameworkException(e.getMessage());
            }
        };
    }

    private static void safeInitWebdriverWithTimeouts(final AbstractBrowser browser, final WebDriver webDriver) {
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(WEB_DRIVER_MAX_TIMEOUT_SECONDS));
        webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(WEB_DRIVER_MAX_TIMEOUT_SECONDS));
        BrowserUtil.manageWindowSize(browser, webDriver);
        Web settings = GlobalTestConfigurationProvider.get().
                getWebSettings(EnvManager.currentEnv());
        log.debug("Navigating to base URL: {}", settings.getBaseUrl());
        webDriver.get(settings.getBaseUrl());
    }

    public static IntegrationHealthCheck<S3Client> forS3() {
        return S3Client::listBuckets;
    }
}
