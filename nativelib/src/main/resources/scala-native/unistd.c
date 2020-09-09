#include <unistd.h>
#include "types.h"

int scalanative_f_ok() { return F_OK; }

int scalanative_r_ok() { return R_OK; }

int scalanative_w_ok() { return W_OK; }

int scalanative_x_ok() { return X_OK; }

int scalanative_stdin_fileno() { return STDIN_FILENO; }

int scalanative_stdout_fileno() { return STDOUT_FILENO; }

int scalanative_stderr_fileno() { return STDERR_FILENO; }

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
