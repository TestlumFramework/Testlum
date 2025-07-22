package com.knubisoft.testlum.testing.framework.db.sqs;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Conditional({OnSQSEnabledCondition.class})
@Component
public class SQSOperation extends AbstractStorageOperation {

    private final Map<AliasEnv, SqsClient> sqsClient;

    public SQSOperation(@Autowired(required = false) final Map<AliasEnv, SqsClient> sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    public AbstractStorageOperation.StorageOperationResult apply(final Source source, final String alias) {
        return null;
    }

    @Override
    public void clearSystem() {
        this.sqsClient.forEach((aliasEnv, amazonSQS) -> {
            if (isTruncate(Sqs.class, aliasEnv)
                && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                ListQueuesResponse listQueuesResponse = amazonSQS.listQueues();
                List<String> queueUrls = listQueuesResponse.queueUrls();
                queueUrls.forEach(queueUrl -> amazonSQS.purgeQueue(
                        PurgeQueueRequest.builder()
                                .queueUrl(queueUrl)
                                .build()
                ));
            }
        });
    }
}