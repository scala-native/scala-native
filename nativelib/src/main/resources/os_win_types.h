#ifndef _OS_WIN_TYPES_H_
#define _OS_WIN_TYPES_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <inttypes.h>
#include "types.h"

typedef unsigned int uid_t;
typedef unsigned int gid_t;
typedef unsigned int pid_t;
typedef long long scalanative_off_t;
typedef int mode_t;

typedef long long ssize_t;

typedef unsigned short in_port_t;
typedef unsigned int in_addr_t;
typedef unsigned long long in_addr6_t;

typedef long long blkcnt_t;
typedef long long blksize_t;

#ifdef __cplusplus
}
#endif

#endif