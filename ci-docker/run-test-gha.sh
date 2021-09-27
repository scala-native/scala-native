#!/usr/bin/env bash
set -e
set -x

if [ $# -eq 0 ]
  then echo "Expected single argument with docker image version"
  exit 1
fi

IMAGE_NAME=$1
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
  imageNamePattern="scala-native-testing:linux-(.*)"

  if [[ "$IMAGE_NAME" =~ $imageNamePattern ]];then
    arch=${BASH_REMATCH[1]}

    docker build \
    -t ${FULL_IMAGE_NAME} \
    --build-arg TARGET_DOCKER_PLATFORM=library \
    --build-arg HOST_ARCHITECTURE=${arch}  \
    --cpuset-cpus=0 \
    ci-docker
  else
    >&2 echo "$IMAGE_NAME is not regular testing image name"
    exit 1
  fi
fi

docker run -i "${FULL_IMAGE_NAME}" java -version
docker run --mount type=bind,source=$HOME/.cache/coursier,target=/home/scala-native/.cache/coursier \
           --mount type=bind,source=$HOME/.sbt,target=/home/scala-native/.sbt \
           --mount type=bind,source=$PWD,target=/home/scala-native/scala-native \
           -e SCALANATIVE_MODE="$SCALANATIVE_MODE" \
           -e SCALANATIVE_GC="$SCALANATIVE_GC" \
           -e SCALANATIVE_OPTIMIZE="$SCALANATIVE_OPTIMIZE" \
           -e SCALANATIVE_LTO="${SCALANATIVE_LTO:-none}" \
           -e TEST_COMMAND="$TEST_COMMAND" \
           -e SCALA_VERSION="$SCALA_VERSION" \
           -i "${FULL_IMAGE_NAME}"
