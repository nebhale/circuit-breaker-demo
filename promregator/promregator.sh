#!/usr/bin/env bash

set -euo pipefail

PASSWORD="$1"
VERSION=0.5.7

docker run \
  --name promregator \
  --memory 600m \
  --env CF_PASSWORD="$PASSWORD" \
  --volume "$(pwd)/promregator.yml":/etc/promregator/promregator.yml \
  --publish 56710:8080 \
  promregator/promregator:$VERSION
