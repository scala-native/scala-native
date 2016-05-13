#!/usr/bin/env bash

SCALANATIVE_DOCKER_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SCALANATIVE_HOME_PATH=$SCALANATIVE_DOCKER_PATH/../

case "$1" in
  build)
    docker -D build -t="ubuntu-scalanative:latest" .
  ;;
  run)
    #remember to start nvidia-docker-plugin
    docker run --rm -i -v $SCALANATIVE_HOME_PATH:/home/sources:rw -t "ubuntu-scalanative:latest"
  ;;
  *)
    echo "Usage [ build, run ] "
    exit 0
  ;;
esac
