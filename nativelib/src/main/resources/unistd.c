#include <unistd.h>

int scalanative_f_ok() { return F_OK; }

int scalanative_r_ok() { return R_OK; }

int scalanative_w_ok() { return W_OK; }

int scalanative_x_ok() { return X_OK; }

int scalanative_stdin_fileno() { return STDIN_FILENO; }

int scalanative_stdout_fileno() { return STDOUT_FILENO; }

int scalanative_stderr_fileno() { return STDERR_FILENO; }
