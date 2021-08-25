#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <winsock2.h>
#pragma comment(lib, "Ws2_32.lib")
#else
#include <sys/ioctl.h>
#endif

int scalanative_ioctl(int fd, long int request, void *argp) {
#ifdef _WIN32
    return ioctlsocket(fd, request, argp);
#else
    return ioctl(fd, request, argp);
#endif
}

long int scalanative_fionread() { return FIONREAD; }
