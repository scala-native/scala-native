#!/usr/bin/env bash

set -e

scripts/gyb.py nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb  --line-directive "" -o nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala
scripts/gyb.py nativelib/src/main/scala/scala/scalanative/unsafe/Size.scala.gyb  --line-directive "" -o nativelib/src/main/scala/scala/scalanative/unsafe/Size.scala
scripts/gyb.py nativelib/src/main/scala/scala/scalanative/unsafe/Nat.scala.gyb  --line-directive "" -o nativelib/src/main/scala/scala/scalanative/unsafe/Nat.scala
scripts/gyb.py nativelib/src/main/scala/scala/scalanative/unsafe/CStruct.scala.gyb  --line-directive "" -o nativelib/src/main/scala/scala/scalanative/unsafe/CStruct.scala
scripts/gyb.py nativelib/src/main/scala/scala/scalanative/unsafe/CFuncPtr.scala.gyb  --line-directive "" -o nativelib/src/main/scala/scala/scalanative/unsafe/CFuncPtr.scala

scripts/gyb.py nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb  --line-directive "" -o nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala
scripts/gyb.py nativelib/src/main/scala/scala/scalanative/runtime/Boxes.scala.gyb  --line-directive "" -o nativelib/src/main/scala/scala/scalanative/runtime/Boxes.scala
scripts/gyb.py nativelib/src/main/scala/scala/scalanative/runtime/Primitives.scala.gyb  --line-directive "" -o nativelib/src/main/scala/scala/scalanative/runtime/Primitives.scala

scripts/gyb.py nativelib/src/main/scala/scala/scalanative/unsigned/USize.scala.gyb  --line-directive "" -o nativelib/src/main/scala/scala/scalanative/unsigned/USize.scala

scripts/scalafmt \
  nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala \
  nativelib/src/main/scala/scala/scalanative/unsafe/Size.scala \
  nativelib/src/main/scala/scala/scalanative/unsafe/Nat.scala \
  nativelib/src/main/scala/scala/scalanative/unsafe/CStruct.scala \
  nativelib/src/main/scala/scala/scalanative/unsafe/CFuncPtr.scala \
  nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala \
  nativelib/src/main/scala/scala/scalanative/runtime/Boxes.scala \
  nativelib/src/main/scala/scala/scalanative/runtime/Primitives.scala \
  nativelib/src/main/scala/scala/scalanative/unsigned/USize.scala
