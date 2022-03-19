# For list of supported platforms check https://github.com/dockcross/dockcross#summary-cross-compilers
ARG TARGET_PLATFORM
FROM dockcross/$TARGET_PLATFORM
ENV DEFAULT_DOCKCROSS_IMAGE testing-container

RUN apt-get update && apt-get install -y clang lld
# We cannot easily install dependencies, clone and build zlib locally
# We might need to do the same for Boehmc GC if we would use it in the future
# Use default user so you can have acess to /cxx/ cross directory
RUN git clone https://github.com/madler/zlib /tmp/zlib \
  && cd /tmp/zlib/ \
  && ./configure \
  && make install prefix=$CROSS_ROOT/${CROSS_TRIPLE}/sysroot \
  && rm -rf /tmp/zlib

# Switch shell and user to allow for usage of sdk and installed by it binaries 
SHELL ["/bin/bash", "-c"]
RUN useradd -ms /bin/bash scala-native
USER scala-native
WORKDIR /home/scala-native/scala-native

RUN curl -s "https://get.sdkman.io" | bash  \
  && . "$HOME/.sdkman/bin/sdkman-init.sh" \
  && sdk install sbt 1.6.2 \ 
  && sdk install java 8.0.275.hs-adpt

ENV LC_ALL "C.UTF-8"
ENV LANG "C.UTF-8"
ENV PATH=/usr/lib/llvm-6.0/bin:~/.sdkman/candidates/java/current/bin:~/.sdkman/candidates/sbt/current/bin:${PATH}

CMD sbt "++ $SCALA_VERSION -v" \
  "-Dscala.scalanative.testinterface.processrunner.emulator=$TARGET_EMULATOR" \
  "-J-Xmx5G" \
  "set sbtScalaNative/scriptedBufferLog := false" \
  "$TEST_COMMAND"
