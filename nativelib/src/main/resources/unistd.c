#include <unistd.h>

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
