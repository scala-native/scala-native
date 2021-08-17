#!/usr/bin/env bash

shopt -s globstar

LIBUNWIND_FOLDER=$1

TARGET_FOLDER=nativelib/src/main/resources/scala-native/platform/posix/libunwind

rm -rf $TARGET_FOLDER

mkdir $TARGET_FOLDER

cp -r $LIBUNWIND_FOLDER/src/* $TARGET_FOLDER/
cp -r $LIBUNWIND_FOLDER/include/* $TARGET_FOLDER/

nl='
'

for source in $TARGET_FOLDER/{**,.}/*.{c,cpp,h,hpp}; do
  sed -i '1i\// clang-format off'"\\${nl}"'#if defined(__unix__) || defined(__unix) || defined(unix) || \\'"\\${nl}"'    (defined(__APPLE__) && defined(__MACH__))' "$source"
  echo "#endif" >> "$source"

  sed -i 's/<mach-o\/dyld.h>/"mach-o\/dyld.h"/g' "$source"
  sed -i 's/<mach-o\/compact_unwind_encoding.h>/"mach-o\/compact_unwind_encoding.h"/g' "$source"
  sed -i 's/<__libunwind_config.h>/"__libunwind_config.h"/g' "$source"
  sed -i 's/<libunwind.h>/"libunwind.h"/g' "$source"
  sed -i 's/<unwind.h>/"unwind.h"/g' "$source"
done
