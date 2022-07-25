package com.knubisoft.cott.testing.framework.db.dynamodb;

import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.paginators.ScanIterable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DynamoDBOperation implements StorageOperation {

    private static final String FORBIDDEN_DDB_TABLE_NAME = "dynamobee";

    private final Map<String, DynamoDbClient> dynamoDbClient;

    public DynamoDBOperation(@Autowired(required = false) final Map<String, DynamoDbClient> dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String alias) {
        return new StorageOperationResult(execute(source.getQueries(), alias));
    }

    @Override
    public void clearSystem() {
        for (Map.Entry<String, DynamoDbClient> entry : dynamoDbClient.entrySet()) {
            entry.getValue().listTables().tableNames().forEach(t -> truncate(t, entry.getKey()));
        }
    }

    private List<QueryResult<List<Map<String, AttributeValue>>>> execute(final List<String> statements,
                                                                         final String alias) {
        return statements.stream().map(s -> executeSingleQuery(s, alias)).collect(Collectors.toList());
    }

    private QueryResult<List<Map<String, AttributeValue>>> executeSingleQuery(final String query, final String alias) {

        ExecuteStatementResponse singleResponse = dynamoDbClient.get(alias)
                .executeStatement(builder -> builder.statement(query));
        return new QueryResult<>(query, singleResponse.items());
    }

    private void truncate(final String tableName, final String alias) {
        if (!tableName.equals(FORBIDDEN_DDB_TABLE_NAME)) {
            ScanIterable scanIterable = dynamoDbClient.get(alias).scanPaginator(ScanRequest.builder()
                    .tableName(tableName).build());
            DescribeTableResponse describeTableResponse = getDescribeTableResponse(tableName, alias);
            String hashKey = describeTableResponse.table().keySchema().get(0).attributeName();
            String rangeKey = describeTableResponse.table().keySchema().get(1).attributeName();
            truncate(scanIterable, tableName, hashKey, rangeKey, alias);
        }
    }

    private void truncate(final ScanIterable scanIterable,
                          final String tableName,
                          final String hashKey,
                          final String rangeKey,
                          final String alias) {
        for (ScanResponse scanResponse : scanIterable) {
            for (Map<String, AttributeValue> item : scanResponse.items()) {
                deleteItem(tableName, hashKey, rangeKey, item, alias);
            }
        }
    }

    private DescribeTableResponse getDescribeTableResponse(final String tableName, final String alias) {
        DescribeTableRequest tableRequest = DescribeTableRequest.builder().tableName(tableName)
                .build();
        return dynamoDbClient.get(alias).describeTable(tableRequest);
    }

    private void deleteItem(final String tableName,
                            final String hashKey,
                            final String rangeKey,
                            final Map<String, AttributeValue> item,
                            final String alias) {
        Map<String, AttributeValue> deleteKey = new HashMap<>();
        deleteKey.put(hashKey, item.get(hashKey));
        if (rangeKey != null) {
            deleteKey.put(rangeKey, item.get(rangeKey));
        }
        deleteItemByTableNameAndKey(tableName, deleteKey, alias);
    }

    private void deleteItemByTableNameAndKey(final String tableName,
                                             final Map<String, AttributeValue> deleteKey,
                                             final String alias) {
        dynamoDbClient.get(alias).deleteItem(DeleteItemRequest
                .builder()
                .tableName(tableName)
                .key(deleteKey)
                .build());
    }
}
