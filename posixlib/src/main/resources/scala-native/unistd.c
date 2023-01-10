#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <unistd.h>
#include "types.h"

#if defined(__FreeBSD__)

/* Apply a Pareto cost/benefit analysis here.
 *
 * Some relevant constants are not defined on FreeBSD.
 * This implementation is one of at least 3 design possibilities. One can:
 *   1) cause a runtime or semantic error by returning "known wrong" values
 *      as done here. This causes only the parts of applications which
 *      actually use the constants to, hopefully, fail.
 *
 *   2) cause a link time error.
 *
 *   3) cause a compile time error.
 *
 * The last ensure that no wrong constants slip out to a user but they also
 * prevent an application developer from getting the parts of an application
 * which do not actually use the constants from running.
 */
#define _XOPEN_VERSION 0
#define _PC_2_SYMLINKS 0
#define _SC_SS_REPL_MAX 0
#define _SC_TRACE_EVENT_NAME_MAX 0
#define _SC_TRACE_NAME_MAX 0
#define _SC_TRACE_SYS_MAX 0
#define _SC_TRACE_USER_EVENT_MAX 0
#endif // __FreeBSD__

long scalanative__posix_version() { return _POSIX_VERSION; }

int scalanative__xopen_version() { return _XOPEN_VERSION; }

int scalanative_f_ok() { return F_OK; }

int scalanative_r_ok() { return R_OK; }

int scalanative_w_ok() { return W_OK; }

int scalanative_x_ok() { return X_OK; }

// SEEK_CUR, SEEK_END, SEEK_SET in clib stdio

int scalanative_f_lock() { return F_LOCK; }

int scalanative_f_test() { return F_TEST; }

int scalanative_f_tlock() { return F_TLOCK; }

int scalanative_f_ulock() { return F_ULOCK; }

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

#endif // Unix or Mac OS
