FROM amazon/aws-cli:2.0.56

ENV AWS_ACCESS_KEY_ID=fake_access_key\
    AWS_SECRET_ACCESS_KEY=fake_secret_access_key\
    DYNAMODB_REGION=ap-northeast-1
#Create the process you want to execute from the AWS CLI with shellscript under bin
WORKDIR /opt/pme/
COPY .. .