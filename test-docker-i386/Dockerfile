FROM i386/ubuntu:16.04

ENV SBT_VERSION 1.1.2

RUN apt-get update && apt-get install -y wget software-properties-common

RUN add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer && \
    apt-get clean

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

RUN apt-get install -y zlib1g-dev libre2-dev

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

USER scala-native

WORKDIR /home/scala-native/scala-native

CMD ["sbt", "^^ $SBT_VERSION", "dirty-rebuild_arch-i386", "test-all_arch-i386"]
