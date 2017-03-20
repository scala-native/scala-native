FROM ubuntu:16.04

RUN apt-get -qq update
RUN apt-get install -y wget
RUN sh -c "echo 'deb http://llvm.org/apt/precise/ llvm-toolchain-precise-3.7 main' >> /etc/apt/sources.list"
RUN sh -c "echo 'deb http://llvm.org/apt/precise/ llvm-toolchain-precise main' >> /etc/apt/sources.list"

# use storage driver devicemapper or it fails on renaming
# https://github.com/docker/docker/issues/26317#issuecomment-244895700
RUN wget -O - http://llvm.org/apt/llvm-snapshot.gpg.key | apt-key add -
RUN apt-get install -y software-properties-common
RUN add-apt-repository --yes ppa:ubuntu-toolchain-r/test
RUN apt-get -qq update
RUN apt-get install -y wget
RUN apt-get install -y tar
RUN apt-get install -y curl
RUN apt-get install -y git
RUN apt-get install -y openjdk-8-jdk
RUN apt-get install -y clang++-3.7
RUN apt-get install -y llvm-3.7
RUN apt-get install -y llvm-3.7-dev
RUN apt-get install -y llvm-3.7-runtime
RUN apt-get install -y llvm-3.7-tool
RUN apt-get install -y libgc-dev
RUN apt-get install -y libunwind-dev

# Install SBT
ENV SBT_VERSION 0.13.13
RUN curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb
RUN dpkg -i sbt-$SBT_VERSION.deb
RUN rm sbt-$SBT_VERSION.deb
RUN apt-get update
RUN apt-get install sbt

ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk
ENV SBT_OPTS -Xms512M -Xmx1536M

WORKDIR /root

# Run a dummy scala project
RUN mkdir scala-211
RUN mkdir scala-211/project
RUN echo 'sbt.version = 0.13.13' > scala-211/project/build.properties
RUN echo 'scalaVersion := "2.11.8"' > scala-211/build.sbt
RUN mkdir scala-211/src
RUN mkdir scala-211/src/main
RUN mkdir scala-211/src/main/scala
RUN echo 'object Main { def main(args: Array[String]): Unit = println("Hello, World!") }' > scala-211/src/main/scala/Main.scala
RUN cd scala-211; sbt run

# Run a dummy scala project
RUN mkdir scala-210
RUN mkdir scala-210/project
RUN echo 'sbt.version = 0.13.13' > scala-210/project/build.properties
RUN echo 'scalaVersion := "2.10.6"' > scala-210/build.sbt
RUN mkdir scala-210/src
RUN mkdir scala-210/src/main
RUN mkdir scala-210/src/main/scala
RUN echo 'object Main { def main(args: Array[String]): Unit = println("Hello, World!") }' > scala-210/src/main/scala/Main.scala
RUN cd scala-210; sbt run

# git clone git://github.com/scala-native/scala-native.git

CMD ["/bin/bash"]

