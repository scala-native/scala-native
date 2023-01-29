#!/usr/bin/env bash

set -e

scalanativeUnsafe=nativelib/src/main/scala/scala/scalanative/unsafe
scalanativeUnsigned=nativelib/src/main/scala/scala/scalanative/unsigned
scalanativeRuntime=nativelib/src/main/scala/scala/scalanative/runtime

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

gyb $scalanativeUnsafe/Tag.scala.gyb
gyb $scalanativeUnsafe/Nat.scala.gyb
gyb $scalanativeUnsafe/CStruct.scala.gyb
gyb $scalanativeUnsafe/CFuncPtr.scala.gyb
gyb $scalanativeUnsafe/Size.scala.gyb
gyb $scalanativeUnsigned/USize.scala.gyb

gyb $scalanativeRuntime/Arrays.scala.gyb
gyb $scalanativeRuntime/Boxes.scala.gyb
gyb $scalanativeRuntime/Primitives.scala.gyb

gyb clib/src/main/scala/scala/scalanative/libc/atomic.scala.gyb
gyb clib/src/main/resources/scala-native/atomic.c.gyb
