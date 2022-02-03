package com.knubisoft.e2e.testing.framework.db.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class S3Operation implements StorageOperation {

    private final Map<String, AmazonS3> amazonS3;


    public S3Operation(@Autowired(required = false) final Map<String, AmazonS3> amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String bucketName) {
        return null;
    }

    @Override
    public void clearSystem() {
        for (Map.Entry<String, AmazonS3> entry : this.amazonS3.entrySet()) {
            final String bucketName = entry.getKey();
            final ListObjectsV2Result objectsInBucket = entry.getValue().listObjectsV2(bucketName);
            this.deleteObjectsInBucket(bucketName, objectsInBucket);
        }
    }

    private void deleteObjectsInBucket(final String bucketName,
                                       final ListObjectsV2Result objectsInBucket) {
        for (final S3ObjectSummary objectSummary : objectsInBucket.getObjectSummaries()) {
            this.amazonS3.get(bucketName).deleteObject(bucketName, objectSummary.getKey());
        }
    }
}
