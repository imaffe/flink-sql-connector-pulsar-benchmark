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

import org.apache.flink.connector.pulsar.table.PulsarTableOptions;
import org.apache.flink.formats.json.JsonFormatFactory;
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

    private static final String CONFIG_FILE_PATH = "/opt/flink/examples/benchmark.properties";

    private static Properties properties;

	public static void main(String[] args) throws Exception {

        EnvironmentSettings settings = EnvironmentSettings
            .newInstance()
            .inStreamingMode()
            .build();
        TableEnvironment tEnv = TableEnvironment.create(settings);

        File configFile = new File(CONFIG_FILE_PATH);
        try {
            FileReader reader = new FileReader(configFile);
            properties = new Properties();
            properties.load(reader);
            reader.close();
        } catch (FileNotFoundException ex) {
            // file does not exist
        } catch (IOException ex) {
            // I/O error
        }
        runJobBasedOnConfig(properties);
	}


    private static void runJobBasedOnConfig(Properties properties) throws OperationNotSupportedException {
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
    private static void runDataStreamJob(Properties properties) {
       if (Objects.equals(properties.getProperty(CONFIG_SCHEMA), SCHEMA_STRING)) {
           runSimpleStringDataStreamJob(properties);
       } else {
           runStructDataStreamJob(properties);
       }
    }

    private static void runSimpleStringDataStreamJob(Properties properties) {

    }

    private static void runStructDataStreamJob(Properties properties) {

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

        String format;
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


