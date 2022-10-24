FROM streamnative/pulsar-flink:1.15.1.2

#ENV FLINK_PLUGINS_DIR=/opt/flink/plugins
#COPY ./flink-sql-connector-pulsar-demo/target/flink-sql-connector-pulsar-demo-1.0-SNAPSHOT.jar $FLINK_PLUGINS_DIR/benchmark/
COPY ./flink-sql-connector-pulsar-demo/target/flink-sql-connector-pulsar-demo-1.0-SNAPSHOT.jar /opt/flink/examples/