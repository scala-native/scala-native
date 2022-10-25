#!/usr/bin/env bash
set -e
set -x

if [ $# -ne 2 ]; then
  echo "Expected exactly 3 arguments: <docker image> <scala version>"
  exit 1
fi

IMAGE_NAME=$1
SCALA_VERSION=$2
FULL_IMAGE_NAME="localhost:5000/${IMAGE_NAME}"
sudo chmod a+rwx -R "$HOME"

# Start registry containing images built in previous CI steps
docker run -d -p 5000:5000 \
  --restart=always \
  --name registry \
  -v /tmp/docker-registry:/var/lib/registry \
  registry:2 &&
  npx wait-on tcp:5000

# Pull cached image or build locally if image is missing
# In most cases image should exist, however in the past we have observed single
# CI jobs failing due to missing image.
if ! docker pull $FULL_IMAGE_NAME; then
  echo "Image not found found in cache, building locally"
  imageNamePattern="scala-native-testing:(.*)"

  if [[ "$IMAGE_NAME" =~ $imageNamePattern ]]; then
    arch=${BASH_REMATCH[1]}
    . ci-docker/env/${arch}

    docker build \
      -t ${FULL_IMAGE_NAME} \
      --build-arg BASE_IMAGE=$BASE_IMAGE \
      --build-arg LLVM_VERSION=$LLVM_VERSION \
      ci-docker &&
      docker tag ${FULL_IMAGE_NAME} localhost:5000/${FULL_IMAGE_NAME} &&
      docker push localhost:5000/${FULL_IMAGE_NAME}
  else
    echo >&2 "$IMAGE_NAME is not regular testing image name"
    exit 1
  fi
fi

# Make sure the binded directories are present
CacheDir=$HOME/.cache
IvyDir=$HOME/.ivy
SbtDir=$HOME/.sbt
mkdir -p $CacheDir $IvyDir $SbtDir

docker run -i "${FULL_IMAGE_NAME}" --platform=linux/amd64 bash -c "java -version"
docker run --mount type=bind,source=$CacheDir,target=/home/scala-native/.cache \
  --mount type=bind,source=$SbtDir,target=/home/scala-native/.sbt \
  --mount type=bind,source=$IvyDir,target=/home/scala-native/.ivy \
  --mount type=bind,source=$PWD,target=/home/scala-native/scala-native \
  --platform=linux/amd64 \
  -e SCALA_VERSION="$SCALA_VERSION" \
  -e TARGET_EMULATOR="${TARGET_EMULATOR}" \
  -e TEST_COMMAND="$TEST_COMMAND" \
  -i "${FULL_IMAGE_NAME}"
