#!/usr/bin/env bash
set -e

sudo chmod a+rwx -R $HOME;

ci-docker/run-test.sh
