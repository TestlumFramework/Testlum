package com.knubisoft.cott.testing.framework.db.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SQSOperation implements StorageOperation {

    private final Map<String, AmazonSQS> amazonSQS;

    public SQSOperation(@Autowired(required = false) final Map<String, AmazonSQS> amazonSQS) {
        this.amazonSQS = amazonSQS;
    }

    @Override
    public StorageOperation.StorageOperationResult apply(final Source source, final String alias) {
        return null;
    }

    @Override
    public void clearSystem() {
        for (Map.Entry<String, AmazonSQS> entry : amazonSQS.entrySet()) {
            ListQueuesResult listQueuesResult = entry.getValue().listQueues();
            List<String> queueUrls = listQueuesResult.getQueueUrls();
            for (String queueUrl : queueUrls) {
                entry.getValue().deleteQueue(queueUrl);
            }
        }
    }
}
