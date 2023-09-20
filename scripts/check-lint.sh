#!/usr/bin/env bash

set -e

scripts/clangfmt --test

scripts/scalafmt --test
