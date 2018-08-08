FROM ubuntu:18.04

ENV SBT_LAUNCHER_VERSION 1.2.0

RUN apt-get update && apt-get install -y wget software-properties-common

RUN apt-get install -y openjdk-8-jdk-headless

RUN apt-get install -y curl

# Install sbt
RUN \
  curl -L -o sbt-$SBT_LAUNCHER_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_LAUNCHER_VERSION.deb && \
  dpkg -i sbt-$SBT_LAUNCHER_VERSION.deb && \
  rm sbt-$SBT_LAUNCHER_VERSION.deb && \
  apt-get update && \
  apt-get install -y sbt

RUN wget -O - https://apt.llvm.org/llvm-snapshot.gpg.key | apt-key add -

RUN apt-add-repository "deb http://apt.llvm.org/bionic/ llvm-toolchain-bionic-5.0 main" && apt-get update

RUN apt-get install -y clang-5.0

RUN apt-get install -y zlib1g-dev libgc-dev libre2-dev

ENV LD_LIBRARY_PATH "/usr/local/lib"

ENV LC_ALL "C.UTF-8"

ENV LANG "C.UTF-8"

RUN useradd -ms /bin/bash scala-native

USER scala-native

WORKDIR /home/scala-native/scala-native

CMD sbt -no-colors -J-Xmx2G "^^ $SBT_VERSION" "set scriptedBufferLog in sbtScalaNative := false" "$TEST_COMMAND"
