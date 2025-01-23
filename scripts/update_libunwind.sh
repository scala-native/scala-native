#!/usr/bin/env zsh

REV=$1
SCALANATIVE_FOLDER=$(pwd)
LLVM_FOLDER=$SCALANATIVE_FOLDER/llvm
LIBUNWIND_FOLDER=$LLVM_FOLDER/libunwind
TARGET_FOLDER=$SCALANATIVE_FOLDER/nativelib/src/main/resources/scala-native/platform/posix/libunwind

rm -rf "$TARGET_FOLDER"
mkdir -p "$TARGET_FOLDER"

git clone -b $REV --depth 1 https://github.com/llvm/llvm-project.git $LLVM_FOLDER

cp -r "$LIBUNWIND_FOLDER/src/"* "$TARGET_FOLDER/"
cp -r "$LIBUNWIND_FOLDER/include/"* "$TARGET_FOLDER/"

# Enable recursive globbing for zsh
setopt extended_glob

for source in $TARGET_FOLDER/**/*.(c|cpp|h|hpp|S)(.N); do
  sed -i '' '1i\
// clang-format off\
#if defined(__unix__) || defined(__unix) || defined(unix) || \\\
    (defined(__APPLE__) && defined(__MACH__))
' "$source"
  echo "#endif" >> "$source"

  sed -i '' 's|<mach-o/dyld.h>|"mach-o/dyld.h"|g' "$source"
  sed -i '' 's|<mach-o/compact_unwind_encoding.h>|"mach-o/compact_unwind_encoding.h"|g' "$source"
  sed -i '' 's|<__libunwind_config.h>|"__libunwind_config.h"|g' "$source"
  sed -i '' 's|<libunwind.h>|"libunwind.h"|g' "$source"
  sed -i '' 's|<unwind.h>|"unwind.h"|g' "$source"
  sed -i '' 's|<unwind_itanium.h>|"unwind_itanium.h"|g' "$source"
  sed -i '' 's|<unwind_arm_ehabi.h>|"unwind_arm_ehabi.h"|g' "$source"
  
done

echo "$REV" > "$TARGET_FOLDER/rev.txt"

echo "Please remove the temporary LLVM clone at $LLVM_FOLDER"
