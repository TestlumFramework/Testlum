#!/usr/bin/env bash
aws dynamodb delete-table \
    --table-name cloud \
    --region ap-northeast-1 \
    --endpoint-url http://dynamodb-two:8000

#aws dynamodb scan \
#  --region ap-northeast-1 \
#  --endpoint-url http://dynamodb-two:8000 \
#  --attributes-to-get year title \
#  --table-name playground --query "Items[*]"
## use jq to get each item on its own line
#jq --compact-output '.[]'
## replace newlines with null terminated so
## we can tell xargs to ignore special characters
#tr '\n' '\0' \
#  xargs -0 -t -I keyItem
## use the whole item as the key to delete (dynamo keys *are* dynamo items)
#aws dynamodb delete-item --table-name playground --key=keyItem

aws dynamodb \
  --region ap-northeast-1 \
  --endpoint-url http://dynamodb-two:8000 \
    create-table \
  --table-name cloud \
  --attribute-definitions \
    AttributeName=year,AttributeType=N \
    AttributeName=title,AttributeType=S \
  --key-schema \
    AttributeName=year,KeyType=HASH \
    AttributeName=title,KeyType=RANGE \
  --billing-mode PROVISIONED \
  --provisioned-throughput ReadCapacityUnits=10,WriteCapacityUnits=10

aws dynamodb list-tables \
  --region ap-northeast-1 \
  --endpoint-url http://dynamodb-two:8000