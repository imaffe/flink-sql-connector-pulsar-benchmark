package com.example.affe;

import com.example.affe.data.HuataiUser;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.impl.schema.JSONSchema;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class HuataiJsonUserProducer {
    public static void main(String[] args) throws Exception {
        PulsarClient client = PulsarClient.builder()
                .serviceUrl("pulsar://127.0.0.1:6650")
                .build();
        String topic = "sample/flink/user";
//        Consumer<HuataiUser> consumer = client.newConsumer(JSONSchema.of(HuataiUser.class)).topic(topic)
//                .subscriptionName("sub-schema").subscribe();
        Producer<HuataiUser> producer = client.newProducer(JSONSchema.of(HuataiUser.class))
                .topic(topic)
                .create();
        DateTimeFormatter formatter
                = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss:SSS");
        String text = "2022-04-02 12:00:00:111";
        LocalDateTime dateTime = LocalDateTime.parse(text, formatter);
        for(int i = 0 ; i< 10 ; i++) {
            HuataiUser user = new HuataiUser("Test" + i, i, 1, System.currentTimeMillis());
            System.out.println(user.getCreateTime());
            MessageId msgId = producer.newMessage()
                    .eventTime(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli())
                    .value(user)
                    .send();
            System.out.println(msgId);
        }

        client.close();
    }
}
