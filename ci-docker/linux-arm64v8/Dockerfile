FROM library/debian:stretch

RUN apt-get update && apt-get install -y qemu-user-static

FROM arm64v8/debian:stretch

COPY --from=0 /usr/bin/qemu-aarch64-static /usr/bin/qemu-aarch64-static

RUN dpkg --add-architecture amd64

RUN apt-get update && apt-get install -y openjdk-8-jdk-headless:amd64

ENV PATH "/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH"

ENV SBT_LAUNCHER_VERSION 1.2.0

RUN apt-get install -y wget curl

# Install sbt
RUN \
  curl -L -o sbt-$SBT_LAUNCHER_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_LAUNCHER_VERSION.deb && \
  dpkg -i sbt-$SBT_LAUNCHER_VERSION.deb && \
  rm sbt-$SBT_LAUNCHER_VERSION.deb && \
  apt-get update && \
  apt-get install -y sbt

RUN echo "deb http://deb.debian.org/debian stretch-backports main contrib non-free" >> /etc/apt/sources.list

RUN apt-get update && apt-get install -y clang-5.0 zlib1g-dev libgc-dev libre2-dev

ENV LD_LIBRARY_PATH "/usr/local/lib"

ENV LC_ALL "C.UTF-8"

ENV LANG "C.UTF-8"

RUN useradd -ms /bin/bash scala-native

USER scala-native

WORKDIR /home/scala-native/scala-native

CMD sbt -no-colors -J-Xmx2G "^^ $SBT_VERSION" "set scriptedBufferLog in sbtScalaNative := false" "$TEST_COMMAND"
