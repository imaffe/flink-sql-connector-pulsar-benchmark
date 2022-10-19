package com.example.affe;

import com.example.affe.data.ExampleUser;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.example.affe.data.ExampleUser.createRandomUserWithCreateTime;

public class BenchmarkProducer {
    private static final Logger logger
            = LoggerFactory.getLogger(BenchmarkProducer.class);
    private static final String USER_COMPLETE_TOPIC_PATH = "sample/flink-benchmark/string-topic";

    private static final int NUM_MESSAGES = 10000000;

    private static final String ADMIN_URL = "http://20.81.113.183:80";

    private static final String SERVICE_URL = "pulsar://20.81.113.183:6650";

    public static void main(String[] args) throws PulsarClientException, InterruptedException {


        PulsarAdmin admin = PulsarAdmin.builder().serviceHttpUrl(ADMIN_URL).build();

        PulsarClient client = PulsarClient.builder()
                .serviceUrl(SERVICE_URL)
                .build();

        // designate an AVRO schema
        Producer<String> producer = client.newProducer(Schema.STRING)
                .topic(USER_COMPLETE_TOPIC_PATH)
                .enableBatching(true)
                .create();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                producer.close();
                client.close();
            } catch (PulsarClientException e) {
                e.printStackTrace();
            }
        }));

        while(true) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            long epochMillis = currentDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
            // ExampleUser user = createRandomUserWithCreateTime(epochMillis);
            producer.newMessage()
                    .eventTime(epochMillis)
                    .value("Simple String")
                    .sendAsync();
        }
    }
}
