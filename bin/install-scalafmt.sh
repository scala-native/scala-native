#!/usr/bin/env bash

if [ ! -f coursier ]; then
  curl -L -o coursier https://git.io/vgvpD
  chmod +x coursier
fi

./coursier bootstrap com.geirsson:scalafmt-cli_2.11:0.4.7 --main org.scalafmt.cli.Cli -f -o scalafmt
