#ifndef __NETINET_IN_H
#define __NETINET_IN_H

#include <inttypes.h>
#include "../sys/socket.h"

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#define WINSOCK_DEPRECATED_NO_WARNINGS
#pragma comment(lib, "Ws2_32.lib")
#include <WinSock2.h>
#include <WS2tcpip.h>
typedef uint32_t in_addr_t;
typedef uint16_t in_port_t;
#else
#include <netinet/in.h>
#endif

struct scalanative_in_addr {
    in_addr_t so_addr;
};

struct scalanative_in6_addr {
    uint8_t _s6_addr[16];
};

struct scalanative_sockaddr_in {
    scalanative_sa_family_t sin_family;
    in_port_t sin_port;
    struct scalanative_in_addr sin_addr;
    // sin_zero makes sizeof(scalanative_sockaddr_in) == sizeof(sockaddr)
    uint8_t _sin_zero[8]; // Posix allowed.
};

struct scalanative_sockaddr_in6 {
    struct scalanative_in6_addr sin6_addr;
    scalanative_sa_family_t sin6_family;
    in_port_t sin6_port;
    uint32_t sin6_flowinfo;
    uint32_t sin6_scope_id;
};

void scalanative_convert_in_addr(struct scalanative_in_addr *in,
                                 struct in_addr *out);
void scalanative_convert_in6_addr(struct scalanative_in6_addr *in,
                                  struct in6_addr *out);
void scalanative_convert_scalanative_in_addr(struct in_addr *in,
                                             struct scalanative_in_addr *out);
void scalanative_convert_scalanative_in6_addr(struct in6_addr *in,
                                              struct scalanative_in6_addr *out);

#endif // __NETINET_IN_H
