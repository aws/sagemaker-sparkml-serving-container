#!/bin/bash
# This is needed to make sure Java correctly detects CPU/Memory set by the container limits
java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -jar /usr/local/lib/sparkml-model-server-1.0-SNAPSHOT.jar