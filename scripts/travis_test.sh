#!/usr/bin/env bash
java -version;

docker run --rm --privileged multiarch/qemu-user-static:register;

docker build -t $TEST_CONTAINER $TEST_CONTAINER

sudo chmod a+rwx -R $HOME;

docker run -v $HOME/.ivy2:/home/scala-native/.ivy2 -v $HOME/.sbt:/home/scala-native/.sbt -v $PWD:/home/scala-native/scala-native -e SCALANATIVE_GC=$SCALANATIVE_GC -e SBT_VERSION=$SBT_VERSION -it $TEST_CONTAINER;
