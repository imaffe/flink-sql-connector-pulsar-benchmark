package com.example.affe;

import com.example.affe.protos.User;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;

public class ProtobufNativeProducer {
    public static void main(String[] args) {
        try {
            produceToTopic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void produceToTopic() throws Exception {
        PulsarClient client = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();
        Producer<User> producer = client
                .newProducer(Schema.PROTOBUF_NATIVE(User.class))
                .topic("persistent://public/default/affe-proto")
                .create();
        producer.newMessage().value(User.newBuilder().setName("hello affe").setAge(13).build()).send();
    }
}
