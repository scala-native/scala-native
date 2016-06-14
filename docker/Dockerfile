FROM ubuntu:16.04

MAINTAINER andrea peruffo <andrea.peruffo1982@gmail.com>

# --- --- ---  Proxy Settings --- --- ---
#UNCOMMENT IF BHEIND A PROXY
#SET A PROPER PROXY IP

#ENV DOCKER_PROXY PROXY_IP

#ENV http_proxy ${DOCKER_PROXY}
#ENV HTTP_PROXY ${DOCKER_PROXY}
#ENV https_proxy ${DOCKER_PROXY}
#ENV HTTPS_PROXY ${DOCKER_PROXY}
#ENV NO_PROXY '127.0.0.1, localhost, /var/run/docker.sock'

ENV SCALA_VERSION 2.11.8
ENV SBT_VERSION 0.13.11

RUN apt-get update && \
    apt-get install -y \
    wget tar curl git \
    openjdk-8-jdk \
    clang++-3.7 llvm-3.7 llvm-3.7-dev llvm-3.7-runtime llvm-3.7-tool \
    libgc-dev

ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk

RUN \
  curl -fsL http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> /root/.bashrc

RUN \
  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb

RUN apt-get update && \
    apt-get -y install sbt

WORKDIR /tmp

RUN git clone https://github.com/scala-native/scala-native.git

WORKDIR /tmp/scala-native

RUN sbt 'rtlib/publishLocal' && sbt 'nscplugin/publishLocal'

WORKDIR /home/sources

CMD ["/bin/bash"]
