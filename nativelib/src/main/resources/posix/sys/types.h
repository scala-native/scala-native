#ifndef __TYPES_H
#define __TYPES_H

// nativelib resources/types.h &
// nativelib scalanative/posix/sys/types.scala should/MUST be kept
// in sync. 
//
// Someday, one file will be generated from the other. For now, the
// synchronization is manual & error prone.  If you  make a change to
// this file, please consider if the change is applicable to the other
// file.  Future developers will sing you praises if you document the
// existence and reason for designed, desired inconsistencies.

#if defined(__LP64__) || defined(_LP64)
// long long is probably unnecessary in this block, but that is what
// git mainline 0.3.7 used. Do not disturb.
typedef long long scalanative_blkcnt_t;
typedef unsigned long scalanative_dev_t;
// Comments at top of ./stat.c seem to indicate that "long long" here is
// an OSX thing.
typedef unsigned long long scalanative_ino_t;
#elif defined(__ILP32__) || defined(_ILP32)
#if defined(__i386__) || defined(__i386)
// Large File Support is OFF in types.scala & other places, so blkcnt_t
// fsblkck_t, ino_t, and, especially, off_t are all 32 bits.
// Keep blkcnt_t consistent with other non-LFS, no expensive 64 bit math.
typedef long scalanative_blkcnt_t;
// Linux uses 64 bit dev_t, even on __i386__.
typedef unsigned long long scalanative_dev_t;
typedef unsigned long scalanative_ino_t;
#else
#error("types.h has not been verified on this 32 bit architecture.")
#endif
#else
#error("types.h has not been verified on this ILP data model.")
#endif

typedef long scalanative_blksize_t;
typedef unsigned long scalanative_fsblkcnt_t;
typedef unsigned long scalanative_fsfilcnt_t;
typedef unsigned int scalanative_gid_t;
typedef unsigned int scalanative_mode_t;
typedef unsigned long scalanative_nlink_t;
typedef long scalanative_off_t;
typedef long int scalanative_time_t;
typedef unsigned int scalanative_uid_t;

#endif // __TYPES_H
