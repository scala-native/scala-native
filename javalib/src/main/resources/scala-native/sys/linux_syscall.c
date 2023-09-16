#if defined(__linux__)

#if __has_include(<sys/syscall.h>)
#include <sys/syscall.h>
#endif // syscall.h

// 2023-09-16 07:16 -0400 Lee -- If you decide at somepoint to go the
// path of defining SYS_pidfd_open, it should probably be to -1, NOT 434.
//
// 434 was intended to be "helpful". Almost a year's experience has shown
// that path not to be helpful at all.  If one does not know the constant,
// one should probably __not__ be using what could well be a j-random value
// used for another syscall entirely.
//
// Looks like the next evolution should first check if the value is defined.
// if not, return "false". If true, probe the actually OS by trying to
// use that constant to pidfd_open the current process (getpid()).  This
// should probably be done in an init() method, and lazily called (once)
// by the UnixProcess() code.  The presence or absence of a working
// pidfd_open() is not going to change over the lifetime of the execution.
//
// Looks like the underlying issue is that 

#ifndef SYS_pidfd_open
// Provide a fallback for developers who many not have a full linux
// installation.
#define SYS_pidfd_open -1 // Force InvalidArgument failure is ever executed.
#endif

#include <unistd.h>

// pidfd_open was first introduced in Linux 5.3. Be sure to check return status.
int scalanative_linux_pidfd_open(pid_t pid, unsigned int flags) {
    return syscall(SYS_pidfd_open, pid, flags);
}

#endif // __linux__
