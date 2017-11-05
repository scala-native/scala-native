#ifndef _WIN32
#include <sys/ioctl.h>
#else
#include "../../os_win_winsock2.h"
#endif

int scalanative_ioctl(int fd, long int request, void *argp) {
#ifndef _WIN32
    return ioctl(fd, request, argp);
#else
    return ioctlsocket(fd, request, argp);
#endif
}

long int scalanative_FIONREAD() { return FIONREAD; }
