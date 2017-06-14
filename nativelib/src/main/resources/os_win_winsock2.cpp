#ifdef _WIN32

#include "os_win_winsock2.h"

extern "C" char *win_inet_ntoa(int family, struct in_addr* in)
{
    const int buf_size = INET6_ADDRSTRLEN + 1;
    static char buf[buf_size];
    InetNtopA(family, in, buf, buf_size);
    return buf;
}

extern "C" ssize_t readv(int fd, const struct iovec *iov, int iovcnt)
{
    return 0;
}
extern "C" ssize_t writev(int fd, const struct iovec *iov, int iovcnt)
{
    return 0;
}

#endif