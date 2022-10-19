FROM streamnative/pulsar-flink:1.15.1.2

ADD ./flink-sql-connector-pulsar-demo/target/flink-sql-connector-pulsar-demo-1.0-SNAPSHOT.jar /opt/flink/examples/
