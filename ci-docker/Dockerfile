ARG TARGET_DOCKER_PLATFORM

FROM ubuntu:18.04

RUN apt-get update && apt-get install -y qemu-user-static

FROM $TARGET_DOCKER_PLATFORM/ubuntu:18.04

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

RUN apt-get update

RUN apt-get install -y openjdk-8-jdk-headless:$HOST_ARCHITECTURE
ENV PATH "/usr/lib/jvm/java-8-openjdk-$HOST_ARCHITECTURE/bin:$PATH"

ENV SBT_LAUNCHER_VERSION 1.3.7
ENV SBT_INSTALL_FILE sbt-$SBT_LAUNCHER_VERSION.deb
ENV SBT_INSTALL_URL \
        https://repo.scala-sbt.org/scalasbt/debian/$SBT_INSTALL_FILE

RUN apt-get install -y curl

# Install sbt
RUN \
  curl -L -O $SBT_INSTALL_URL && \
  dpkg -i $SBT_INSTALL_FILE && \
  rm $SBT_INSTALL_FILE

RUN apt-get install -y clang-6.0 zlib1g-dev libgc-dev git

ENV LC_ALL "C.UTF-8"

ENV LANG "C.UTF-8"

ENV PATH=/usr/lib/llvm-6.0/bin:${PATH}

RUN useradd -ms /bin/bash scala-native

USER scala-native

WORKDIR /home/scala-native/scala-native

CMD sbt "++ $SCALA_VERSION -v" -no-colors -J-Xmx5G "set scriptedBufferLog in sbtScalaNative := false" "$TEST_COMMAND"
