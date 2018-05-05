FROM arm32v7/ubuntu:16.04

ENV SBT_VERSION 1.1.2

RUN apt-get update && apt-get install -y wget software-properties-common

RUN apt-get install -y openjdk-8-jdk

RUN apt-get install -y curl

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt

RUN wget -O - https://apt.llvm.org/llvm-snapshot.gpg.key | apt-key add -

RUN apt-add-repository "deb http://apt.llvm.org/xenial/ llvm-toolchain-xenial-5.0 main" && apt-get update

RUN apt-get install -y clang-5.0

RUN apt-get install -y libunwind8-dev liblzma-dev zlib1g-dev libre2-dev

RUN apt-get install -y gcc git autoconf libtool pkg-config

RUN git clone git://github.com/ivmai/bdwgc.git && \
    cd bdwgc && \
    git checkout v7.6.4 && \
    git clone git://github.com/ivmai/libatomic_ops.git -b release-7_6 && \
    ./autogen.sh && \
    ./configure && \
    make -j && \
    make install

ENV LD_LIBRARY_PATH "/usr/local/lib"

ENV LC_ALL "C.UTF-8"

ENV LANG "C.UTF-8"

RUN useradd -ms /bin/bash scala-native

RUN update-ca-certificates -f

USER scala-native

RUN cd /home/scala-native && mkdir project && echo "sbt.version=0.13.17" >> project/build.properties && \
    sbt exit && rm -rf project && rm -rf target

WORKDIR /home/scala-native

CMD ["sbt", "dirty-rebuild", "test-all"]
