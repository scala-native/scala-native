#!/usr/bin/env bash

shopt -s globstar

REV=$1
LLVM_FOLDER=$(mktemp -d -t libunwind-clone-XXXXXXX)
SCALANATIVE_FOLDER=$(pwd)

TARGET_FOLDER=nativelib/src/main/resources/scala-native/platform/posix/libunwind

rm -rf $TARGET_FOLDER

mkdir $TARGET_FOLDER

cd $LLVM_FOLDER
git clone -b $REV --depth 1 https://github.com/llvm/llvm-project.git .

cd $SCALANATIVE_FOLDER

LIBUNWIND_FOLDER=$LLVM_FOLDER/libunwind

cp -r $LIBUNWIND_FOLDER/src/* $TARGET_FOLDER/
cp -r $LIBUNWIND_FOLDER/include/* $TARGET_FOLDER/

# rm -rf $LIBUNWIND_FOLDER

nl='
'

for source in $TARGET_FOLDER/**/*.{c,cpp,h,hpp,S}; do
  sed -i '1i\// clang-format off'"\\${nl}"'#if defined(__unix__) || defined(__unix) || defined(unix) || \\'"\\${nl}"'    (defined(__APPLE__) && defined(__MACH__))' "$source"
  echo "#endif" >> "$source"

  sed -i 's/<mach-o\/dyld.h>/"mach-o\/dyld.h"/g' "$source"
  sed -i 's/<mach-o\/compact_unwind_encoding.h>/"mach-o\/compact_unwind_encoding.h"/g' "$source"
  sed -i 's/<__libunwind_config.h>/"__libunwind_config.h"/g' "$source"
  sed -i 's/<libunwind.h>/"libunwind.h"/g' "$source"
  sed -i 's/<unwind.h>/"unwind.h"/g' "$source"
done

echo $REV > $TARGET_FOLDER/rev.txt

echo "Please remove the temporary LLVM clone at $LLVM_FOLDER"
