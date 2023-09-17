#if defined(__linux__)

#if __has_include(<sys/syscall.h>) // Should almost always be true
#include <sys/syscall.h>
#endif

#ifndef SYS_pidfd_open
/* If syscall.h did not define, force syscall() error by giving known bad value
 *
 * This could give a false negative in the unexpected case where this file is
 * being compiled on a system which does not have 'sys/syscall.h' available.
 */
#define SYS_pidfd_open -1L
#endif

#include <stdbool.h>
#include <unistd.h>

// pidfd_open was first introduced in Linux 5.3. Be sure to check return status.
int scalanative_linux_pidfd_open(pid_t pid, unsigned int flags) {
    return syscall(SYS_pidfd_open, pid, flags);
}

bool scalanative_linux_has_pidfd_open() {
#if (SYS_pidfd_open == -1L)
    false // SYS_pidfd_open not in syscall.h, so probably not in this kernel
#else
    int pid = getpid(); // self

    int pidfd = scalanative_linux_pidfd_open(pid, 0);
    close(pidfd);

    return pidfd > 0;
#endif
}
#endif // __linux__
