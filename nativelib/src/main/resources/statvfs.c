#include <sys/statvfs.h>
#include "types.h"
#include "gc.h"

struct scalanative_statvfs {
    unsigned long f_bsize;           /** File system block size. */
    unsigned long f_frsize;          /** Fundamental file system block size. */
    scalanative_fsblkcnt_t f_blocks; /** Total number of blocks on file system in units of f_frsize. */
    scalanative_fsblkcnt_t f_bfree;  /** Total number of free blocks. */
    scalanative_fsblkcnt_t f_bavail; /** Number of free blocks available to
                                         non-privileged process. */
    scalanative_fsfilcnt_t f_files;  /** Total number of file serial numbers. */
    scalanative_fsfilcnt_t f_ffree;  /** Total number of free file serial numbers. */
    scalanative_fsfilcnt_t f_favail; /** Number of file serial numbers available to
                                         non-privileged process. */
    unsigned long f_fsid;            /** File system ID. */
    unsigned long f_flag;            /** Bit mask of f_flag values. */
    unsigned long f_namemax;         /** Maximum filename length. */
};

void scalanative_statvfs_init(struct statvfs *statvfs, struct scalanative_statvfs *my_statvfs) {
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

struct scalanative_statvfs *scalanative_statvfs_copy(struct statvfs *statvfs) {
    struct scalanative_statvfs *my_statvfs =
        (struct scalanative_statvfs *) GC_malloc(sizeof(struct scalanative_statvfs));
    scalanative_statvfs_init(statvfs, my_statvfs);
    return my_statvfs;
}

struct scalanative_statvfs *scalanative_statvfs(char *path) {
    struct statvfs buf;
    if (statvfs(path, &buf) == 0) {
        return scalanative_statvfs_copy(&buf);
    } else {
        return NULL;
    }
}

struct scalanative_statvfs *scalanative_fstatvfs(int fd) {
    struct statvfs buf;
    if (fstatvfs(fd, &buf) == 0) {
        return scalanative_statvfs_copy(&buf);
    } else {
        return NULL;
    }
}

unsigned long scalanative_st_rdonly() {
    return ST_RDONLY;
}

unsigned long scalanative_st_nosuid() {
    return ST_NOSUID;
}
