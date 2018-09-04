#include <poll.h>

int scalanative_POLLIN() { return POLLIN; }

int scalanative_POLLRDNORM() { return POLLRDNORM; }

int scalanative_POLLRDBAND() { return POLLRDBAND; }

int scalanative_POLLPRI() { return POLLPRI; }

int scalanative_POLLOUT() { return POLLOUT; }

int scalanative_POLLWRNORM() { return POLLWRNORM; }

int scalanative_POLLWRBAND() { return POLLWRBAND; }

int scalanative_POLLERR() { return POLLERR; }

int scalanative_POLLHUP() { return POLLHUP; }

int scalanative_POLLNVAL() { return POLLNVAL; }
