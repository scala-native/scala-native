#include <stdlib.h>
#include <sys/statvfs.h>
#include "types.h"

struct scalanative_statvfs {
    unsigned long f_bsize;           /** File system block size. */
    unsigned long f_frsize;          /** Fundamental file system block size. */
    scalanative_fsblkcnt_t f_blocks; /** Total number of blocks on file system
                                        in units of f_frsize. */
    scalanative_fsblkcnt_t f_bfree;  /** Total number of free blocks. */
    scalanative_fsblkcnt_t f_bavail; /** Number of free blocks available to
                                         non-privileged process. */
    scalanative_fsfilcnt_t f_files;  /** Total number of file serial numbers. */
    scalanative_fsfilcnt_t
        f_ffree; /** Total number of free file serial numbers. */
    scalanative_fsfilcnt_t
        f_favail;            /** Number of file serial numbers available to
                                 non-privileged process. */
    unsigned long f_fsid;    /** File system ID. */
    unsigned long f_flag;    /** Bit mask of f_flag values. */
    unsigned long f_namemax; /** Maximum filename length. */
};

void scalanative_statvfs_copy(struct statvfs *statvfs,
                              struct scalanative_statvfs *my_statvfs) {
    my_statvfs->f_bsize = statvfs->f_bsize;
    my_statvfs->f_frsize = statvfs->f_frsize;
    my_statvfs->f_bavail = statvfs->f_bavail;
    my_statvfs->f_files = statvfs->f_files;
    my_statvfs->f_ffree = statvfs->f_ffree;
    my_statvfs->f_favail = statvfs->f_favail;
    my_statvfs->f_fsid = statvfs->f_fsid;
    my_statvfs->f_flag = statvfs->f_flag;
    my_statvfs->f_namemax = statvfs->f_namemax;
}

int scalanative_statvfs(char *path, struct scalanative_statvfs *buf) {
    struct statvfs statvfs_buf;
    int err = statvfs(path, &statvfs_buf);
    if (err == 0) {
        scalanative_statvfs_copy(&statvfs_buf, buf);
        return 0;
    } else {
        return err;
    }
}

int scalanative_fstatvfs(int fd, struct scalanative_statvfs *buf) {
    struct statvfs statvfs_buf;
    int err = fstatvfs(fd, &statvfs_buf);
    if (err == 0) {
        scalanative_statvfs_copy(&statvfs_buf, buf);
        return 0;
    } else {
        return err;
    }
}

unsigned long scalanative_st_rdonly() { return ST_RDONLY; }

unsigned long scalanative_st_nosuid() { return ST_NOSUID; }
