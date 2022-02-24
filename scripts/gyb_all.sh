#!/usr/bin/env bash

set -e

scalanativeUnsafe=nativelib/src/main/scala/scala/scalanative/unsafe
scalanativeRuntime=nativelib/src/main/scala/scala/scalanative/runtime

function gyb {
  file=$1
  if [ ${file: -4} == ".gyb" ]; then
    scripts/gyb.py --line-directive '' -o "${file%.gyb}" "$file"
  else 
    echo "$file is not a .gyb file"
    exit 1
  fi
}

gyb $scalanativeUnsafe/Tag.scala.gyb
gyb $scalanativeUnsafe/Nat.scala.gyb
gyb $scalanativeUnsafe/CStruct.scala.gyb
gyb $scalanativeUnsafe/CFuncPtr.scala.gyb

gyb $scalanativeRuntime/Arrays.scala.gyb
gyb $scalanativeRuntime/Boxes.scala.gyb
gyb $scalanativeRuntime/Primitives.scala.gyb
