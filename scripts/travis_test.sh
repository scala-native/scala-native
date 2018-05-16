#!/usr/bin/env bash
sudo chmod a+rwx -R $HOME;

docker run $DOCKER_OPTIONS -it $TEST_CONTAINER java -version

docker run -v $HOME/.ivy2:/home/scala-native/.ivy2 -v $HOME/.sbt:/home/scala-native/.sbt -v $PWD:/home/scala-native/scala-native -e SCALANATIVE_GC=$SCALANATIVE_GC -e SBT_VERSION=$SBT_VERSION -e TEST_COMMAND=$TEST_COMMAND $DOCKER_OPTIONS -it $TEST_CONTAINER;
