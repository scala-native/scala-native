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

ENV SBT_LAUNCHER_VERSION 1.3.7
ENV SBT_INSTALL_FILE sbt-$SBT_LAUNCHER_VERSION.deb
ENV SBT_INSTALL_URL \
        https://repo.scala-sbt.org/scalasbt/debian/$SBT_INSTALL_FILE

RUN apt-get install -y curl

# Install sbt
RUN \
  curl -L -O $SBT_INSTALL_URL && \
  dpkg -i $SBT_INSTALL_FILE && \
  rm $SBT_INSTALL_FILE && \
  apt-get update && \
  apt-get install -y sbt

RUN apt-get update && apt-get install -y clang-6.0 zlib1g-dev libgc-dev

ENV LC_ALL "C.UTF-8"

ENV LANG "C.UTF-8"

ENV PATH=/usr/lib/llvm-6.0/bin:${PATH}

RUN useradd -ms /bin/bash scala-native

USER scala-native

WORKDIR /home/scala-native/scala-native

# Put on your Personal Protective Equipment (PPE) before proceeding!
#
# Using sbt -ivy option is workaround required by sbt +/- 1.5.0
# to prevent sbt lock file errors in Docker when changing build.sbt.
# See Scala Native Issue #2260 and Sbt Issue #6634.
# This appears to be the singular critical intervention point.
# Other uses of sbt, say in .yml files, seem to work after the change here.
# Sbt 1.5.n mainly uses Coursier and is moving off using Ivy2, so there
# should be only a minimal, and lessening, re-fetch performance hit.

CMD sbt \
    -ivy /tmp/.ivy2 \
    -no-colors \
    -J-Xmx5G \
    "++ $SCALA_VERSION -v" \
    "set sbtScalaNative / scriptedBufferLog := false" \
    "$TEST_COMMAND"
