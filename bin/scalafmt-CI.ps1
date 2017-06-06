# Test formatting Scala code using scalafmt.

$SCALAFMT_VERSION="0.6.8"
$SCALAFMTTEST="$PSScriptRoot\scalafmt-CI-$SCALAFMT_VERSION.jar"

&java -jar $SCALAFMTTEST --test