/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.streamnative.flink.demo;

import io.streamnative.flink.demo.data.ExampleUser;
import io.swagger.annotations.Example;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.blackhole.table.BlackHoleTableSinkFactory;
import org.apache.flink.connector.pulsar.source.PulsarSource;
import org.apache.flink.connector.pulsar.source.enumerator.cursor.StartCursor;
import org.apache.flink.connector.pulsar.source.enumerator.cursor.StopCursor;
import org.apache.flink.connector.pulsar.table.PulsarTableOptions;
import org.apache.flink.formats.json.JsonFormatFactory;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.table.api.*;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

import static io.streamnative.flink.demo.PulsarFlinkTableConfigs.*;
import static java.time.Duration.ofMinutes;
import static org.apache.flink.api.common.eventtime.WatermarkStrategy.forBoundedOutOfOrderness;
import static org.apache.flink.configuration.Configuration.fromMap;
import static org.apache.flink.connector.pulsar.source.reader.deserializer.PulsarDeserializationSchema.flinkSchema;
import static org.apache.flink.connector.pulsar.source.reader.deserializer.PulsarDeserializationSchema.pulsarSchema;
import static org.apache.pulsar.client.api.SubscriptionType.Exclusive;
import static org.apache.pulsar.client.api.SubscriptionType.Failover;


