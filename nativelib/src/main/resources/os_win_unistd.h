#ifndef _UNISTD_H
#define _UNISTD_H 1

#ifdef __cplusplus
extern "C" {
#endif

#include "os_win_types.h"

#include <io.h>
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

/*#define access _access
#define dup2 _dup2
#define execve _execve
#define unlink _unlink
#define fileno _fileno
#define getcwd _getcwd
#define chdir _chdir
#define isatty _isatty
#define lseek _lseek*/
/* read, write, and close are NOT being #defined here, because while there are
 * file handle specific versions for Windows, they probably don't work for
 * sockets. You need to look at your app and consider whether to call e.g.
 * closesocket(). */

#define STDIN_FILENO (0)
#define STDOUT_FILENO (1)
#define STDERR_FILENO (2)

int os_win_unistd_symlink(char *path1, char *path2);

int os_win_unistd_symlinkat(char *path1, int fd, char *path2);

int os_win_unistd_link(char *oldpath, char *newpath);

int os_win_unistd_linkat(int fd1, char *path1, int fd2, char *path2, int flag);

int os_win_unistd_chown(char *path, uid_t owner, gid_t group);

int os_win_unistd_access(const char *path, int amode);

int os_win_unistd_sleep(uint32_t seconds);

int os_win_unistd_usleep(uint32_t usecs);

int os_win_unistd_unlink(const char *path);

int os_win_unistd_readlink(const char *path, const char *buf, size_t bufsize);

const char *os_win_unistd_getcwd(char *buf, size_t size);

int os_win_unistd_write(int fildes, void *buf, size_t nbyte);

int os_win_unistd_read(int fildes, void *buf, size_t nbyte);

int os_win_unistd_close(int fildes);

int os_win_unistd_fsync(int fildes);

scalanative_off_t os_win_unistd_lseek(int fildes, scalanative_off_t offset,
                                      int whence);

int os_win_unistd_ftruncate(int fildes, scalanative_off_t length);

int os_win_unistd_truncate(const char *path, scalanative_off_t length);

int __imp_close(int fildes);
int __imp_open(const char *pathname, int flags, mode_t mode);

#ifdef __cplusplus
}
#endif

#endif /* unistd.h  */