#!/usr/bin/env bash
set -eux

_AUTHOR="$1"
AUTHOR=$(jq -rn --arg x "${_AUTHOR}" '$x|@uri')
echo "Pull request submitted by $AUTHOR";
signed=$(curl -L -s "https://contribute.akka.io/contribute/cla/scala/check/$AUTHOR" | jq -r ".signed");
if [ "$signed" = "true" ] ; then
  echo "CLA check for $AUTHOR successful";
else
  echo "CLA check for $AUTHOR failed";
  echo "Please sign the Scala CLA to contribute to Scala Native.";
  echo "Go to https://contribute.akka.io/contribute/cla/scala and then";
  echo "comment on the pull request to ask for a new check.";
  echo "";
  echo "Check if CLA is signed: https://contribute.akka.io/contribute/cla/scala/check/$AUTHOR";
  exit 1;
fi;
