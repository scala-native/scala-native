#ifndef _OS_WIN_WINSOCK2_H_
#define _OS_WIN_WINSOCK2_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "os_win_types.h"

/*
 * Include windows.h without Windows Sockets 1.1 to prevent conflicts with
 * Windows Sockets 2.0.
 */
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>
#include <winsock2.h>
#include <In6addr.h>
#include <Ws2tcpip.h>

#if !defined(INET_ADDRSTRLEN)
#define INET_ADDRSTRLEN 16
#endif

#if !defined(INET6_ADDRSTRLEN)
#define INET6_ADDRSTRLEN 48
#endif

#if !defined(IPPROTO_IPV6)
#define IPPROTO_IPV6 41
#endif

#if !defined(IPV6_UNICAST_HOPS)
#define IPV6_UNICAST_HOPS 4
#endif

#if !defined(IPV6_MULTICAST_IF)
#define IPV6_MULTICAST_IF 9
#endif

#if !defined(IPV6_MULTICAST_HOPS)
#define IPV6_MULTICAST_HOPS 10
#endif

#if !defined(IPV6_MULTICAST_LOOP)
#define IPV6_MULTICAST_LOOP 11
#endif

#if !defined(IPV6_JOIN_GROUP)
#define IPV6_JOIN_GROUP 12
#endif

#if !defined(IPV6_LEAVE_GROUP)
#define IPV6_LEAVE_GROUP 13
#endif

#if !defined(IPV6_V6ONLY)
#define IPV6_V6ONLY 27
#endif

#define MSG_EOR 0x8 /* data completes record */

#define SCM_RIGHTS 0x01

struct iovec {
    void *iov_base; /** Base address of a memory region for input or output. */
    size_t iov_len; /** The size of the memory pointed to by iov_base. */
};

char *os_win_inet_ntoa(int family, struct in_addr *in);
int os_win_socket(int domain, int type, int protocol);
int os_win_closesocket(int fildes);
in_addr_t os_win_inet_addr4(char *in);
in_addr6_t os_win_inet_addr6(char *in);

ssize_t readv(int fd, const struct iovec *iov, int iovcnt);
ssize_t writev(int fd, const struct iovec *iov, int iovcnt);

#ifdef __cplusplus
}
#endif

#endif