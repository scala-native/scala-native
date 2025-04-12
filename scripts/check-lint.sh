#!/usr/bin/env bash

set -e

scripts/clangfmt --test

scala-cli format --check
