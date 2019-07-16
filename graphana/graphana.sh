#!/usr/bin/env bash

set -euo pipefail

docker run \
  --name=grafana \
  --publish 3000:3000 \
  --link prometheus \
  grafana/grafana:latest
