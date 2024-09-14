#!/usr/bin/env bash
set -e
set -x

if [ $# -ne 1 ]; then
  echo "Expected exactly 1 argument: <docker image>"
  exit 1
fi

IMAGE_NAME=$1
FULL_IMAGE_NAME="localhost:5000/${IMAGE_NAME}"
sudo chmod a+rwx -R "$HOME"

imageNamePattern="scala-native-testing:(.*)"
if [[ "$IMAGE_NAME" =~ $imageNamePattern ]]; then
  arch=${BASH_REMATCH[1]}
  . ci-docker/env/${arch}
else
  echo >&2 "$IMAGE_NAME is not regular testing image name"
  exit 1
fi
# Start registry containing images built in previous CI runs
docker kill registry && docker rm registry || true
docker run -d -p 5000:5000 \
  --restart=always \
  --name registry \
  -v /tmp/docker-registry:/var/lib/registry \
  registry:2 &&
  npx wait-on tcp:5000

docker run --privileged --rm tonistiigi/binfmt --install all

# Pull cached image or build locally if image is missing
# In most cases image should exist, however in the past we have observed single
# CI jobs failing due to missing image.
if ! docker pull $FULL_IMAGE_NAME; then
  echo "Image not found found in cache, building locally"
  docker buildx build \
    -t ${IMAGE_NAME} \
    --build-arg BASE_IMAGE="$BASE_IMAGE" \
    --build-arg LLVM_VERSION="$LLVM_VERSION" \
    --build-arg IMAGE_NAME="${IMAGE_NAME}" \
    ci-docker && \
    docker tag ${IMAGE_NAME} ${FULL_IMAGE_NAME} && \
    docker push ${FULL_IMAGE_NAME}
fi

# Make sure the binded directories are present
CacheDir=$HOME/.cache
IvyDir=$HOME/.ivy
SbtDir=$HOME/.sbt
mkdir -p $CacheDir $IvyDir $SbtDir

docker run --rm \
  --mount type=bind,source=$CacheDir,target=/home/scala-native/.cache \
  --mount type=bind,source=$SbtDir,target=/home/scala-native/.sbt \
  --mount type=bind,source=$IvyDir,target=/home/scala-native/.ivy \
  --mount type=bind,source=$PWD,target=/home/scala-native/scala-native \
  -e TEST_COMMAND="$TEST_COMMAND" \
  -e SCALANATIVE_MODE="$SCALANATIVE_MODE" \
  -e SCALANATIVE_GC="$SCALANATIVE_GC" \
  -e SCALANATIVE_LTO="${SCALANATIVE_LTO:-none}" \
  -e SCALANATIVE_TEST_DEBUG_SIGNALS=1 \
  -e SCALANATIVE_TEST_PREFETCH_DEBUG_INFO=1 \
  -e GC_MAXIMUM_HEAP_SIZE=2G \
  -i "${FULL_IMAGE_NAME}"
