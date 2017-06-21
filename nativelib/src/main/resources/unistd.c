#ifndef _WIN32
#include <unistd.h>
#else
#include "os_win_unistd.h"
#endif
#include "types.h"

int scalanative_f_ok() { return F_OK; }

int scalanative_r_ok() { return R_OK; }

int scalanative_w_ok() { return W_OK; }

int scalanative_x_ok() { return X_OK; }

int scalanative_stdin_fileno() { return STDIN_FILENO; }

int scalanative_stdout_fileno() { return STDOUT_FILENO; }

int scalanative_stderr_fileno() { return STDERR_FILENO; }

int scalanative_unistd_access(const char *path, int amode) {
#ifndef _WIN32
    return access(path, amode);
#else
    return os_win_unistd_access(path, amode);
#endif
}

int scalanative_symlink(char *path1, char *path2) {
    return symlink(path1, path2);
}

int scalanative_symlinkat(char *path1, int fd, char *path2) {
    return symlinkat(path1, fd, path2);
}

int scalanative_link(char *oldpath, char *newpath) {
    return link(oldpath, newpath);
}

int scalanative_linkat(int fd1, char *path1, int fd2, char *path2, int flag) {
    return linkat(fd1, path1, fd2, path2, flag);
}

int scalanative_chown(char *path, scalanative_uid_t owner,
                      scalanative_gid_t group) {
    return chown(path, owner, group);
}

int scalanative_unistd_sleep(unsigned int seconds) {
#ifndef _WIN32
    return sleep(seconds);
#else
    return os_win_unistd_sleep(seconds);
#endif
}

int scalanative_unistd_usleep(unsigned int usecs) {
#ifndef _WIN32
    return usleep(usecs);
#else
    return os_win_unistd_usleep(usecs);
#endif
}

int scalanative_unistd_unlink(const char *path) {
#ifndef _WIN32
    return unlink(path);
#else
    return os_win_unistd_unlink(path);
#endif
}

int scalanative_unistd_readlink(const char *path, const char *buf,
                                unsigned int bufsize) {
#ifndef _WIN32
    return readlink(path, buf, bufsize);
#else
    return os_win_unistd_readlink(path, buf, bufsize);
#endif
}

const char *scalanative_unistd_getcwd(char *buf, unsigned int size) {
#ifndef _WIN32
    return getcwd(buf, size);
#else
    return os_win_unistd_getcwd(buf, size);
#endif
}

int scalanative_unistd_write(int fildes, void *buf, unsigned int nbyte) {
#ifndef _WIN32
    return write(fildes, buf, nbyte);
#else
    return os_win_unistd_write(fildes, buf, nbyte);
#endif
}

int scalanative_unistd_read(int fildes, void *buf, unsigned int nbyte) {
#ifndef _WIN32
    return read(fildes, buf, nbyte);
#else
    return os_win_unistd_read(fildes, buf, nbyte);
#endif
}

int scalanative_unistd_close(int fildes) {
#ifndef _WIN32
    return close(fildes);
#else
    return os_win_unistd_close(fildes);
#endif
}

int scalanative_unistd_fsync(int fildes) {
#ifndef _WIN32
    return fsync(fildes);
#else
    return os_win_unistd_fsync(fildes);
#endif
}

off_t scalanative_unistd_lseek(int fildes, off_t offset, int whence) {
#ifndef _WIN32
    return lseek(fildes, offset, whence);
#else
    return os_win_unistd_lseek(fildes, offset, whence);
#endif
}

int scalanative_unistd_ftruncate(int fildes, off_t length) {
#ifndef _WIN32
    return ftruncate(fildes, length);
#else
    return os_win_unistd_ftruncate(fildes, length);
#endif
}

int scalanative_unistd_truncate(const char *path, off_t length) {
#ifndef _WIN32
    return truncate(path, length);
#else
    return os_win_unistd_truncate(path, length);
#endif
}