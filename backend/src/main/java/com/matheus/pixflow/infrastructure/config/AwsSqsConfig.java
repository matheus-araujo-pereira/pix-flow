package com.matheus.pixflow.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "pixflow.messaging.provider", havingValue = "sqs")
public class AwsSqsConfig {

    private static final Logger log = LoggerFactory.getLogger(AwsSqsConfig.class);

    @Value("${pixflow.aws.region:us-east-1}")
    private String awsRegion;

    @Value("${pixflow.aws.endpoint:}")
    private String awsEndpoint;

    @Value("${pixflow.aws.access-key-id:mock-key}")
    private String accessKeyId;

    @Value("${pixflow.aws.secret-access-key:mock-secret}")
    private String secretAccessKey;

    @Bean
    public SqsClient sqsClient() {
        log.info("Configuring AWS SQS Client. Region: {}, Endpoint Override: {}", awsRegion, awsEndpoint);
        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                ));

        if (awsEndpoint != null && !awsEndpoint.isBlank()) {
            builder.endpointOverride(URI.create(awsEndpoint));
        }

        return builder.build();
    }
}
