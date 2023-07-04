# syntax=docker/dockerfile:1
ARG BASE_IMAGE

FROM --platform=${TARGETPLATFORM} $BASE_IMAGE as cross
# Platform args are populated by buildx, needs to be defined after FROM command
ARG BUILDPLATFORM
ARG TARGETPLATFORM
ARG LLVM_VERSION
ARG BUILD_DEPS
RUN echo "Running on $BUILDPLATFORM, building for $TARGETPLATFORM, LLVM toolchain: $LLVM_VERSION"
RUN apt-get update && apt-get install -y zip unzip lsb-release curl wget software-properties-common iputils-ping libgc-dev libz-dev git

RUN wget -O - https://apt.llvm.org/llvm.sh | bash /dev/stdin $LLVM_VERSION
RUN update-alternatives --install /usr/bin/clang clang /usr/bin/clang-$LLVM_VERSION 100
RUN update-alternatives --install /usr/bin/clang++ clang++ /usr/bin/clang++-$LLVM_VERSION 100

# We cannot easily install dependencies, clone and build zlib and Boehm locally
RUN $BUILD_DEPS \
    && git clone https://github.com/madler/zlib /tmp/zlib \
    && cd /tmp/zlib/ \
    && git checkout v1.2.11 \
    && ./configure \
    && make install prefix=$QEMU_LD_PREFIX \
    && rm -rf /tmp/zlib \
    || echo "Skip building zlib"

RUN $BUILD_DEPS \
    && git clone https://github.com/ivmai/bdwgc /tmp/bdwgc \
    && cd /tmp/bdwgc/ \
    && git checkout v8.0.6 \
    && git clone https://github.com/ivmai/libatomic_ops \
    && cd libatomic_ops/ \
    && git checkout v7.6.12 \
    && cd .. \
    && ./autogen.sh \
    && ./configure --host $CROSS_TRIPLE \
    && make install prefix=$QEMU_LD_PREFIX \
    && rm -rf /tmp/bdwgc \
    || echo "Skip building Boehm GC"

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
ENV ENABLE_EXPERIMENTAL_COMPILER=true

CMD sbt \
  "-Dscala.scalanative.testinterface.processrunner.emulator=$TARGET_EMULATOR" \
  "-J-Xmx5G" \
  "set sbtScalaNative/scriptedBufferLog := false" \
  "$TEST_COMMAND"
