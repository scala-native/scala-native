#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_FCNTL)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <fcntl.h>

int scalanative_f_dupfd() { return F_DUPFD; }

int scalanative_f_getfd() { return F_GETFD; }

int scalanative_f_setfd() { return F_SETFD; }

int scalanative_f_getfl() { return F_GETFL; }

int scalanative_f_setfl() { return F_SETFL; }

int scalanative_f_getlk() { return F_GETLK; }

int scalanative_f_setlk() { return F_SETLK; }

int scalanative_f_setlkw() { return F_SETLKW; }

int scalanative_f_getown() { return F_GETOWN; }

int scalanative_f_setown() { return F_SETOWN; }

int scalanative_fd_cloexec() { return FD_CLOEXEC; }

int scalanative_f_rdlck() { return F_RDLCK; }

int scalanative_f_unlck() { return F_UNLCK; }

int scalanative_f_wrlck() { return F_WRLCK; }

int scalanative_o_creat() { return O_CREAT; }

int scalanative_o_excl() { return O_EXCL; }

int scalanative_o_noctty() { return O_NOCTTY; }

int scalanative_o_trunc() { return O_TRUNC; }

int scalanative_o_append() { return O_APPEND; }

int scalanative_o_nonblock() { return O_NONBLOCK; }

int scalanative_o_sync() { return O_SYNC; }

int scalanative_o_accmode() { return O_ACCMODE; }

int scalanative_o_rdonly() { return O_RDONLY; }

int scalanative_o_rdwr() { return O_RDWR; }

int scalanative_o_wronly() { return O_WRONLY; }

struct scalanative_flock {
    off_t l_start; /* starting offset */
    off_t l_len;   /* len = 0 means until end of file */
    pid_t l_pid;   /* lock owner */
    int l_type;    /* lock type: read/write, etc. */
    int l_whence;  /* type of l_start */
};
/* POSIX does not define the order of fields in flock, and there can be an
 * unidentified amount of additional ones. Because of this, we have to access
 * them by name and not by position, which is impossible for now in the c
 * interop of Scala Native. This is a way around that.
 */
int scalanative_fcntl(int fd, int cmd, struct scalanative_flock *flock_struct) {
    struct flock flock_buf;
    flock_buf.l_start = flock_struct->l_start;
    flock_buf.l_len = flock_struct->l_len;
    flock_buf.l_pid = flock_struct->l_pid;
    flock_buf.l_type = (short)flock_struct->l_type;
    flock_buf.l_whence = (short)flock_struct->l_whence;

    return fcntl(fd, cmd, &flock_buf);
}

// On MacOS Arm64 it is defined as macro taking varargs delegating to _fcntl
int scalanative_fcntl_i(int fd, int cmd, int flags) {
    return fcntl(fd, cmd, flags);
}

// On MacOS Arm64 is's defined as macro taking varargs delagating to _open
int scalanative_open_m(const char *pathname, int flags, mode_t mode) {
    return open(pathname, flags, mode);
}

#endif // Unix or Mac OS
#endif