FROM i386/ubuntu

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

RUN mkdir project && echo 'sbt.version=0.13.17' >> project/build.properties

RUN sbt exit && rm -rf project && rm -rf target

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

RUN echo 'export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/usr/local/lib"' >> ~/.bashrc && bash ~/.bashrc
