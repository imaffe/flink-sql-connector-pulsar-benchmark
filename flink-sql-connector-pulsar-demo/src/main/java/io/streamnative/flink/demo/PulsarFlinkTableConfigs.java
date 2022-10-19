package io.streamnative.flink.demo;

public class PulsarFlinkTableConfigs {
    // datastream or table values
    public static final String CONFIG_TYPE = "test.type";

    public static final String TYPE_DATASTREAM = "datastream";

    public static final String TYPE_TABLE = "table";

    // schema

    public static final String CONFIG_SCHEMA = "test.schema";

    public static final String SCHEMA_STRING = "string";

    public static final String SCHEMA_JSON = "json";

    public static final String SCHEMA_AVRO = "avro";

    // semantic

    public static final String CONFIG_SEMANTIC = "test.semantic";

    public static final String SEMANTIC_EXACTLY_ONCE = "exactly-once";

    public static final String SEMANTIC_AT_LEAST_ONCE = "at-least-once";

    // parallelism

    public static final String CONFIG_PARALLELISM = "test.parallelism";

    // partitions

    public static final String CONFIG_PARTITIONS = "test.partitions";

    // Subscription Mode

    public static final String CONFIG_SUBSCRIPTION_MODE = "test.subscription-mode";
}
