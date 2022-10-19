#!/usr/bin/env python3
import multiprocessing
import subprocess
from concurrent.futures import ThreadPoolExecutor
import time
import json
import atexit

import pulsar

import argparse

# prerequisites
# 1. a running flink operator
# 2. a running pulsar cluster

# argument parsing
parser = argparse.ArgumentParser()
parser.add_argument("-a", "--admin-url", type=str, help="The admin url of Pulsar cluster")
parser.add_argument("-s", "--service-url", type=str, help="The service url of Pulsar cluster")
parser.add_argument("-c", "--cluster", type=str, help="The cluster name of the Pulsar cluster")
parser.add_argument("-w", "--workers", type=int, help="How many threads we want to use for the producer program")
parser.add_argument("-p", "--partitions", type=int, help="The partitions of the test topic")
args = parser.parse_args()

admin_url = args.admin_url
service_url = args.service_url
cluster = args.cluster
workers = args.workers
partitions = args.partitions

end_condition = multiprocessing.Event()

# before exit, delete the k8s resource and cp file from the pod
def exit_handler():
    end_condition.set()
    # stop the pipeline after 5 minutes.
    cmd_get_task_manager_log = "kubectl cp flink-benchmark-taskmanager-1-1:/opt/flink/log/ ./logs/"
    subprocess.run(cmd_get_task_manager_log, shell=True, check=True)

    cmd_stop_flink_job = "kubectl delete -f  ../k8s-configs/benchmark-table-job.yaml"
    subprocess.run(cmd_stop_flink_job, shell=True, check=True)

atexit.register(exit_handler)

# create pulsar resources
setup_commands = [
    "pulsarctl context set benchmark --admin-service-url=\"{}\"".format(admin_url),
    "pulsarctl context use benchmark",
    "pulsarctl tenants create sample -c {}".format(cluster),
    "pulsarctl namespace create sample/flink-benchmark",
    "pulsarctl topics create sample/flink-benchmark/string-topic {}".format(partitions),
    "pulsarctl topics create sample/flink-benchmark/json-topic {}".format(partitions),
]


for cmd in setup_commands:
    subprocess.run(cmd, shell=True)

image_build_commands = [
    "cd ../table-api-consumer-only && mvn clean package",
    "cd .. && docker build . -t affe/pulsar-flink-benchmark-demo:latest",
    "cd .. && docker push affe/pulsar-flink-benchmark-demo:latest",
]

for cmd in image_build_commands:
    subprocess.run(cmd, shell=True, check=True)


#start flink job
cmd_start_flink_job = "kubectl apply -f ../k8s-configs/benchmark-table-job.yaml"
subprocess.run(cmd_start_flink_job, shell=True, check=True)

# wait for the pod up and running

# feed data into the topic

def send_str_messages(pulsar_client: pulsar.Client):
    producers = []
    for i in range(partitions):
        producers[i] =  pulsar_client.create_producer(topic="sample/flink-benchmark/string-topic-partition-{}".format(i),
                                             schema=pulsar.schema.StringSchema)
    while not end_condition.is_set():
        for i in range(partitions):
            producers[i].send_async("Simple String")

with ThreadPoolExecutor(max_workers=workers) as executor:
    client = pulsar.Client(service_url)
    for i in range(workers):
        executor.submit(send_str_messages, client)


# observing the output: use the data from pulsar, we need to refresh the interval
for i in range(30):
    msg_rates_in, msg_rates_out, bytes_rates_in, bytes_rates_out = 0.0, 0.0, 0.0, 0.0
    for j in range(partitions):
        cmd_observe_metrics = "pulsarctl topics stats {}-partition-{}".format("sample/flink-benchmark/string-topic", j)
        result = subprocess.run(cmd_observe_metrics, shell=True, capture_output=True, text=True)
        # print("The command  is : {}".format(cmd_observe_metrics))
        if result.stdout.startswith("["):
            print("Got error")
            continue
        stats_json = json.loads(result.stdout)

        msg_rates_in += float(stats_json["msgRateIn"])
        msg_rates_out += float(stats_json["msgRateOut"])
        bytes_rates_in += float(stats_json["msgThroughputIn"])
        bytes_rates_out += float(stats_json["msgThroughputOut"])
        # print(msg_rates_in)
        # print(msg_rates_out)
        # print(bytes_rates_in)
        # print(bytes_rates_out)
    print("Iteration {0: <2}, {1: <20} , {2: <20} , {3: <20} , {4: <20}".format(i, msg_rates_in, msg_rates_out, bytes_rates_in, bytes_rates_out))
    time.sleep(30)





