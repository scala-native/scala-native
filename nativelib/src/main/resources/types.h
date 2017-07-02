#ifndef __TYPES_H
#define __TYPES_H

// this is cross-patform definition of integer types
typedef signed char scalanative_int8_t;
typedef short scalanative_int16_t;
typedef int scalanative_int32_t;
typedef long long scalanative_int64_t;
typedef unsigned char scalanative_uint8_t;
typedef unsigned short scalanative_uint16_t;
typedef unsigned int scalanative_uint32_t;
typedef unsigned long long scalanative_uint64_t;

typedef scalanative_uint64_t scalanative_dev_t;
typedef scalanative_uint32_t scalanative_mode_t;
typedef scalanative_uint64_t scalanative_ino_t;
typedef scalanative_uint32_t scalanative_uid_t;
typedef scalanative_uint32_t scalanative_gid_t;
typedef scalanative_int64_t scalanative_off_t;
typedef scalanative_int64_t scalanative_time_t;
typedef scalanative_int64_t scalanative_blkcnt_t;
typedef scalanative_int64_t scalanative_blksize_t;
typedef scalanative_uint64_t scalanative_nlink_t;

typedef scalanative_uint64_t scalanative_fsblkcnt_t;
typedef scalanative_uint64_t scalanative_fsfilcnt_t;

#endif // __TYPES_H
