#ifdef _WIN32
#pragma once

#include "os_win_types.h"

#define PROT_READ 0x1  /* Page can be read.  */
#define PROT_WRITE 0x2 /* Page can be written.  */
#define PROT_EXEC 0x4  /* Page can be executed.  */
#define PROT_NONE 0x0  /* Page can not be accessed.  */

/*
 * Flags contain sharing type and options.
 * Sharing types; choose one.
 */
#define MAP_SHARED 0x0001  /* share changes */
#define MAP_PRIVATE 0x0002 /* changes are private */
#define MAP_COPY 0x0004    /* "copy" region at mmap time */

#define MAP_FIXED 0x0010        /* map addr must be exactly as requested */
#define MAP_RENAME 0x0020       /* Sun: rename private pages to file */
#define MAP_NORESERVE 0x0040    /* Sun: don't reserve needed swap area */
#define MAP_INHERIT 0x0080      /* region is retained after exec */
#define MAP_NOEXTEND 0x0100     /* for MAP_FILE, don't change file size */
#define MAP_HASSEMAPHORE 0x0200 /* region may contain semaphores */

/*
 * Mapping type; default is map from file.
 */
#define MAP_ANONYMOUS 0x1000 /* allocated from memory, swap space */

#ifdef __cplusplus
extern "C" {
#endif

int mprotect(void *addr, size_t len, int prot);
void *mmap(void *addr, size_t length, int prot, int flags, int fd,
           scalanative_off_t offset);
int munmap(void *addr, size_t length);

#ifdef __cplusplus
}
#endif

#endif