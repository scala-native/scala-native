# syntax=docker/dockerfile:1
ARG BASE_IMAGE
ARG LLVM_VERSION

FROM --platform=${TARGETPLATFORM} $BASE_IMAGE as cross
# Platform args are populated by buildx, needs to be defined after FROM command
ARG BUILDPLATFORM
ARG TARGETPLATFORM
RUN echo "Running on $BUILDPLATFORM, building for $TARGETPLATFORM, base image: ${BASE_IMAGE}, LLVM toolchain: ${LLVM_VERSION}"
RUN apt-get update && apt-get install -y zip unzip lsb-release curl wget software-properties-common libgc-dev libz-dev

RUN wget -O - https://apt.llvm.org/llvm.sh | bash /dev/stdin $LLVM_VERSION
RUN update-alternatives --install /usr/bin/clang clang /usr/bin/clang-$LLVM_VERSION 100
RUN update-alternatives --install /usr/bin/clang++ clang++ /usr/bin/clang++-$LLVM_VERSION 100

# Switch shell and user to allow for usage of sdk and installed by it binaries 
SHELL ["/bin/bash", "-c"]
RUN useradd -ms /bin/bash scala-native
USER scala-native
WORKDIR /home/scala-native/scala-native

RUN curl -s "https://get.sdkman.io" | bash  \
  && . "$HOME/.sdkman/bin/sdkman-init.sh" \
  && sdk install sbt 1.7.2 \ 
  && sdk install java 8.0.332-tem

ENV LC_ALL "C.UTF-8"
ENV LANG "C.UTF-8"
ENV PATH=/usr/lib/llvm-$LLVM_VERSION/bin:~/.sdkman/candidates/java/current/bin:~/.sdkman/candidates/sbt/current/bin:${PATH}

CMD sbt "++ $SCALA_VERSION -v" \
  "-J-Xmx5G" \
  "set sbtScalaNative/scriptedBufferLog := false" \
  "$TEST_COMMAND"
