#!/bin/bash

set -e

scripts/clangfmt --test

scripts/scalafmt --test
