FROM ubuntu:24.04
RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    ca-certificates curl openssh-client qemu-system-x86 qemu-utils rsync socat zstd \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /vm