/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class PulsarFlinkTableDemo {
//    private static final String CONFIG_FILE_PATH = "/opt/flink/plugins/benchmark/config/benchmark.properties";
    private static final String CONFIG_FILE_PATH = "/opt/flink/examples/config/benchmark.properties";
    private static Properties properties;

	public static void main(String[] args) throws Exception {

        File configFile = new File(CONFIG_FILE_PATH);
        try {
            FileReader reader = new FileReader(configFile);
            properties = new Properties();
            properties.load(reader);
            reader.close();
        } catch (FileNotFoundException ex) {
            // file does not exist
            properties = new Properties();
            properties.setProperty(CONFIG_TYPE, TYPE_TABLE);
            properties.setProperty(CONFIG_SCHEMA, SCHEMA_STRING);
        } catch (IOException ex) {
            // I/O error
            properties = new Properties();
            properties.setProperty(CONFIG_TYPE, TYPE_TABLE);
            properties.setProperty(CONFIG_SCHEMA, SCHEMA_STRING);
        }
        runJobBasedOnConfig(properties);
//        runStructTableJob(new Properties());
	}


    private static void runJobBasedOnConfig(Properties properties) throws Exception {
        switch (properties.getProperty(CONFIG_TYPE)) {
            case TYPE_DATASTREAM:
                runDataStreamJob(properties);
                break;
            case TYPE_TABLE:
                runTableJob(properties);
                break;
            default:
                throw new OperationNotSupportedException("Do not support this test.type");
        }
    }

    // DataStream tests
    private static void runDataStreamJob(Properties properties) throws Exception {
       if (Objects.equals(properties.getProperty(CONFIG_SCHEMA), SCHEMA_STRING)) {
           runSimpleStringDataStreamJob(properties);
       } else {
           runStructDataStreamJob(properties);
       }
    }

    private static void runSimpleStringDataStreamJob(Properties properties) throws Exception {
        // Create execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        PulsarSource<String> pulsarSource = PulsarSource.builder()
                .setServiceUrl("pulsar://20.81.113.183:6650")
                .setAdminUrl("http://20.81.113.183:80")
                .setStartCursor(StartCursor.earliest())
                .setUnboundedStopCursor(StopCursor.never())
                .setTopics("persistent://sample/flink-benchmark/string-topic")
                .setDeserializationSchema(flinkSchema(new SimpleStringSchema()))
                .setSubscriptionName("flink-source")
                .setConsumerName("flink-source-%s")
                .setSubscriptionType(Exclusive)
                .build();

        // Pulsar Source don't require extra TypeInformation be provided.
        DataStreamSource<String> source = env.fromSource(pulsarSource, forBoundedOutOfOrderness(ofMinutes(5)), "pulsar-source");
        source.addSink(new SinkFunction<String>() {
            @Override
            public void invoke(String value, Context context) throws Exception {

            }

        });
        env.execute("Simple Pulsar Source");
    }

    private static void runStructDataStreamJob(Properties properties) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        PulsarSource<ExampleUser> pulsarSource = PulsarSource.builder()
                .setServiceUrl("pulsar://20.81.113.183:6650")
                .setAdminUrl("http://20.81.113.183:80")
                .setStartCursor(StartCursor.earliest())
                .setUnboundedStopCursor(StopCursor.never())
                .setTopics("persistent://sample/flink-benchmark/json-topic")
                .setDeserializationSchema(pulsarSchema(org.apache.pulsar.client.api.Schema.JSON(ExampleUser.class), ExampleUser.class))
                .setSubscriptionName("flink-source")
                .setConsumerName("flink-source-%s")
                .setSubscriptionType(Exclusive)
                .build();

        // Pulsar Source don't require extra TypeInformation be provided.
        DataStreamSource<ExampleUser> source = env.fromSource(pulsarSource, forBoundedOutOfOrderness(ofMinutes(5)), "pulsar-source");
        source.addSink(new SinkFunction<ExampleUser>() {
            @Override
            public void invoke(ExampleUser value, Context context) throws Exception {

            }

        });
        env.execute("Simple Pulsar Source");
    }


    // Table tests
    private static void runTableJob(Properties properties) throws OperationNotSupportedException {
        if (Objects.equals(properties.getProperty(CONFIG_SCHEMA), SCHEMA_STRING)) {
            runSimpleStringTableJob(properties);
        } else {
            runStructTableJob(properties);
        }
    }

    private static void runSimpleStringTableJob(Properties properties) {
        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        TableEnvironment tEnv = TableEnvironment.create(settings);

        tEnv.createTable("MyStrings", TableDescriptor.forConnector("pulsar")
                .schema(Schema.newBuilder()
                        .column("value", DataTypes.STRING())
                        .build())
                .format("raw")
                .option(PulsarTableOptions.SOURCE_START_FROM_MESSAGE_ID, "earliest")
                .option(PulsarTableOptions.ADMIN_URL, "http://20.81.113.183:80")
                .option(PulsarTableOptions.SERVICE_URL, "pulsar://20.81.113.183:6650")
                .option(PulsarTableOptions.TOPICS, Collections.singletonList("persistent://sample/flink-benchmark/string-topic"))
                .build());

        tEnv.createTable("BlackHole", TableDescriptor.forConnector("blackhole").schema(
                Schema.newBuilder()
                        .column("value", DataTypes.STRING())
                        .build())
                .build());
        tEnv.executeSql("INSERT INTO BlackHole SELECT * FROM MyStrings");
    }

    private static void runStructTableJob(Properties properties) throws OperationNotSupportedException {
        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        TableEnvironment tEnv = TableEnvironment.create(settings);

        String format = "json";
        if (Objects.equals(properties.getProperty(CONFIG_SCHEMA), SCHEMA_JSON)) {
            format = "json";
        } else if (Objects.equals(properties.getProperty(CONFIG_SCHEMA), SCHEMA_AVRO)) {
            format = "avro";
        } else {
            throw new OperationNotSupportedException("Only support json and avro struct schema");
        }
        tEnv.createTable("MyUsers", TableDescriptor.
                forConnector("pulsar").
                schema(Schema.newBuilder()
                        .column("name", DataTypes.STRING())
                        .column("age", DataTypes.INT())
                        .column("single", DataTypes.BOOLEAN())
                        .column("income", DataTypes.DOUBLE())
                        .column("createTime", DataTypes.BIGINT())
                        .columnByExpression("row_time", "cast(TO_TIMESTAMP(FROM_UNIXTIME(createTime / 1000), 'yyyy-MM-dd HH:mm:ss') as timestamp(3))")
                        .watermark("row_time", "row_time - INTERVAL '5' SECOND")
                        .build())
                .format(format)
                .option(PulsarTableOptions.SOURCE_START_FROM_MESSAGE_ID, "earliest")
                .option(PulsarTableOptions.ADMIN_URL, "http://20.81.113.183:80")
                .option(PulsarTableOptions.SERVICE_URL, "pulsar://20.81.113.183:6650")
                .option(PulsarTableOptions.TOPICS, Collections.singletonList("persistent://sample/flink-benchmark/json-topic"))
                .build());

        tEnv.createTable("BlackHole", TableDescriptor.forConnector("blackhole").schema(
                        Schema.newBuilder()
                                .column("name", DataTypes.STRING())
                                .column("age", DataTypes.INT())
                                .column("single", DataTypes.BOOLEAN())
                                .column("income", DataTypes.DOUBLE())
                                .column("createTime", DataTypes.BIGINT())
                                .column("row_time", DataTypes.TIMESTAMP(3))
                                .build())
                .build());

        tEnv.executeSql("INSERT INTO BlackHole SELECT * FROM MyUsers");
    }
}


