#!/bin/bash

# Get the Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')

# Extract major and minor version numbers
JAVA_MAJOR=$(echo "$JAVA_VERSION" | awk -F. '{print $1}')
JAVA_MINOR=$(echo "$JAVA_VERSION" | awk -F. '{print $2}')

if [[ "$JAVA_MAJOR" -ge 9 ]]; then
    JAVA_MINOR=0
fi

# Check if Java version is 1.8 or higher
if [[ "$JAVA_MAJOR" -gt 1 || ("$JAVA_MAJOR" -eq 1 && "$JAVA_MINOR" -ge 8) ]]; then
    echo "Java version $JAVA_VERSION is compatible."
else
    echo "Error: Java version $JAVA_VERSION is too old. Please install Java 1.8 or newer."
    exit 1
fi


java -cp target/CumberBench_J-1.0-SNAPSHOT.jar:lib/* App
