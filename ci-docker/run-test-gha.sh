#!/usr/bin/env bash
set -e
set -x

if [ $# -eq 0 ]
  then echo "Expected single argument with docker image version"
  exit 1
fi

IMAGE_NAME=$1
FULL_IMAGE_NAME="localhost:5000/${IMAGE_NAME}"
sudo chmod a+rwx -R "$HOME"

docker run -d -p 5000:5000 \
  --restart=always \
  --name registry \
  -v /tmp/docker-registry:/var/lib/registry \
  registry:2 && \
  npx wait-on tcp:5000

docker run -i "${FULL_IMAGE_NAME}" java -version
docker run -v $HOME/.ivy2:/home/scala-native/.ivy2 \
           -v $HOME/.sbt:/home/scala-native/.sbt \
           -v $PWD:/home/scala-native/scala-native \
           -e SCALANATIVE_MODE="$SCALANATIVE_MODE" \
           -e SCALANATIVE_GC="$SCALANATIVE_GC" \
           -e SCALANATIVE_OPTIMIZE="$SCALANATIVE_OPTIMIZE" \
           -e TEST_COMMAND="$TEST_COMMAND" \
           -e SCALA_VERSION="$SCALA_VERSION" \
           -i "${FULL_IMAGE_NAME}"
