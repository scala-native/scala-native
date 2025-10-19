#!/usr/bin/env bash

set -e

scripts/clangfmt --test

## Provisioning and version control
##
## Script down loads the scalafmt version built using Scala Native specified
## in <projectRoot>.scalafmt.conf.
##
## --check will quit after first format failure.

scripts/scalafmt --check
# Native hangs...
# scripts/scalafmt-native --check
