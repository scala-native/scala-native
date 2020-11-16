#!/usr/bin/env bash
set -e
set -x

if [ $# -eq 0 ]
  then echo "Expected single argument with docker image version"
  exit 1
fi
IMAGE_VERSION=$1

sudo chmod 777 -R $HOME;

docker run -i scala-native-testing/linux-$TARGET_DOCKER_PLATFORM:${IMAGE_VERSION} java -version

docker run -v $HOME/.ivy2:/home/scala-native/.ivy2 \
           -v $HOME/.sbt:/home/scala-native/.sbt \
           -v $PWD:/home/scala-native/scala-native \
           -e SCALANATIVE_MODE=$SCALANATIVE_MODE \
           -e SCALANATIVE_GC=$SCALANATIVE_GC \
           -e SCALANATIVE_OPTIMIZE=$SCALANATIVE_OPTIMIZE \
           -e TEST_COMMAND=$TEST_COMMAND \
           -e SCALA_VERSION=$SCALA_VERSION \
           -i scala-native-testing/linux-$TARGET_DOCKER_PLATFORM:${IMAGE_VERSION}

