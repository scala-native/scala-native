#include <unistd.h>
#include "types.h"

int scalanative_f_ok() { return F_OK; }

int scalanative_r_ok() { return R_OK; }

int scalanative_w_ok() { return W_OK; }

int scalanative_x_ok() { return X_OK; }

int scalanative_stdin_fileno() { return STDIN_FILENO; }

int scalanative_stdout_fileno() { return STDOUT_FILENO; }

int scalanative_stderr_fileno() { return STDERR_FILENO; }

extern char **environ;
char **scalanative_environ() { return environ; }

extern char *optarg;
char *scalanative_optarg() { return optarg; }

extern int opterr;
int scalanative_opterr() { return opterr; }

extern int optind;
int scalanative_optind() { return optind; }

extern int optopt;
int scalanative_optopt() { return optopt; }
