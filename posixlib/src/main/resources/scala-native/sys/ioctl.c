#include <sys/ioctl.h>

int scalanative_ioctl(int fd, long int request, void *argp) {
    return ioctl(fd, request, argp);
}

long int scalanative_FIONREAD() { return FIONREAD; }
