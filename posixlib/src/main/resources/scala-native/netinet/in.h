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
    uint32_t so_addr;
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

struct scalanative_ipv6_mreq {
    struct scalanative_in6_addr ipv6mr_multiaddr;
    uint32_t ipv6mr_interface;
};

#endif // __NETINET_IN_H
