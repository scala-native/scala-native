#if defined(__linux__)

#if __has_include(<sys/syscall.h>) // Should almost always be true
#include <sys/syscall.h>
#endif

#ifndef SYS_pidfd_open
/* There are at least two cases to consider here. Both mean that pidfd_open()
 * is not available for use.
 *
 * This code may be compiled in varied build environments. It is possible
 * to get a false negatives.
 *
 * To aid future tracing and debugging:
 *
 * 1) sys/syscall.h did not exist.  This is probably because it is missing
 *    on the local/user include path. The file should almost always be
 *    present.
 *
 *    Solution: add the file to the default include path. If the target OS
 *    supports pidfd_open() and that support is intended to be used, make
 *    sure syscall.h defines the SYS_pidfd_open macro.
 *
 * 2) sys/syscall.h exists but does not define SYS_pid_open. If one makes
 *    the reasonable assumption that the .h files and OS correspond, then
 *    this most probably means that the OS does not support pidfd_open().
 *    An example would be Linux before V5.3.
 *
 *    This path avoids a nasty Linux version parse.
 */

#define SYS_pidfd_open -1L // all valid syscall numbers are >= 0.
#endif

#include <stdbool.h>
#include <unistd.h>

// pidfd_open was first introduced in Linux 5.3. Be sure to check return status.
int scalanative_linux_pidfd_open(pid_t pid, unsigned int flags) {
#if (SYS_pidfd_open <= 0)
    return -1;
#else
    return syscall(SYS_pidfd_open, pid, flags);
#endif
}

bool scalanative_linux_has_pidfd_open() {
#if (SYS_pidfd_open <= 0)
    false; // SYS_pidfd_open not in syscall.h, so probably not in this kernel
#else
    /* For those following along:
     * By this point, the OS is known to be Linux. The distribution and
     * compilation environment are know to support pidfd_open(). linux-arm64
     * build failures seem to indicate there is a way to tailor off pidfd_open()
     * support. Ask the OS itself exactly what it supports.
     */
    int pid = getpid(); // self

    int pidfd = scalanative_linux_pidfd_open(pid, 0);
    close(pidfd);

    return pidfd > 0;
#endif
}
#endif // __linux__
