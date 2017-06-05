# Windows version of coursier

$COURSIER="$PSScriptRoot\.coursier.jar"

$url = "https://github.com/coursier/coursier/raw/master/coursier"

Invoke-WebRequest -Uri $url -OutFile $COURSIER

&java -jar $COURSIER $args