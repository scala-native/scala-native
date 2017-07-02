#ifndef _STAT_H_
#define _STAT_H_

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>
#define _CRT_NO_TIME_T
#include <sys\types.h>
#include <sys\stat.h>
#include <direct.h>

#include "os_win_types.h"
#include "os_win_dirent.h"

struct stat {
    _dev_t st_dev;
    _ino_t st_ino;
    unsigned int st_mode;
    short st_nlink;
    short st_uid;
    short st_gid;
    _dev_t st_rdev;
    scalanative_off_t st_size;
    time_t st_atime;
    time_t st_mtime;
    time_t st_ctime;
    blkcnt_t st_blocks;
    blksize_t st_blksize;
};

#include <io.h>

#define MS_MODE_MASK (0x0000ffff)

#ifdef _USE_32BIT_TIME_T

int fstat(int const _FileHandle, struct stat *const _Stat);

int stat(char const *const _FileName, struct stat *const _Stat);

#else

int fstat(int const _FileHandle, struct stat *const _Stat);

int stat(char const *const _FileName, struct stat *const _Stat);

#endif

int lstat(char const *const _FileName, struct stat *const _Stat);

int pchmod(const char *path, mode_t mode);

int fchmod(int const _FileHandle, mode_t mode);

#endif /* !_STAT_H_ */
