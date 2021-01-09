ARG TARGET_DOCKER_PLATFORM

FROM ubuntu:20.04

RUN apt-get update && apt-get install -y qemu-user-static

FROM $TARGET_DOCKER_PLATFORM/ubuntu:20.04

ARG HOST_ARCHITECTURE

COPY --from=0 /usr/bin/qemu-* /usr/bin/
COPY --from=0 /etc/apt/sources.list /etc/apt/host-sources.list

RUN dpkg --add-architecture $HOST_ARCHITECTURE

# Add lists for the host platform and mark architectures
RUN sed -i -e "s/deb http/deb [arch=$HOST_ARCHITECTURE] http/g" /etc/apt/host-sources.list && \
    TARGET_UBUNTU_ARCH=$(dpkg --print-architecture) && \
    sed -i -e "s/deb http/deb [arch=$TARGET_UBUNTU_ARCH] http/g" /etc/apt/sources.list && \
    cat /etc/apt/host-sources.list >> /etc/apt/sources.list && \
    rm /etc/apt/host-sources.list

RUN apt-get update && apt-get install -y openjdk-8-jdk-headless:$HOST_ARCHITECTURE
ENV PATH "/usr/lib/jvm/java-8-openjdk-$HOST_ARCHITECTURE/bin:$PATH"

ENV SBT_LAUNCHER_VERSION 1.3.7

RUN apt-get install -y curl

# Install sbt
RUN \
  curl -L -o sbt-$SBT_LAUNCHER_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_LAUNCHER_VERSION.deb && \
  dpkg -i sbt-$SBT_LAUNCHER_VERSION.deb && \
  rm sbt-$SBT_LAUNCHER_VERSION.deb && \
  apt-get update && \
  apt-get install -y sbt

RUN DEBIAN_FRONTEND="noninteractive" apt-get install -y clang:$TARGET_DOCKER_PLATFORM zlib1g-dev:$TARGET_DOCKER_PLATFORM libre2-dev:$TARGET_DOCKER_PLATFORM libgc-dev:$TARGET_DOCKER_PLATFORM

ENV LC_ALL "C.UTF-8"

ENV LANG "C.UTF-8"

RUN useradd -ms /bin/bash scala-native

USER scala-native

WORKDIR /home/scala-native/scala-native

CMD sbt "++ $SCALA_VERSION -v" -no-colors -J-Xmx5G "set scriptedBufferLog in sbtScalaNative := false" "$TEST_COMMAND"
