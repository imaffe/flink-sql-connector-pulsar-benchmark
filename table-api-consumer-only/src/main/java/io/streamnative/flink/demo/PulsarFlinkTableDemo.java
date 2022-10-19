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
import java.util.Collections;


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

	public static void main(String[] args) throws Exception {

        EnvironmentSettings settings = EnvironmentSettings
            .newInstance()
            .inStreamingMode()
            .build();
        TableEnvironment tEnv = TableEnvironment.create(settings);

        tEnv.createTable("MyUsers", TableDescriptor
            .forConnector("pulsar")
            .schema(Schema.newBuilder()
                .column("name", DataTypes.STRING())
                .column("age", DataTypes.INT())
                .column("single", DataTypes.BOOLEAN())
                .column("income", DataTypes.DOUBLE())
                .column("createTime", DataTypes.BIGINT())
                .columnByExpression("row_time", "cast(TO_TIMESTAMP(FROM_UNIXTIME(createTime / 1000), 'yyyy-MM-dd HH:mm:ss') as timestamp(3))")
                .watermark("row_time", "row_time - INTERVAL '5' SECOND")
                .build())
            .format(JsonFormatFactory.IDENTIFIER)
                .option(PulsarTableOptions.SOURCE_START_FROM_MESSAGE_ID, "earliest")
                .option(PulsarTableOptions.ADMIN_URL, "http://20.81.113.183:80")
                .option(PulsarTableOptions.SERVICE_URL, "pulsar://20.81.113.183:6650")
                .option(PulsarTableOptions.TOPICS, Collections.singletonList("persistent://sample/flink-benchmark/json-topic"))
            .build());

        tEnv.createTable("MyStrings", TableDescriptor
                .forConnector("pulsar")
                .schema(Schema.newBuilder()
                        .column("value", DataTypes.STRING())
                        .build())
                .format("raw")
                .option(PulsarTableOptions.SOURCE_START_FROM_MESSAGE_ID, "earliest")
                .option(PulsarTableOptions.ADMIN_URL, "http://20.81.113.183:80")
                .option(PulsarTableOptions.SERVICE_URL, "pulsar://20.81.113.183:6650")
                .option(PulsarTableOptions.TOPICS, Collections.singletonList("persistent://sample/flink-benchmark/string-topic"))
                .build());


        tEnv.executeSql("SELECT * FROM MyStrings");
	}
}
