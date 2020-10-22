#!/bin/bash

set -e

if [ -n "$CLANG_FORMAT_PATH" ]; then
  scripts/clangfmt --test
fi

scripts/scalafmt --test
