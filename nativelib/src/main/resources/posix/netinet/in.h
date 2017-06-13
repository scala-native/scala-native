#ifndef __NETINET_IN_H
#define __NETINET_IN_H

#include <netinet/in.h>
#include <inttypes.h>
#include "../sys/socket.h"

struct scalanative_in_addr {
    in_addr_t s_addr;
};

struct scalanative_in6_addr {
    uint8_t _s6_addr[16];
};

struct scalanative_sockaddr_in {
    scalanative_sa_family_t sin_family;
    in_port_t sin_port;
    struct scalanative_in_addr sin_addr;
};

struct scalanative_sockaddr_in6 {
    struct scalanative_in6_addr sin6_addr;
    scalanative_sa_family_t sin6_family;
    in_port_t sin6_port;
    uint32_t sin6_flowinfo;
    uint32_t sin6_scope_id;
    uint32_t padding; // So that the struct has the same
                      // size in C and in Native.
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
