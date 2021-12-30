#!/bin/bash
awslocal sqs create-queue --queue-name queue_one
awslocal sqs create-queue --queue-name queue_two
awslocal sqs list-queues