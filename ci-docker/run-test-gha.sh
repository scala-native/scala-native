#!/usr/bin/env bash
set -e
set -x

sudo chmod a+rwx -R $HOME;

docker run \
 --rm \
 --privileged \
 multiarch/qemu-user-static:register

docker images | grep scala-native-testing

docker build \
  --cache-from scala-native-testing:linux-$TARGET_DOCKER_PLATFORM \
  -t scala-native-testing:linux-$TARGET_DOCKER_PLATFORM \
  --build-arg TARGET_DOCKER_PLATFORM=$TARGET_DOCKER_PLATFORM \
  --build-arg HOST_ARCHITECTURE=amd64 \
  --cpuset-cpus=0 \
  ci-docker

docker run -i scala-native-testing:linux-$TARGET_DOCKER_PLATFORM java -version

docker run -v $HOME/.ivy2:/home/scala-native/.ivy2:z \
           -v $HOME/.sbt:/home/scala-native/.sbt:z \
           -v $PWD:/home/scala-native/scala-native:z \
           -e SCALANATIVE_MODE=$SCALANATIVE_MODE \
           -e SCALANATIVE_GC=$SCALANATIVE_GC \
           -e SCALANATIVE_OPTIMIZE=$SCALANATIVE_OPTIMIZE \
           -e TEST_COMMAND=$TEST_COMMAND \
           -e SCALA_VERSION=$SCALA_VERSION \
           -i scala-native-testing:linux-$TARGET_DOCKER_PLATFORM;

