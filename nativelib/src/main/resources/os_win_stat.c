#ifdef _WIN32
#include "os_win_stat.h"

#ifdef __cplusplus
extern "C" {
#endif

mode_t getAccessModeF(const int fileHandle);
mode_t getAccessMode(const char *path);

#ifdef _USE_32BIT_TIME_T

void copy_systemstat_to_stat(struct _stat32 *const _src,
                             struct stat *const _dst) {
    _dst->st_dev = _src->st_dev;
    _dst->st_ino = _src->st_ino;
    _dst->st_mode = _src->st_mode;
    _dst->st_nlink = _src->st_nlink;
    _dst->st_uid = _src->st_uid;
    _dst->st_gid = _src->st_gid;
    _dst->st_rdev = _src->st_rdev;
    _dst->st_size = _src->st_size;
    _dst->st_atime = _src->st_atime;
    _dst->st_mtime = _src->st_mtime;
    _dst->st_ctime = _src->st_ctime;
    _dst->st_blocks = 0;
    _dst->st_blksize = 0;
}

int fstat(int const _FileHandle, struct stat *const _Stat) {
    struct _stat32 temp;
    int result = _fstat32(_FileHandle, &temp);
    copy_systemstat_to_stat(&temp, _Stat);
    _Stat->st_mode = getAccessModeF(_FileHandle);
    return result;
}

int stat(char const *const _FileName, struct stat *const _Stat) {
    struct _stat32 temp;
    int result = _stat32(_FileName, &temp);
    copy_systemstat_to_stat(&temp, _Stat);
    _Stat->st_mode = getAccessMode(_FileName);
    return result;
}

#else

void copy_systemstat_to_stat(struct _stat64i32 *const _src,
                             struct stat *const _dst) {
    _dst->st_dev = _src->st_dev;
    _dst->st_ino = _src->st_ino;
    _dst->st_mode = _src->st_mode;
    _dst->st_nlink = _src->st_nlink;
    _dst->st_uid = _src->st_uid;
    _dst->st_gid = _src->st_gid;
    _dst->st_rdev = _src->st_rdev;
    _dst->st_size = _src->st_size;
    _dst->st_atime = _src->st_atime;
    _dst->st_mtime = _src->st_mtime;
    _dst->st_ctime = _src->st_ctime;
    _dst->st_blocks = 0;
    _dst->st_blksize = 0;
}

int fstat(int const _FileHandle, struct stat *const _Stat) {
    struct _stat64i32 temp;
    int result = _fstat64i32(_FileHandle, &temp);
    copy_systemstat_to_stat(&temp, _Stat);
    _Stat->st_mode = getAccessModeF(_FileHandle);
    return result;
}

int stat(char const *const _FileName, struct stat *const _Stat) {
    struct _stat64i32 temp;
    int result = _stat64i32(_FileName, &temp);
    copy_systemstat_to_stat(&temp, _Stat);
    _Stat->st_mode = getAccessMode(_FileName);
    return result;
}

#endif

int lstat(char const *const _FileName, struct stat *const _Stat) {
    // todo: make check for symbolic link
    return stat(_FileName, _Stat);
}

int fchmod(int const _FileHandle, mode_t mode) {
    HANDLE hFile = (HANDLE)(uintptr_t)(_FileHandle);
    char existingTarget[MAX_PATH];
    if (INVALID_HANDLE_VALUE != hFile) {
        GetFinalPathNameByHandleA(hFile, existingTarget, MAX_PATH,
                                  FILE_NAME_OPENED);
    }

    return pchmod(existingTarget, mode);
}

#ifdef __cplusplus
}
#endif
#endif