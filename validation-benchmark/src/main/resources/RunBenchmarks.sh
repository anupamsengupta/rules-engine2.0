#!/bin/bash

# Script to run JMH benchmarks for expression evaluator performance comparison

echo "Building benchmark module..."
mvn clean compile -q -pl validation-benchmark

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo ""
echo "Running benchmarks..."
echo ""

# Run all benchmarks using exec:java
mvn exec:java \
    -Dexec.args=".*Benchmark -f 2 -wi 5 -i 10" \
    -pl validation-benchmark

