#ifndef _OS_WIN_TYPES_H_
#define _OS_WIN_TYPES_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <inttypes.h>

typedef unsigned int uid_t;
typedef unsigned int gid_t;
typedef long off_t;
typedef int mode_t;

typedef int ssize_t;

typedef unsigned short in_port_t;
typedef unsigned int in_addr_t;

#ifdef __cplusplus
}
#endif

#endif