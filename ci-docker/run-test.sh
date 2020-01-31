#!/usr/bin/env bash
set -e

docker run -it scala-native-testing:linux-$TARGET_DOCKER_PLATFORM java -version

docker run -v $HOME/.ivy2:/home/scala-native/.ivy2 \
           -v $HOME/.sbt:/home/scala-native/.sbt \
           -v $PWD:/home/scala-native/scala-native \
           -e SCALANATIVE_MODE=$SCALANATIVE_MODE \
           -e SCALANATIVE_GC=$SCALANATIVE_GC \
           -e TEST_COMMAND=$TEST_COMMAND \
           -it scala-native-testing:linux-$TARGET_DOCKER_PLATFORM;
