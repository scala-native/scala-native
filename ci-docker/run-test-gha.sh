#!/usr/bin/env bash
set -e
set -x

if [ $# -ne 3 ]
  then echo "Expected exactly 3 arguments: <docker image> <scala version> <emulator>"
  exit 1
fi

IMAGE_NAME=$1
SCALA_VERSION=$2
TARGET_EMULATOR=$3
FULL_IMAGE_NAME="localhost:5000/${IMAGE_NAME}"
sudo chmod a+rwx -R "$HOME"

# Start registry containing images built in previous CI steps
docker run -d -p 5000:5000 \
  --restart=always \
  --name registry \
  -v /tmp/docker-registry:/var/lib/registry \
  registry:2 && \
  npx wait-on tcp:5000

# Pull cached image or build locally if image is missing
# In most cases image should exist, however in the past we have observed single
# CI jobs failing due to missing image.
if ! docker pull $FULL_IMAGE_NAME;then
  echo "Image not found found in cache, building locally"
  imageNamePattern="scala-native-testing:(.*)"

  if [[ "$IMAGE_NAME" =~ $imageNamePattern ]];then
    arch=${BASH_REMATCH[1]}

    docker build \
    -t ${FULL_IMAGE_NAME} \
    --build-arg TARGET_PLATFORM=${arch} \
    ci-docker \
    && docker tag ${FULL_IMAGE_NAME} localhost:5000/${FULL_IMAGE_NAME} \
    && docker push localhost:5000/${FULL_IMAGE_NAME}
  else
    >&2 echo "$IMAGE_NAME is not regular testing image name"
    exit 1
  fi
fi

docker run -i "${FULL_IMAGE_NAME}" java -version
docker run --mount type=bind,source=$HOME/.cache,target=/home/scala-native/.cache \
           --mount type=bind,source=$HOME/.sbt,target=/home/scala-native/.sbt \
           --mount type=bind,source=$PWD,target=/home/scala-native/scala-native \
           -e SCALA_VERSION="$SCALA_VERSION" \
           -e TARGET_EMULATOR="${TARGET_EMULATOR}" \
           -e TEST_COMMAND="$TEST_COMMAND" \
           -i "${FULL_IMAGE_NAME}"
