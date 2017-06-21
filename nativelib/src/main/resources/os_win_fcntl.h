#ifndef OS_WIN_FCNTL_H
#define OS_WIN_FCNTL_H 1

#ifdef __cplusplus
extern "C" {
#endif

#include <stdarg.h>
#include "os_win_types.h"

#define O_RDONLY 0x0000 // open for reading only
#define O_WRONLY 0x0001 // open for writing only
#define O_RDWR 0x0002   // open for reading and writing
#define O_APPEND 0x0008 // writes done at eof

#define O_CREAT 0x0100 // create and open file
#define O_TRUNC 0x0200 // open and truncate
#define O_EXCL 0x0400  // open only if file doesn't already exist

// O_TEXT files have <cr><lf> sequences translated to <lf> on read()'s and <lf>
// sequences translated to <cr><lf> on write()'s

#define O_TEXT 0x4000     // file mode is text (translated)
#define O_BINARY 0x8000   // file mode is binary (untranslated)
#define O_WTEXT 0x10000   // file mode is UTF16 (translated)
#define O_U16TEXT 0x20000 // file mode is UTF16 no BOM (translated)
#define O_U8TEXT 0x40000  // file mode is UTF8  no BOM (translated)

// macro to translate the C 2.0 name used to force binary mode for files
#define O_RAW _O_BINARY

#define O_NOINHERIT 0x0080 // child process doesn't inherit file
#define O_TEMPORARY                                                            \
    0x0040 // temporary file bit (file is deleted when last handle is closed)
#define O_SHORT_LIVED 0x1000 // temporary storage file, try not to flush
#define O_OBTAIN_DIR 0x2000  // get information about a directory
#define O_SEQUENTIAL 0x0020  // file access is primarily sequential
#define O_RANDOM 0x0010      // file access is primarily random

#define F_DUPFD 0  /* duplicate file descriptor */
#define F_GETFD 1  /* get file descriptor flags */
#define F_SETFD 2  /* set file descriptor flags */
#define F_GETFL 3  /* get file status flags */
#define F_SETFL 4  /* set file status flags */
#define F_GETOWN 5 /* get SIGIO/SIGURG proc/pgrp */
#define F_SETOWN 6 /* set SIGIO/SIGURG proc/pgrp */
#define F_GETLK 7  /* get record locking information */
#define F_SETLK 8  /* set record locking information */
#define F_SETLKW 9 /* F_SETLK; wait if blocked */

int os_win_fcntl_open(const char *pathname, int flags, mode_t mode);
int os_win_fcntl_close(int fd);
int os_win_fcntl_fcntl(int fd, int cmd, va_list args);

#ifdef __cplusplus
}
#endif

#endif /* os_win_fcntl.h  */