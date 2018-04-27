FROM arm32v7/openjdk:8-jdk

ENV SBT_VERSION 1.1.2

ENV SBT_OPTS "-Dsbt.server.autostart=false"

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt

RUN apt-get update && apt-get install -y wget software-properties-common

RUN wget http://releases.llvm.org/5.0.0/clang+llvm-5.0.0-armv7a-linux-gnueabihf.tar.xz

RUN echo 'export PATH="$PATH:/llvm/clang+llvm-5.0.0-armv7a-linux-gnueabihf/bin"' >> ~/.bashrc && sh ~/.bashrc

RUN rm /llvm/clang+llvm-5.0.0-armv7a-linux-gnueabihf/lib/libunwind*

RUN mkdir llvm && tar xf clang+llvm-5.0.0-armv7a-linux-gnueabihf.tar.xz && mv clang+llvm-5.0.0-armv7a-linux-gnueabihf llvm

RUN apt-get install -y libc6-dev libc++-dev gcc g++ libgc-dev libunwind8-dev zlib1g-dev libre2-dev
