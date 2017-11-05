#ifndef _OS_WIN_STATVFS_H_
#define _OS_WIN_STATVFS_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef unsigned long fsblkcnt_t;
typedef unsigned long fsfilcnt_t;

struct statvfs {
    unsigned long f_bsize;   /** File system block size. */
    unsigned long f_frsize;  /** Fundamental file system block size. */
    fsblkcnt_t f_blocks;     /** Total number of blocks on file system
                                            in units of f_frsize. */
    fsblkcnt_t f_bfree;      /** Total number of free blocks. */
    fsblkcnt_t f_bavail;     /** Number of free blocks available to
                                             non-privileged process. */
    fsfilcnt_t f_files;      /** Total number of file serial numbers. */
    fsfilcnt_t f_ffree;      /** Total number of free file serial numbers. */
    fsfilcnt_t f_favail;     /** Number of file serial numbers available to
                                 non-privileged process. */
    unsigned long f_fsid;    /** File system ID. */
    unsigned long f_flag;    /** Bit mask of f_flag values. */
    unsigned long f_namemax; /** Maximum filename length. */
};

int statvfs(const char *path, struct statvfs *buf);
int fstatvfs(int fd, struct statvfs *buf);

#define ST_RDONLY 0x0001 /* read-only */
#define ST_NOSUID 0x0002 /* ignore suid and sgid bits */

#ifdef __cplusplus
}
#endif

#endif