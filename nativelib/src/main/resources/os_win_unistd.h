#ifndef _UNISTD_H
#define _UNISTD_H 1

/* This file intended to serve as a drop-in replacement for
 *  unistd.h on Windows
 *  Please add functionality as neeeded
 */

#ifdef __cplusplus
extern "C" {
#endif

//#include <io.h>
#include <stdlib.h>
#include <stdio.h>
#include <process.h> /* for getpid() and the exec..() family */
#include <direct.h>  /* for _getcwd() and _chdir() */

#define srandom srand
#define random rand

/* Values for the second argument to access.
   These may be OR'd together.  */
#define R_OK 4 /* Test for read permission.  */
#define W_OK 2 /* Test for write permission.  */
#define X_OK 1 /* execute permission - unsupported in windows*/
#define F_OK 0 /* Test for existence.  */

#define access _access
#define dup2 _dup2
#define execve _execve
#define unlink _unlink
#define fileno _fileno
#define getcwd _getcwd
#define chdir _chdir
#define isatty _isatty
#define lseek _lseek
/* read, write, and close are NOT being #defined here, because while there are
 * file handle specific versions for Windows, they probably don't work for
 * sockets. You need to look at your app and consider whether to call e.g.
 * closesocket(). */

#define ssize_t int

#define STDIN_FILENO (0)
#define STDOUT_FILENO (1)
#define STDERR_FILENO (2)
/* should be in some equivalent to <sys/types.h> */
typedef __int8 int8_t;
typedef __int16 int16_t;
typedef __int32 int32_t;
typedef __int64 int64_t;
typedef unsigned __int8 uint8_t;
typedef unsigned __int16 uint16_t;
typedef unsigned __int32 uint32_t;
typedef unsigned __int64 uint64_t;

typedef unsigned int uid_t;
typedef unsigned int gid_t;
typedef long off_t;

int symlink(char *path1, char *path2);

int symlinkat(char *path1, int fd, char *path2);

int link(char *oldpath, char *newpath);

int linkat(int fd1, char *path1, int fd2, char *path2, int flag);

int chown(char *path, uid_t owner, gid_t group);

#ifdef __cplusplus
}
#endif

#endif /* unistd.h  */