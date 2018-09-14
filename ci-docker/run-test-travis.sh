#!/usr/bin/env bash
sudo chmod a+rwx -R $HOME;

scriptdir="$(dirname "$0")"

$scriptdir/run-test.sh
