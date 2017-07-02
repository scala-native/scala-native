#ifndef _WIN32
#include <fcntl.h>
#include <unistd.h>
#else
#include "os_win_fcntl.h"
#endif
#include <stdarg.h>

int scalanative_o_rdonly() { return O_RDONLY; }

int scalanative_o_wronly() { return O_WRONLY; }

int scalanative_o_rdwr() { return O_RDWR; }

int scalanative_o_append() { return O_APPEND; }

int scalanative_o_creat() { return O_CREAT; }

int scalanative_o_trunc() { return O_TRUNC; }

int scalanative_f_dupfd() { return F_DUPFD; }

int scalanative_f_getfd() { return F_GETFD; }

int scalanative_f_setfd() { return F_SETFD; }

int scalanative_f_getfl() { return F_GETFL; }

int scalanative_f_setfl() { return F_SETFL; }

int scalanative_f_getown() { return F_GETOWN; }

int scalanative_f_setown() { return F_SETOWN; }

int scalanative_f_getlk() { return F_GETLK; }

int scalanative_f_setlk() { return F_SETLK; }

int scalanative_f_setlkw() { return F_SETLKW; }

int scalanative_fcntl_open(const char *pathname, int flags) {
#ifndef _WIN32
    return open(pathname, flags);
#else
    return os_win_fcntl_open(pathname, flags, 0x640);
#endif
}

int scalanative_fcntl_open_with_mode(const char *pathname, int flags,
                                     mode_t mode) {
#ifndef _WIN32
    return open(pathname, flags, mode);
#else
    return os_win_fcntl_open(pathname, flags, mode);
#endif
}

int scalanative_fcntl_close(int fd) {
#ifndef _WIN32
    return close(fd);
#else
    return os_win_fcntl_close(fd);
#endif
}

int scalanative_fcntl_fcntl(int fd, int cmd, va_list args) {
#ifndef _WIN32
    return fcntl(fd, cmd, args);
#else
    return os_win_fcntl_fcntl(fd, cmd, args);
#endif
}