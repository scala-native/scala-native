ARG BASE_IMAGE
ARG LLVM_VERSION=14

FROM $BASE_IMAGE

RUN apt-get update && \
  apt-get install -y zip unzip lsb-release curl wget software-properties-common libgc-dev libz-dev

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
