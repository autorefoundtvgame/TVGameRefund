#!/bin/bash

# Build the Docker image
docker build -t tvgamerefund-builder .

# Run Gradle build in the container
docker run --rm -v $(pwd):/app -v gradle-cache:/gradle tvgamerefund-builder ./gradlew build