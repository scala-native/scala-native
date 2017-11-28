#include <sys/ioctl.h>

int scalanative_ioctl(int fd, long int request, void *argp) {
    return ioctl(fd, request, argp);
}

long int scalanative_FIONREAD() { return FIONREAD; }

long int scalanative_SIOCGIFCONF() { return SIOCGIFCONF; }
long int scalanative_SIOCGIFFLAGS() { return SIOCGIFFLAGS; }
long int scalanative_SIOCGIFHWADDR() { return SIOCGIFHWADDR; }
long int scalanative_SIOCGIFMTU() { return SIOCGIFMTU; }
