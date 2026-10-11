#!/bin/bash
echo "Initializing LocalStack AWS SQS Queue for PixFlow..."

awslocal sqs create-queue --queue-name pix-settlement-dlq --region us-east-1 || true
awslocal sqs create-queue --queue-name pix-settlement-queue --region us-east-1 || true

echo "LocalStack SQS Queues created successfully:"
awslocal sqs list-queues --region us-east-1

