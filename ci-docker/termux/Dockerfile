FROM termux/termux-docker:aarch64@sha256:3a0a340fca9947f7139a0d695f1234edf0172daed6a2f1e0b1413ef91453c9ce

# The upstream entrypoint clears the environment when started as root.
# Package installation and tests must run as the Termux system user.
USER 1000:1000
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y clang openjdk-21 git curl zlib which \
        libandroid-posix-semaphore libandroid-wordexp && \
    mkdir -p "$PREFIX/share" && \
    curl -fL --retry 3 \
        https://github.com/sbt/sbt/releases/download/v2.0.9/sbt-2.0.9.tgz | \
        tar -xz -C "$PREFIX/share" sbt/bin/sbt sbt/bin/sbt-launch.jar

# Any sbt 2 runner can launch the version selected by project/build.properties.
# Adapt the runner's shebang to Termux's filesystem layout.
RUN sed -i "1s|.*|#!$PREFIX/bin/bash|" "$PREFIX/share/sbt/bin/sbt" && \
    ln -s "$PREFIX/share/sbt/bin/sbt" "$PREFIX/bin/sbt"

# Create parents before Docker mounts caches; otherwise Docker creates them
# as root and sbt cannot write its action cache next to the Coursier cache.
RUN mkdir -p "$HOME/.cache/coursier" "$HOME/.cache/sbt" "$HOME/.sbt" "$HOME/.ivy2"

COPY --chown=1000:1000 run-tests.sh /data/data/com.termux/files/home/run-tests.sh
RUN sed -i "1s|.*|#!$PREFIX/bin/bash|" /data/data/com.termux/files/home/run-tests.sh && \
    chmod +x /data/data/com.termux/files/home/run-tests.sh
WORKDIR /workspace
ENV GITHUB_WORKSPACE=/workspace
ENV CI=true
ENV ENABLE_EXPERIMENTAL_COMPILER=true
ENV SCALANATIVE_MODE=debug
ENV SCALANATIVE_GC=immix
# GC regressions allocate arrays larger than 256 MB and need contiguous space.
ENV GC_MAXIMUM_HEAP_SIZE=4G
ENV SCALANATIVE_LTO=none
ENV SCALANATIVE_TEST_DEBUG_SIGNALS=1
ENV SCALANATIVE_TEST_PREFETCH_DEBUG_INFO=1
CMD ["bash", "/data/data/com.termux/files/home/run-tests.sh"]
