#if defined(__linux__)

#if __has_include(<sys/syscall.h>)
#include <sys/syscall.h>
#endif

#ifndef SYS_pidfd_open
// Provide a fallback for developers who many not have a full linux
// installation.
#define SYS_pidfd_open 434
#endif

#include <unistd.h>

// pidfd_open was first introduced in Linux 5.3. Be sure to check return status.
int scalanative_linux_pidfd_open(pid_t pid, unsigned int flags) {
    return syscall(SYS_pidfd_open, pid, flags);
}
#endif // __linux__
