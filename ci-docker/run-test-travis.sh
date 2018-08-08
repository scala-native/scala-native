#!/usr/bin/env bash
sudo chmod a+rwx -R $HOME;

scriptdir="$(dirname "$0")"

$scriptdir/load-cached-images.sh

$scriptdir/run-test.sh
