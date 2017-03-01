#include "types.h"
#include <sys/stat.h>

// We don't use the "standard" types such as `dev_t` for instance
// because these have different sizes on eg. Linux and OSX. We use the
// smallest type that can hold all the possible values for the different
// systems.
struct scalanative_stat {
  scalanative_dev_t st_dev; /** Device ID of device containing file. */
  scalanative_dev_t
      st_rdev; /** Device ID (if file is character or block special). */
  scalanative_ino_t st_ino; /** File serial number. */
  scalanative_uid_t st_uid; /** User ID of file. */
  scalanative_gid_t st_gid; /** Group ID of file. */
  scalanative_off_t
      st_size;                  /** For regular files, the file size in bytes.
                                    For symbolic links, the length in bytes of the
                                    pathname contained in the symbolic link.
                                    For a shared memory object, the length in bytes.
                                    For a typed memory object, the length in bytes.
                                    For other file types, the use of this field is
                                    unspecified. */
  scalanative_time_t _st_atime; /** Time of last access. */
  scalanative_time_t _st_mtime; /** Time of last data modification. */
  scalanative_time_t _st_ctime; /** Time of last status change. */
  scalanative_blkcnt_t
      st_blocks; /** Number of blocks allocated for this object. */
  scalanative_blksize_t
      st_blksize; /** A file system-specific preferred I/O block size for
                      this object. In some file system types, this may
                      vary from file to file. */
  scalanative_nlink_t st_nlink; /** Number of hard links to the file. */
  scalanative_mode_t st_mode;   /** Mode of file (see below). */
};

void scalanative_stat_init(struct stat *stat,
                           struct scalanative_stat *my_stat) {
  my_stat->st_dev = stat->st_dev;
  my_stat->st_rdev = stat->st_rdev;
  my_stat->st_ino = stat->st_ino;
  my_stat->st_uid = stat->st_uid;
  my_stat->st_gid = stat->st_gid;
  my_stat->st_size = stat->st_size;
  my_stat->_st_atime = stat->st_atime;
  my_stat->_st_mtime = stat->st_mtime;
  my_stat->_st_ctime = stat->st_ctime;
  my_stat->st_blksize = stat->st_blksize;
  my_stat->st_blocks = stat->st_blocks;
  my_stat->st_nlink = stat->st_nlink;
  my_stat->st_mode = stat->st_mode;
}

int scalanative_stat(char *path, struct scalanative_stat *buf) {
  struct stat orig_buf;
  if (stat(path, &orig_buf) == 0) {
    scalanative_stat_init(&orig_buf, buf);
    return 0;
  } else {
    return 1;
  }
}

int scalanative_fstat(int fildes, struct scalanative_stat *buf) {
  struct stat orig_buf;
  if (fstat(fildes, &orig_buf) == 0) {
    scalanative_stat_init(&orig_buf, buf);
    return 0;
  } else {
    return 1;
  }
}

int scalanative_lstat(char *path, struct scalanative_stat *buf) {
  struct stat orig_buf;
  if (lstat(path, &orig_buf) == 0) {
    scalanative_stat_init(&orig_buf, buf);
    return 0;
  } else {
    return 1;
  }
}

int scalanative_mkdir(char *path, mode_t mode) { return mkdir(path, mode); }

int scalanative_chmod(char *pathname, mode_t mode) {
  return chmod(pathname, mode);
}

int scalanative_fchmod(int fd, mode_t mode) { return fchmod(fd, mode); }

mode_t scalanative_s_isuid() { return S_ISUID; }

mode_t scalanative_s_isgid() { return S_ISGID; }

mode_t scalanative_s_isvtx() { return S_ISVTX; }

mode_t scalanative_s_irusr() { return S_IRUSR; }

mode_t scalanative_s_iwusr() { return S_IWUSR; }

mode_t scalanative_s_ixusr() { return S_IXUSR; }

mode_t scalanative_s_irgrp() { return S_IRGRP; }

mode_t scalanative_s_iwgrp() { return S_IWGRP; }

mode_t scalanative_s_ixgrp() { return S_IXGRP; }

mode_t scalanative_s_iroth() { return S_IROTH; }

mode_t scalanative_s_iwoth() { return S_IWOTH; }

mode_t scalanative_s_ixoth() { return S_IXOTH; }

int scalanative_s_isdir(mode_t mode) { return S_ISDIR(mode); }

int scalanative_s_isreg(mode_t mode) { return S_ISREG(mode); }

int scalanative_s_ischr(mode_t mode) { return S_ISCHR(mode); }

int scalanative_s_isblk(mode_t mode) { return S_ISBLK(mode); }

int scalanative_s_isfifo(mode_t mode) { return S_ISFIFO(mode); }

int scalanative_s_islnk(mode_t mode) { return S_ISLNK(mode); }

int scalanative_s_issock(mode_t mode) { return S_ISSOCK(mode); }
