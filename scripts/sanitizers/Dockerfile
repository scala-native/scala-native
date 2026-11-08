# Linux environment for scripts/sanitizers/run.sh: clang (ASan/LSan/UBSan)
# and Valgrind. LeakSanitizer and Valgrind are not usable on macOS.
FROM ubuntu:24.04

ARG LLVM_VERSION=18
RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y \
      clang-$LLVM_VERSION lld-$LLVM_VERSION llvm-$LLVM_VERSION \
      libgc-dev zlib1g-dev valgrind \
      curl zip unzip git ca-certificates \
  && rm -rf /var/lib/apt/lists/*

RUN useradd -ms /bin/bash scala-native
USER scala-native
WORKDIR /home/scala-native/scala-native
RUN git config --global --add safe.directory /home/scala-native/scala-native

SHELL ["/bin/bash", "-c"]
RUN curl -s "https://get.sdkman.io" | bash \
  && . "$HOME/.sdkman/bin/sdkman-init.sh" \
  && sdk install sbt 1.12.9 \
  && sdk install java 25.0.3-tem

ENV LC_ALL="C.UTF-8" LANG="C.UTF-8"
ENV LLVM_BIN=/usr/lib/llvm-$LLVM_VERSION/bin
ENV PATH=/usr/lib/llvm-$LLVM_VERSION/bin:/home/scala-native/.sdkman/candidates/java/current/bin:/home/scala-native/.sdkman/candidates/sbt/current/bin:${PATH}
