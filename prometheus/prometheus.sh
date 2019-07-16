#!/usr/bin/env bash

set -euo pipefail

docker run \
  --name prometheus \
  --volume "$(pwd)/prometheus.yml":/etc/prometheus/prometheus.yml \
  --publish 9090:9090 \
  --link promregator \
  prom/prometheus:latest
