#ifndef _WIN32
#include <unistd.h>
#else
#include "os_win_unistd.h"
#include "os_win_dirent.h"
#include "types.h"

typedef int mode_t;

mode_t getAccessMode(const char *path);

#endif

int scalanative_f_ok() { return F_OK; }

int scalanative_r_ok() { return R_OK; }

int scalanative_w_ok() { return W_OK; }

int scalanative_x_ok() { return X_OK; }

int scalanative_stdin_fileno() { return STDIN_FILENO; }

int scalanative_stdout_fileno() { return STDOUT_FILENO; }

int scalanative_stderr_fileno() { return STDERR_FILENO; }

int scalanative_access(const char *path, int amode) {
#ifndef _WIN32
    return access(path, amode);
#else
    if (path == 0 || strlen(path) == 0) {
        return -1;
    }

    mode_t mode = getAccessMode(path);

    if (amode == F_OK) {
        return mode != -1 ? 0 : -1;
    }

    if (((amode & R_OK) == R_OK) && ((mode & S_IRUSR) != S_IRUSR)) {
        return -1;
    }

    if (((amode & W_OK) == W_OK) && ((mode & S_IWUSR) != S_IWUSR)) {
        return -1;
    }

    if (((amode & X_OK) == X_OK) && ((mode & S_IXUSR) != S_IXUSR)) {
        return -1;
    }
    return 0;
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
