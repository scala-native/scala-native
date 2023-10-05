#!/usr/bin/env bash

set -e

nativelib=nativelib/src/main
scala=scala
scalaNext=scala-next
unsafe=scala/scalanative/unsafe
unsigned=scala/scalanative/unsigned
runtime=scala/scalanative/runtime
javaNIO=javalib/src/main/scala/java/nio/

function gyb() {
  file=$1
  if [ ${file: -4} == ".gyb" ]; then
    outputFile="${file%.gyb}"
    echo "Generate $outputFile"
    scripts/gyb.py --line-directive '' -o "$outputFile" "$file"
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

gyb clib/src/main/scala/scala/scalanative/libc/atomic.scala.gyb
gyb clib/src/main/resources/scala-native/atomic.c.gyb

gyb $javaNIO/HeapBuffers.scala.gyb
gyb $javaNIO/HeapByteBufferViews.scala.gyb
gyb $javaNIO/MappedByteBufferViews.scala.gyb
gyb $javaNIO/PointerByteBufferViews.scala.gyb


