# syntax=docker/dockerfile:1
ARG BASE_IMAGE

FROM $BASE_IMAGE 
# Platform args are populated by buildx, needs to be defined after FROM command
ARG LLVM_VERSION
ARG IMAGE_NAME
ENV DEFAULT_DOCKCROSS_IMAGE=${IMAGE_NAME}

RUN echo "BASE_IMAGE: $BASE_IMAGE, LLVM toolchain: $LLVM_VERSION"
RUN apt-get update && apt-get install -y zip unzip lsb-release curl wget software-properties-common iputils-ping libgc-dev libz-dev git

RUN wget -O - https://apt.llvm.org/llvm.sh | bash /dev/stdin $LLVM_VERSION
RUN update-alternatives --install /usr/bin/clang clang /usr/bin/clang-$LLVM_VERSION 100
RUN update-alternatives --install /usr/bin/clang++ clang++ /usr/bin/clang++-$LLVM_VERSION 100

# We cannot easily install dependencies, clone and build zlib and Boehm locally
RUN git clone https://github.com/madler/zlib /tmp/zlib \
    && cd /tmp/zlib/ \
    && git checkout v1.2.11 \
    && ./configure \
    && make install prefix=$QEMU_LD_PREFIX \
    && rm -rf /tmp/zlib

RUN git clone https://github.com/ivmai/bdwgc /tmp/bdwgc \
    && cd /tmp/bdwgc/ \
    && git checkout v8.0.6 \
    && git clone https://github.com/ivmai/libatomic_ops \
    && cd libatomic_ops/ \
    && git checkout v7.6.12 \
    && cd .. \
    && ./autogen.sh \
    && ./configure --host $CROSS_TRIPLE \
    && make install prefix=$QEMU_LD_PREFIX \
    && rm -rf /tmp/bdwgc


# Switch shell and user to allow for usage of sdk and installed by it binaries 
SHELL ["/bin/bash", "-c"]
RUN useradd -ms /bin/bash scala-native
WORKDIR /home/scala-native/scala-native
# Fix Git "dubious ownership" error for mounted repo
RUN git config --global --add safe.directory /home/scala-native/scala-native

RUN curl -s "https://get.sdkman.io" | bash  \
  && . "$HOME/.sdkman/bin/sdkman-init.sh" \
  && sdk install sbt 1.10.1 \ 
  && sdk install java 8.0.332-tem

ENV LC_ALL="C.UTF-8"
ENV LANG="C.UTF-8"
ENV PATH=/usr/lib/llvm-$LLVM_VERSION/bin:~/.sdkman/candidates/java/current/bin:~/.sdkman/candidates/sbt/current/bin:${PATH}
ENV ENABLE_EXPERIMENTAL_COMPILER=true
ENV GITHUB_WORKSPACE=/home/scala-native/scala-native

# For some reasons the x86 builds cannot find built dependencies 
RUN dpkg --add-architecture i386 && \
    apt-get update && apt-get install -y libgc-dev:i386 libz-dev:i386 libatomic1:i386

CMD sbt \
  "-J-Xmx7G" \
  "set sbtScalaNative/scriptedBufferLog := false" \
  "$TEST_COMMAND"
