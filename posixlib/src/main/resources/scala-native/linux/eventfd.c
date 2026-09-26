#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_EVENTFD)

#ifdef __linux__

#include <sys/eventfd.h>

int scalanative_efd_cloexec() { return EFD_CLOEXEC; }
int scalanative_efd_nonblock() { return EFD_NONBLOCK; }
int scalanative_efd_semaphore() { return EFD_SEMAPHORE; }

#endif // __linux__

#endif // __SCALANATIVE_POSIX_EVENTFD
