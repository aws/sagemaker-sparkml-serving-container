#!/bin/bash
echo "starting Inference server JAR..."
# This is needed to make sure Java correctly detects CPU/Memory set by the container limits
java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -jar /usr/local/lib/spark-serving-assembly-0.1.0-SNAPSHOT.jar