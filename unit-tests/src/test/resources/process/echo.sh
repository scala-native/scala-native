#!/bin/bash

while IFS='$\n' read -r line || true; do
  if [[ $line == "quit" ]]; then
    exit 0
  fi
  printf $line
done
