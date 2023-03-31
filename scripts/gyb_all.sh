#!/usr/bin/env bash

set -e

nativelib=nativelib/src/main
scala=scala
scalaNext=scala-next
unsafe=scala/scalanative/unsafe
unsigned=scala/scalanative/unsigned
runtime=scala/scalanative/runtime

function gyb {
  file=$1
  if [ ${file: -4} == ".gyb" ]; then
    scripts/gyb.py --line-directive '' -o "${file%.gyb}" "$file"
  else 
    echo "$file is not a .gyb file"
    exit 1
  fi
}

gyb_files() {
  local lib="$1"
  local scalaVersion="$2"
  local package="$3"
  shift 3
  for name in "$@"; do
      gyb "$lib/$scalaVersion/$package/$name.scala.gyb"
  done
}

gyb_files $nativelib $scala $unsafe Tag Nat CStruct CFuncPtr Size
gyb_files $nativelib $scala $unsigned USize
gyb_files $nativelib $scala $runtime Arrays Boxes Primitives
gyb_files $nativelib $scalaNext $runtime ArrayExtensions