#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_POLL)

#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <poll.h>

short scalanative_pollin() { return POLLIN; }
short scalanative_pollpri() { return POLLPRI; }
short scalanative_pollout() { return POLLOUT; }

// XOpen events
short scalanative_pollrdnorm() { return POLLRDNORM; }
short scalanative_pollrdband() { return POLLRDBAND; }
short scalanative_pollwrnorm() { return POLLWRNORM; }
short scalanative_pollwrband() { return POLLWRBAND; }

// Always checked in revents
short scalanative_pollerr() { return POLLERR; }
short scalanative_pollhup() { return POLLHUP; }
short scalanative_pollnval() { return POLLNVAL; }

#endif // __unix__

#endif // __SCALANATIVE_POSIX_POLL
