package com.example.affe;

import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import static java.lang.Thread.sleep;

public class SlowProducer {
    public static void main(String[] args) throws InterruptedException, PulsarClientException {
        String brokerUrl = "pulsar://localhost:6650";
        String topic = "persistent://public/default/affe-slow";
        PulsarClient client = PulsarClient.builder().serviceUrl(brokerUrl).build();
        Producer<byte[]> producer = client.newProducer().topic(topic).messageRoutingMode(MessageRoutingMode.RoundRobinPartition).create();

        for (int i = 0; i < 3000; i++) {
            String message = "Message with i = " + i;
            producer.send(message.getBytes());
            sleep(1000);
        }
    }
}
