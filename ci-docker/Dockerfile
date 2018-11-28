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

RUN apt-get update && apt-get install -y openjdk-8-jdk-headless:$HOST_ARCHITECTURE
ENV PATH "/usr/lib/jvm/java-8-openjdk-$HOST_ARCHITECTURE/bin:$PATH"

ENV SBT_LAUNCHER_VERSION 0.13.7

RUN apt-get install -y curl

# Install sbt
RUN \
  curl -L -o sbt-$SBT_LAUNCHER_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_LAUNCHER_VERSION.deb && \
  dpkg -i sbt-$SBT_LAUNCHER_VERSION.deb && \
  rm sbt-$SBT_LAUNCHER_VERSION.deb && \
  apt-get update && \
  apt-get install -y sbt

RUN apt-get update && apt-get install -y clang-5.0 zlib1g-dev libgc-dev libre2-dev

ENV LC_ALL "C.UTF-8"

ENV LANG "C.UTF-8"

RUN useradd -ms /bin/bash scala-native

USER scala-native

WORKDIR /home/scala-native/scala-native

CMD sbt -no-colors -J-Xmx3G "^^ $SBT_VERSION" rebuild "set scriptedBufferLog in sbtScalaNative := false" "$TEST_COMMAND"
